package com.lndb.dwtool.erm.db.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.util.ObjectValueUtils;

public class ExcelProcessor implements FileProcessor {

    /*
     * (non-Javadoc)
     * 
     * @see com.lndb.dwtool.erm.db.data.FileProcessor#process(java.lang.String,
     * com.lndb.dwtool.erm.db.TableDescriptor, java.io.File, boolean,
     * com.lndb.dwtool.erm.SchemaJoinMetaData, java.io.File, java.io.File)
     */
    public void process(Connection con, FileStatistics statistics, String connectionName, TableDescriptor tableDescriptor, File dataFile, boolean headerIncluded, SchemaJoinMetaData joinMetaData,
	    File good, File bad) {

	FileOutputStream badStream = null;
	FileOutputStream goodStream = null;
	FileInputStream dataInputStream = null;
	try {
	    dataInputStream = new FileInputStream(dataFile);
	    badStream = new FileOutputStream(bad);
	    goodStream = new FileOutputStream(good);
	    Workbook wb = new HSSFWorkbook(new POIFSFileSystem(dataInputStream));
	    Workbook goodWb = new HSSFWorkbook();
	    Workbook badWb = new HSSFWorkbook();
	    Sheet goodSheet = goodWb.createSheet("Good");
	    Sheet badSheet = badWb.createSheet("Bad");

	    if (wb.getSheetAt(0) == null) {
		statistics.setImportable(false);
		return;
	    }
	    Iterator<Row> rows = wb.getSheetAt(0).iterator();

	    List<String> tableHeaders = referenceHeaders(tableDescriptor);
	    Object[] dataCols = null;
	    Row headerRow = null;
	    if (headerIncluded) {
		headerRow = (rows != null && rows.hasNext()) ? rows.next() : null;
	    }
	    String[] refheaders = validatedHeaders(headerIncluded, statistics, headerRow, tableHeaders);

	    if (!statistics.isImportable()) {
		return;
	    }
	    statistics.setRefheaders(refheaders);
	    Row row = null;
	    short goodRowCnt = 0;
	    short badRowCnt = 0;
	    while (rows.hasNext()) {
		row = rows.next();
		dataCols = readDataCells(row, tableDescriptor, refheaders);
		if (dataCols == null || dataCols.length == 0) {
		    continue;
		}
		if (dataCols.length != refheaders.length) {
		    badRowCnt++;
		    writeBadLine(statistics, badWb, badSheet, row, badRowCnt, refheaders, null);
		} else {
		    TableRow tableRow = new TableRow(refheaders, tableDescriptor, dataCols);
		    if (tableRow.validateDataTypes()) {
			if ("FAIL".equals(tableRow.checkDataIntegrity(con, joinMetaData))) {
			    badRowCnt++;
			    writeBadLine(statistics, badWb, badSheet, row, badRowCnt, refheaders, tableRow.getErrorsFound());
			} else {
			    goodRowCnt++;
			    writeGoodLine(statistics, goodWb, goodSheet, row, goodRowCnt, refheaders);
			}
		    } else {
			badRowCnt++;
			writeBadLine(statistics, badWb, badSheet, row, badRowCnt, refheaders, tableRow.getErrorsFound());
		    }
		}
	    }
	    badWb.write(badStream);
	    goodWb.write(goodStream);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    release(dataInputStream, goodStream, badStream);
	}
	return;
    }

    private void writeGoodLine(FileStatistics statistics, Workbook wb, Sheet goodSheet, Row currRow, short rowNum, String[] headers) {
	if (statistics.getGoodCount() == 0) {
	    writeHeaderLine(goodSheet, headers, (short) (rowNum - 1), false);
	}
	statistics.incrementGoodLineCount();
	writeLine(goodSheet, currRow, rowNum, wb, null);
    }

    private void writeBadLine(FileStatistics statistics, Workbook wb, Sheet badSheet, Row currRow, short rowNum, String[] headers, List<String> errors) {
	if (statistics.getBadCount() == 0) {
	    writeHeaderLine(badSheet, headers, (short) (rowNum - 1), true);
	}

	statistics.incrementBadLineCount();
	writeLine(badSheet, currRow, rowNum, wb, errors);
    }

    private void writeLine(Sheet sheet, Row currRow, short rowNum, Workbook wb, List<String> errors) {
	Row newRow = sheet.createRow(rowNum);
	Iterator<Cell> cells = currRow.iterator();
	Cell currCell = null;
	short pos = 0;
	while (cells.hasNext()) {
	    currCell = cells.next();
	    Cell newCell = newRow.createCell(pos, currCell.getCellType());
	    setCellValue(currCell, newCell);
	    CellStyle newCellStyle = wb.createCellStyle();
	    ObjectValueUtils.copySimpleProperties(currCell.getCellStyle(), newCellStyle);
	    newCell.setCellStyle(newCellStyle);
	    pos++;
	}
	if (errors != null && !errors.isEmpty()) {
	    StringBuilder sb = new StringBuilder();
	    for (String err : errors) {
		sb.append(err);
		sb.append("; ");
	    }
	    Cell newCell = newRow.createCell(pos, Cell.CELL_TYPE_NUMERIC);
	    newCell.setCellValue(new HSSFRichTextString(sb.toString()));
	    pos++;
	}
    }

