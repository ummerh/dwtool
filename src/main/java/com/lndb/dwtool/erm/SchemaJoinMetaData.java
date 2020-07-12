package com.lndb.dwtool.erm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class can combine two versions of same schema and produce an aggregated
 * definition. Mainly used to combine DB and OJB relationships
 * 
 * @author harsha07
 * 
 */
public class SchemaJoinMetaData {
    // primary relational map
    private RelationalMap primaryMap;
    // secondary relational map
    private RelationalMap secondaryMap;

    public SchemaJoinMetaData(RelationalMap baseMap, RelationalMap targetMap) {
	this.primaryMap = baseMap;
	this.secondaryMap = targetMap;
    }

    // tables list
    private Set<String> tables = new HashSet<String>();
    // ordered tables
    private List<String> orderedTables = new ArrayList<String>();

    // define relational order for the whole schema
    public List<String> defineOrder() {
	this.orderedTables.clear();
	this.tables.clear();
	List<String> allTables = primaryMap.getAllTables();
	for (String table : allTables) {
	    orderTable(primaryMap, secondaryMap, table);
	}
	return orderedTables;
    }

    // define relational order for a specific table
    public List<String> defineOrder(String tableName) {
	this.orderedTables.clear();
	this.tables.clear();
	orderTable(primaryMap, secondaryMap, tableName);
	return orderedTables;
    }

    /**
     * Method that iterates foreign keys of both schemas and orders table by
     * relations
     * 
     * @param baseMap
     *            primary relational map
     * @param targetMap
     *            secondary relational map
     * @param table
     *            Table name
     */
    private void orderTable(RelationalMap baseMap, RelationalMap targetMap, String table) {
	if (!tables.contains(table)) {
	    tables.add(table);
	    List<ForeignKey> foreignKeys = baseMap.getForeignKeys(table);
	    iterateForeignKeys(baseMap, targetMap, table, foreignKeys);
	    foreignKeys = targetMap.getForeignKeys(table);
	    iterateForeignKeys(baseMap, targetMap, table, foreignKeys);
	    orderedTables.add(table);
	}
    }

    /**
     * Iterates foreign keys and arrange by refer to table
     * 
     * @param baseMap
     * @param targetMap
     * @param table
     * @param foreignKeys
     */
    private void iterateForeignKeys(RelationalMap baseMap, RelationalMap targetMap, String table, List<ForeignKey> foreignKeys) {
	if (foreignKeys == null || foreignKeys.isEmpty()) {
	    return;
	}
	for (ForeignKey foreignKey : foreignKeys) {
	    if (foreignKey.isValid() && !foreignKey.getReferToTable().equalsIgnoreCase(table)) {
		orderTable(baseMap, targetMap, foreignKey.getReferToTable());
	    }
	}
    }

    /**
     * Exported keys, i.e. tables that refer to this specific table
     * 
     * @param tableName
     * @return
     */
    public List<ForeignKey> getExportedKeys(String tableName) {
	List<ForeignKey> exportedKeys = new ArrayList<ForeignKey>();
	List<ForeignKey> primaryKeys = primaryMap.getExportedKeys(tableName);
	List<ForeignKey> secondaryKeys = secondaryMap.getExportedKeys(tableName);
	if (primaryKeys != null) {
	    exportedKeys.addAll(primaryKeys);
	}
	if (secondaryKeys != null) {
	    exportedKeys.addAll(secondaryKeys);
	}
	return exportedKeys;
    }

    /**
     * Imported keys i.e. list of tables this specific table refers
     * 
     * @param tableName
     * @return
     */
    public List<ForeignKey> getImportedKeys(String tableName) {
	List<ForeignKey> importedKeys = new ArrayList<ForeignKey>();
	List<ForeignKey> baseKeys = primaryMap.getForeignKeys(tableName);
	List<ForeignKey> targetKeys = secondaryMap.getForeignKeys(tableName);
	if (baseKeys != null) {
	    for (ForeignKey foreignKey : baseKeys) {
		if (foreignKey.isValid() && !foreignKey.getReferToTable().equals(tableName)) {
		    importedKeys.add(foreignKey);
		}
	    }
	}
	if (targetKeys != null) {
	    for (ForeignKey foreignKey : targetKeys) {
		if (foreignKey.isValid() && !foreignKey.getReferToTable().equals(tableName) && !isDuplicateFK(foreignKey) && !isDuplicateFK(importedKeys, foreignKey)) {
		    importedKeys.add(foreignKey);
		}
	    }
	}
	return importedKeys;
    }

