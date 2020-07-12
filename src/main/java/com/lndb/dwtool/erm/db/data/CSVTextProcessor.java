package com.lndb.dwtool.erm.db.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.util.StringUtil;

public class CSVTextProcessor implements FileProcessor {

    public FileStatistics process(String connectionName, TableDescriptor tableDescriptor, File dataFile, boolean headerIncluded, SchemaJoinMetaData joinMetaData, File good, File bad) {
	FileStatistics statistics = new FileStatistics();
	ConnectionDetail connectionDetail = ConnectionDetail.configure(connectionName);
	Connection con = null;
	try {
	    con = DatabaseConnection.newConnection(connectionDetail);
	    process(con, statistics, connectionName, tableDescriptor, dataFile, headerIncluded, joinMetaData, good, bad);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    try {
		DatabaseConnection.release(con);
	    } catch (SQLException e) {
		throw new RuntimeException(e);
	    }
	}
	return statistics;

    }

    public void process(Connection con, FileStatistics statistics, String connectionName, TableDescriptor tableDescriptor, File dataFile, boolean headerIncluded, SchemaJoinMetaData joinMetaData,
	    File good, File bad) {

	BufferedReader reader = null;
	BufferedWriter goodWriter = null;
	BufferedWriter badWriter = null;
	try {
	    goodWriter = new BufferedWriter(new FileWriter(good));
	    badWriter = new BufferedWriter(new FileWriter(bad));
	    reader = new BufferedReader(new FileReader(dataFile));
	    List<String> tableHeaders = referenceHeaders(tableDescriptor);
	    String line = null;
	    String[] dataCols = null;
	    String[] refheaders = validatedHeaders(headerIncluded, statistics, reader, tableHeaders);

	    if (!statistics.isImportable()) {
		return;
	    }
	    statistics.setRefheaders(refheaders);
	    while ((line = reader.readLine()) != null) {
		if (line.trim().length() > 0) {
		    dataCols = StringUtil.parseQuoted(',', line);
		    if (dataCols == null || dataCols.length == 0) {
			continue;
		    }
		    if (dataCols.length != refheaders.length) {
			writeBadLine(statistics, badWriter, line, null);
		    } else {
			TableRow tableRow = new TableRow(refheaders, tableDescriptor, dataCols);
			if (tableRow.validateDataTypes()) {
			    if ("FAIL".equals(tableRow.checkDataIntegrity(con, joinMetaData))) {
				writeBadLine(statistics, badWriter, line, tableRow.getErrorsFound());
			    } else {
				writeGoodLine(statistics, goodWriter, line);
			    }
			} else {
			    writeBadLine(statistics, badWriter, line, tableRow.getErrorsFound());
			}

		    }
		}
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    releaseIO(reader, goodWriter, badWriter);
	}
	return;
    }

    private void releaseIO(BufferedReader reader, BufferedWriter goodWriter, BufferedWriter badWriter) {
	try {
	    if (reader != null) {
		reader.close();
	    }
	    if (goodWriter != null) {
		goodWriter.flush();
		goodWriter.close();
	    }

	    if (badWriter != null) {
		badWriter.flush();
		badWriter.close();
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private void writeGoodLine(FileStatistics statistics, BufferedWriter goodWriter, String line) throws IOException {
	if (statistics.getGoodCount() == 0) {
	    goodWriter.write(statistics.headerLine());
	    goodWriter.newLine();
	    goodWriter.flush();
	}
	statistics.incrementGoodLineCount();
	goodWriter.write(line);
	goodWriter.newLine();
	goodWriter.flush();
    }

    private void writeBadLine(FileStatistics statistics, BufferedWriter badWriter, String line, List<String> errors) throws IOException {
	if (statistics.getBadCount() == 0) {
	    badWriter.write(statistics.headerLine());
	    if (errors != null && !errors.isEmpty()) {
		badWriter.write(", ERRORS");
	    }
	    badWriter.newLine();
	    badWriter.flush();
	}
	statistics.incrementBadLineCount();
	badWriter.write(line);
	if (errors != null && !errors.isEmpty()) {
	    badWriter.write(",[");
	    for (String err : errors) {
		badWriter.write(err + ";");
	    }
	    badWriter.write("]");
	}
	badWriter.newLine();
	badWriter.flush();
    }

    private String[] validatedHeaders(boolean headerIncluded, FileStatistics statistics, BufferedReader reader, List<String> refheaders) throws IOException {
	String[] headers = null;
	if (headerIncluded) {
	    headers = StringUtil.parseQuoted(',', reader.readLine().toUpperCase());
	    if (headers == null || headers.length == 0) {
		statistics.setFileError("File headers not found");
		statistics.setImportable(false);
	    } else {
		for (String hdr : headers) {
		    if (!refheaders.contains(hdr.toUpperCase())) {
			statistics.setFileError("File headers don't match table headers");
			statistics.setImportable(false);
		    }
		}
	    }
	}

	if (statistics.isImportable() && headerIncluded) {
	    return headers;
	}
	return refheaders.toArray(new String[] {});
    }

    private List<String> referenceHeaders(TableDescriptor tableDescriptor) {
	List<String> refCols = new ArrayList<String>();
	for (ColumnDescriptor col : tableDescriptor.getColumns()) {
	    refCols.add(col.getName().toUpperCase());
	}
	return refCols;
    }

    public FileStatistics performDBUpdates(String connectionName, TableDescriptor tableDescriptor, File goodData) {
	FileStatistics statistics = new FileStatistics();
	performDBUpdates(statistics, connectionName, tableDescriptor, goodData);
	return statistics;
    }

    public void performDBUpdates(FileStatistics statistics, String connectionName, TableDescriptor tableDescriptor, File goodData) {
	Connection con = null;
	BufferedReader reader = null;
	try {
	    ConnectionDetail connectionDetail = ConnectionDetail.configure(connectionName);
	    con = DatabaseConnection.newConnection(connectionDetail);
	    con.setAutoCommit(false);

	    reader = new BufferedReader(new FileReader(goodData));
	    String[] headers = StringUtil.parseQuoted(',', reader.readLine());
	    String[] dataCols = null;
	    String line = null;
	    PreparedStatement updtStmt = con.prepareStatement(TableRow.prepareUpdateStatement(headers, tableDescriptor));
	    PreparedStatement insertStmt = con.prepareStatement(TableRow.prepareInsertStatement(headers, tableDescriptor));

	    while ((line = reader.readLine()) != null) {
		dataCols = StringUtil.parseQuoted(',', line);
		TableRow tableRow = new TableRow(headers, tableDescriptor, dataCols);
		tableRow.updateStatementParams(updtStmt);
		int updtCount = updtStmt.executeUpdate();
		if (updtCount > 0) {
		    statistics.incrementUpdateRecordCount(updtCount);
		} else {
		    try {
			tableRow.insertStatementParams(insertStmt);
			insertStmt.executeUpdate();
			statistics.incrementInsertRecordCount(1);
			insertStmt.clearParameters();
		    } catch (Exception e) {
			e.printStackTrace();
			statistics.incrementBadLineCount();
		    }
		}
		updtStmt.clearParameters();
	    }
	    updtStmt.clearParameters();
	    insertStmt.clearParameters();
	    insertStmt.close();
	    updtStmt.close();
	    con.commit();
	    reader.close();
	} catch (Exception e) {
	    try {
		con.rollback();
	    } catch (SQLException e1) {
		throw new RuntimeException(e);
	    }
	    throw new RuntimeException(e);
	} finally {
	    try {
		if (reader != null) {
		    reader.close();
		}
		DatabaseConnection.release(con);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
	return;
    }

    public void performUncommitedUpdates(Connection con, TableDescriptor tableDescriptor, File goodData) {
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(goodData));
	    String[] headers = StringUtil.parseQuoted(',', reader.readLine());
	    String[] dataCols = null;
	    String line = null;
	    PreparedStatement updtStmt = con.prepareStatement(TableRow.prepareUpdateStatement(headers, tableDescriptor));
	    PreparedStatement insertStmt = con.prepareStatement(TableRow.prepareInsertStatement(headers, tableDescriptor));

	    while ((line = reader.readLine()) != null) {
		dataCols = StringUtil.parseQuoted(',', line);
		TableRow tableRow = new TableRow(headers, tableDescriptor, dataCols);
		tableRow.updateStatementParams(updtStmt);
		int updtCount = updtStmt.executeUpdate();
		if (updtCount == 0) {
		    tableRow.insertStatementParams(insertStmt);
		    insertStmt.executeUpdate();
		    insertStmt.clearParameters();
		}
		updtStmt.clearParameters();
	    }
	    updtStmt.clearParameters();
	    insertStmt.clearParameters();
	    insertStmt.close();
	    updtStmt.close();
	    reader.close();
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    try {
		if (reader != null) {
		    reader.close();
		}
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
	return;
    }

}