    private void writeHeaderLine(Sheet sheet, String[] headers, short rowNum, boolean errors) {
	Row newRow = sheet.createRow(rowNum);
	short pos = 0;
	for (String hdr : headers) {
	    Cell newCell = newRow.createCell(pos);
	    newCell.setCellValue(new HSSFRichTextString(hdr));
	    pos++;
	}
	if (errors) {
	    Cell newCell = newRow.createCell(pos);
	    newCell.setCellValue(new HSSFRichTextString("ERRORS"));
	    pos++;
	}
    }

    private void setCellValue(Cell cell, Cell newCell) {
	int cellType = cell.getCellType();
	switch (cellType) {
	case Cell.CELL_TYPE_BLANK:
	    break;
	case Cell.CELL_TYPE_BOOLEAN:
	    newCell.setCellValue(cell.getBooleanCellValue());
	    break;
	case Cell.CELL_TYPE_ERROR:
	    newCell.setCellValue(cell.getErrorCellValue());
	    break;

	case Cell.CELL_TYPE_FORMULA:
	    newCell.setCellFormula(cell.getCellFormula());
	    break;
	case Cell.CELL_TYPE_NUMERIC:
	    newCell.setCellValue(cell.getNumericCellValue());
	    break;
	case Cell.CELL_TYPE_STRING:
	    newCell.setCellValue(cell.getRichStringCellValue());
	    break;
	default:
	    newCell.setCellValue(cell.getRichStringCellValue());
	    break;
	}
    }