    /**
     * Returns true if FK is already in the given list
     * 
     * @param importedKeys
     * @param foreignKey
     * @return
     */
    public boolean isDuplicateFK(List<ForeignKey> importedKeys, ForeignKey foreignKey) {
	for (ForeignKey alreadyInList : importedKeys) {
	    if (alreadyInList.isSameDefinition(foreignKey)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns true if found within the current primary list
     * 
     * @param targetKey
     * @return
     */
    public boolean isDuplicateFK(ForeignKey targetKey) {
	if (findIdentical(targetKey) != null) {
	    return true;
	}
	return false;
    }

    /**
     * Finds the identical one from the current primary list
     * 
     * @param targetKey
     * @return
     */
    public ForeignKey findIdentical(ForeignKey targetKey) {
	List<ForeignKey> baseForeignKeys = primaryMap.getForeignKeys(targetKey.getReferByTable());
	if (baseForeignKeys != null) {
	    for (ForeignKey baseKey : baseForeignKeys) {
		if (baseKey.isSameDefinition(targetKey)) {
		    return baseKey;
		}
	    }
	}
	return null;
    }

    /**
     * Checks data integrity for the whole schema using the available
     * relationships
     * 
     * @param con
     * @return
     */
    public Map<String, String> checkDataIntegrity(Connection con) {
	Map<String, String> statusMap = new TreeMap<String, String>();
	List<String> allTables = this.primaryMap.getAllTables();

	for (String table : allTables) {
	    statusMap.put(table, checkDataIntegrity(con, table));
	}
	return statusMap;
    }

    /**
     * Prints data integrity errors to output writer specified
     * 
     * @param con
     *            DB Connection
     * @param writer
     *            Output
     * @param tableName
     *            Table Name
     * @throws SQLException
     * @throws IOException
     */
    public void printDataIntegrityErrors(Connection con, BufferedWriter writer, String tableName) throws SQLException, IOException {
	HashSet<ForeignKey> trackkeys = new HashSet<ForeignKey>();
	List<ForeignKey> secondaryKeys = secondaryMap.getForeignKeys(tableName);
	if (secondaryKeys == null) {
	    return;
	}
	for (ForeignKey secondaryKey : secondaryKeys) {
	    List<String> referToCols = secondaryKey.getReferToCols();
	    List<String> referByCols = secondaryKey.getReferByCols();

	    if (shouldCheckReference(secondaryKey, referToCols, referByCols)) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		String existenceCheckSql = null;
		try {
		    existenceCheckSql = generateExistenceDataSql(tableName, secondaryKey, referToCols, referByCols);
		    pstmt = con.prepareStatement(existenceCheckSql);
		    result = pstmt.executeQuery();
		    while (result != null && result.next()) {
			if (trackkeys.add(secondaryKey)) {
			    writer.newLine();
			    writer.write("****** RELATIONSHIP VIOLATED WITH TABLE - " + secondaryKey.getReferToTable() + " " + secondaryKey.getReferByCols().toString() + " *******");
			    writer.newLine();
			}
			int colCount = result.getMetaData().getColumnCount();
			int pos = 1;
			while (pos <= colCount) {
			    String currVal = result.getString(pos);
			    writer.write(currVal == null ? "\"\"" : "\"" + currVal + "\"");
			    if (pos < colCount) {
				writer.write(",");
			    }
			    pos++;
			}
			writer.newLine();
		    }
		    writer.flush();
		    result.close();
		    pstmt.close();
		} catch (Exception e) {
		    printError(writer, e, existenceCheckSql);
		} finally {
		    writer.flush();
		    if (result != null)
			result.close();
		    if (pstmt != null)
			pstmt.close();
		}
	    }
	}
    }

    /**
     * Prints out if any unexpected error occurred during processing
     * 
     * @param writer
     * @param e
     * @param existenceCheckSql
     * @throws IOException
     */
    private void printError(BufferedWriter writer, Exception e, String existenceCheckSql) throws IOException {
	writer.write("Error occured, mostly due to columns missing or column mismatch. Check if DB table version matches OJB definition.");
	writer.newLine();
	writer.write("SQL executed is:- ");
	writer.newLine();
	writer.write(existenceCheckSql);
	writer.newLine();
	writer.write("------------Stack Trace upto 100 lines-------");
	writer.newLine();
	writer.write(e.getMessage());
	writer.newLine();
	StackTraceElement[] stackTrace = e.getStackTrace();
	for (int i = 0; i < 100 && i < stackTrace.length; i++) {
	    writer.write(stackTrace[i].toString());
	    writer.newLine();
	}
    }

    /**
     * Returns true if reference should be checked for this relationship
     * 
     * @param fk
     * @param referToCols
     * @param referByCols
     * @return
     */
    private boolean shouldCheckReference(ForeignKey fk, List<String> referToCols, List<String> referByCols) {
	return !fk.isCascadeUpdate() && !isDuplicateFK(fk) && !fk.isReferToView() && referToCols != null && !referToCols.isEmpty() && referByCols != null && !referByCols.isEmpty()
		&& referByCols.size() == referToCols.size();
    }

    /**
     * Checks data integrity for a specified table name
     * 
     * @param con
     * @param tableName
     * @return
     */
    public String checkDataIntegrity(Connection con, String tableName) {
	try {
	    List<ForeignKey> secondaryKeys = this.secondaryMap.getForeignKeys(tableName);
	    if (secondaryKeys == null) {
		return "PASS";
	    }
	    for (ForeignKey secondaryKey : secondaryKeys) {
		List<String> referToCols = secondaryKey.getReferToCols();
		List<String> referByCols = secondaryKey.getReferByCols();
		if (shouldCheckReference(secondaryKey, referToCols, referByCols)) {
		    String existenceCheckSql = generateExistenceCountSql(tableName, secondaryKey, referToCols, referByCols);
		    PreparedStatement pstmt = con.prepareStatement(existenceCheckSql);
		    ResultSet result = pstmt.executeQuery();
		    if (result != null && result.next()) {
			if (result.getInt(1) > 0) {
			    result.close();
			    pstmt.close();
			    return "FAIL";
			}
		    }
		    result.close();
		    pstmt.close();
		}
	    }
	} catch (Exception e) {
	    return "FAIL";
	}
	return "PASS";
    }

    /**
     * Generates SQL which can identify violating data relationships
     * 
     * @param tableName
     * @param secondaryKey
     * @param referToCols
     * @param referByCols
     * @return
     */
    private String generateExistenceCountSql(String tableName, ForeignKey secondaryKey, List<String> referToCols, List<String> referByCols) {
	StringBuilder sqlBuf = new StringBuilder();
	sqlBuf.append("select count(1) from ");
	sqlBuf.append(tableName);
	sqlBuf.append(" A where ");
	for (int i = 0; i < referToCols.size() && i < referByCols.size(); i++) {
	    sqlBuf.append("A.");
	    sqlBuf.append(referByCols.get(i));
	    sqlBuf.append(" is not null and ");
	    sqlBuf.append("length(rtrim(ltrim(A.");
	    sqlBuf.append(referByCols.get(i));
	    sqlBuf.append("))) > 0 and ");
	}
	sqlBuf.append(" not exists (select 1 from ");
	sqlBuf.append(secondaryKey.getReferToTable());
	sqlBuf.append(" B where ");
	for (int i = 0; i < referToCols.size() && i < referByCols.size(); i++) {
	    sqlBuf.append("B.");
	    sqlBuf.append(referToCols.get(i));
	    sqlBuf.append("= A.");
	    sqlBuf.append(referByCols.get(i));
	    if (i < referToCols.size() - 1) {
		sqlBuf.append(" and ");
	    }
	}
	sqlBuf.append(")");
	return sqlBuf.toString();
    }

    /**
     * Generates SQL to list the violating records
     * 
     * @param tableName
     * @param secondaryKey
     * @param referToCols
     * @param referByCols
     * @return
     */
    private String generateExistenceDataSql(String tableName, ForeignKey secondaryKey, List<String> referToCols, List<String> referByCols) {
	StringBuilder sqlBuf = new StringBuilder();
	sqlBuf.append("select * from ");
	sqlBuf.append(tableName);
	sqlBuf.append(" A where ");
	for (int i = 0; i < referToCols.size() && i < referByCols.size(); i++) {
	    sqlBuf.append("A.");
	    sqlBuf.append(referByCols.get(i));
	    sqlBuf.append(" is not null and ");
	    sqlBuf.append("length(rtrim(ltrim(A.");
	    sqlBuf.append(referByCols.get(i));
	    sqlBuf.append("))) > 0 and ");
	}
	sqlBuf.append(" not exists (select 1 from ");
	sqlBuf.append(secondaryKey.getReferToTable());
	sqlBuf.append(" B where ");
	for (int i = 0; i < referToCols.size() && i < referByCols.size(); i++) {
	    sqlBuf.append("B.");
	    sqlBuf.append(referToCols.get(i));
	    sqlBuf.append("= A.");
	    sqlBuf.append(referByCols.get(i));
	    if (i < referToCols.size() - 1) {
		sqlBuf.append(" and ");
	    }
	}
	sqlBuf.append(")");
	return sqlBuf.toString();
    }

}