    private void release(InputStream dis, OutputStream goodStream, OutputStream badStream) {
	try {
	    if (dis != null) {
		dis.close();
	    }
	    if (goodStream != null) {
		goodStream.flush();
		goodStream.close();
	    }
	    if (badStream != null) {
		badStream.flush();
		badStream.close();
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private String[] validatedHeaders(boolean headerIncluded, FileStatistics statistics, Row headerRow, List<String> refheaders) {
	String[] headers = null;
	if (headerIncluded && headerRow != null) {
	    headers = readHeaderCells(headerRow, refheaders.size());
	    if (headers == null || headers.length == 0) {
		statistics.setFileError("File headers not found");
		statistics.setImportable(false);
	    } else {
		for (String hdr : headers) {
		    if (!refheaders.contains(hdr.toUpperCase())) {
			statistics.setFileError("File headers don't match table headers" + hdr.toUpperCase());
			statistics.setImportable(false);
		    }
		}
	    }
	} else if (headerIncluded) {
	    statistics.setFileError("File headers not found");
	    statistics.setImportable(false);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.lndb.dwtool.erm.db.data.FileProcessor#performDBUpdates(java.lang.String,
     * com.lndb.dwtool.erm.db.TableDescriptor, java.io.File)
     */
    public void performDBUpdates(FileStatistics statistics, String connectionName, TableDescriptor tableDescriptor, File goodData) {
	Connection con = null;
	FileInputStream dataInputStream = null;
	try {
	    ConnectionDetail connectionDetail = ConnectionDetail.configure(connectionName);
	    con = DatabaseConnection.newConnection(connectionDetail);
	    con.setAutoCommit(false);
	    dataInputStream = new FileInputStream(goodData);
	    Workbook wb = new HSSFWorkbook(new POIFSFileSystem(dataInputStream));
	    if (wb.getSheetAt(0) == null) {
		statistics.setImportable(false);
		return;
	    }
	    Iterator<Row> rows = wb.getSheetAt(0).iterator();
	    Row headerRow = rows.next();
	    String[] headers = readHeaderCells(headerRow, tableDescriptor.getColumns().size());

	    Object[] dataCols = null;
	    PreparedStatement updtStmt = con.prepareStatement(TableRow.prepareUpdateStatement(headers, tableDescriptor));
	    PreparedStatement insertStmt = con.prepareStatement(TableRow.prepareInsertStatement(headers, tableDescriptor));
	    Row row = null;
	    while (rows.hasNext()) {
		row = rows.next();
		dataCols = readDataCells(row, tableDescriptor, headers);
		TableRow tableRow = new TableRow(headers, tableDescriptor, dataCols);
		tableRow.updateStatementParams(updtStmt);
		int updtCount = updtStmt.executeUpdate();
		if (updtCount > 0) {
		    statistics.incrementUpdateRecordCount(updtCount);
		} else {
		    tableRow.insertStatementParams(insertStmt);
		    insertStmt.executeUpdate();
		    statistics.incrementInsertRecordCount(1);
		    insertStmt.clearParameters();
		}
		updtStmt.clearParameters();
	    }
	    updtStmt.clearParameters();
	    insertStmt.clearParameters();
	    insertStmt.close();
	    updtStmt.close();
	    con.commit();
	} catch (Exception e) {
	    try {
		con.rollback();
	    } catch (SQLException e1) {
		throw new RuntimeException(e);
	    }
	    throw new RuntimeException(e);
	} finally {
	    try {
		if (dataInputStream != null) {
		    dataInputStream.close();
		}
		DatabaseConnection.release(con);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
	return;
    }

    private String[] readHeaderCells(Row row, int maxCols) {
	Iterator<Cell> cells = row.iterator();
	Cell cell = null;
	List<String> data = new ArrayList<String>(maxCols);
	while (cells.hasNext()) {
	    cell = cells.next();
	    int cellType = cell.getCellType();
	    switch (cellType) {
	    case Cell.CELL_TYPE_STRING:
		data.add(cell.getRichStringCellValue().getString());
		break;
	    default:
		data.add("");
		break;
	    }
	}
	String[] headers = new String[data.size()];
	int pos = 0;
	for (String string : data) {
	    headers[pos] = string.toUpperCase();
	    pos++;
	}
	return headers;
    }

    private Object[] readDataCells(Row row, TableDescriptor descriptor, String[] refHeaders) {
	Iterator<Cell> cells = row.iterator();
	Cell cell = null;
	ArrayList<Object> data = new ArrayList<Object>(refHeaders.length);
	int pos = -1;
	while (cells.hasNext()) {
	    pos++;
	    if (pos == refHeaders.length) {
		break;
	    }
	    ColumnDescriptor column = descriptor.getColumn(refHeaders[pos].toUpperCase());
	    if (column == null) {
		data.add("");
		continue;
	    }
	    int jdbcType = column.getJdbcType();
	    cell = cells.next();
	    int cellType = cell.getCellType();
	    switch (cellType) {
	    case Cell.CELL_TYPE_BLANK:
		data.add("");
		break;
	    case Cell.CELL_TYPE_BOOLEAN:
		data.add(cell.getBooleanCellValue());
		break;
	    case Cell.CELL_TYPE_ERROR:
		data.add("");
		break;

	    case Cell.CELL_TYPE_FORMULA:
		data.add("");
		break;
	    case Cell.CELL_TYPE_NUMERIC:
		switch (jdbcType) {
		case Types.DATE:
		    data.add(cell.getDateCellValue() != null ? new Date(cell.getDateCellValue().getTime()) : "");
		    break;
		case Types.TIMESTAMP:
		    data.add(cell.getDateCellValue() != null ? new Timestamp(cell.getDateCellValue().getTime()) : "");
		    break;
		case Types.TIME:
		    data.add(cell.getDateCellValue() != null ? new Time(cell.getDateCellValue().getTime()) : "");
		    break;
		default:
		    if (column.getDecimalDigits() == 0) {
			data.add((int) cell.getNumericCellValue());
		    } else {
			data.add(cell.getNumericCellValue());
		    }
		    break;
		}
		break;
	    case Cell.CELL_TYPE_STRING:
		data.add(cell.getRichStringCellValue().getString());
		break;
	    default:
		data.add(cell.getRichStringCellValue().getString());
		break;
	    }
	}
	// add empty columns
	while (pos < refHeaders.length - 1) {
	    data.add("");
	    pos++;
	}
	return data.toArray();
    }

    public void performUncommitedUpdates(Connection con, TableDescriptor tableDescriptor, File goodData) {
	FileInputStream dataInputStream = null;
	try {
	    dataInputStream = new FileInputStream(goodData);
	    Workbook wb = new HSSFWorkbook(new POIFSFileSystem(dataInputStream));
	    if (wb.getSheetAt(0) == null) {
		return;
	    }
	    Iterator<Row> rows = wb.getSheetAt(0).iterator();
	    Row headerRow = rows.next();
	    String[] headers = readHeaderCells(headerRow, tableDescriptor.getColumns().size());

	    Object[] dataCols = null;
	    PreparedStatement updtStmt = con.prepareStatement(TableRow.prepareUpdateStatement(headers, tableDescriptor));
	    PreparedStatement insertStmt = con.prepareStatement(TableRow.prepareInsertStatement(headers, tableDescriptor));
	    Row row = null;
	    while (rows.hasNext()) {
		row = rows.next();
		dataCols = readDataCells(row, tableDescriptor, headers);
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
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    try {
		if (dataInputStream != null) {
		    dataInputStream.close();
		}
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
	return;
    }

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
}
