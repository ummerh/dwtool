package com.lndb.dwtool.erm;

import static com.lndb.dwtool.erm.util.StringUtil.rpad;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class RelationalMap {
    protected Set<String> tableSet = new HashSet<String>();
    protected List<String> orderedTables = new ArrayList<String>();
    protected boolean loaded;

    public abstract List<String> getAllTables();

    public abstract List<ForeignKey> getExportedKeys(String tableName);

    public abstract List<ForeignKey> getForeignKeys(String tableName);

    protected List<ForeignKey> getNotSelfForeignKeys(String tableName) {
	List<ForeignKey> notSelfRefs = new ArrayList<ForeignKey>();
	List<ForeignKey> fkInfo = getForeignKeys(tableName);

	for (ForeignKey foreignKey : fkInfo) {
	    if (!tableName.equalsIgnoreCase(foreignKey.getReferToTable())) {
		notSelfRefs.add(foreignKey);
	    }
	}
	fkInfo.clear();
	fkInfo = null;
	return notSelfRefs;
    }

    protected List<ForeignKey> getSelfReferences(String tableName) {
	List<ForeignKey> selfRefs = new ArrayList<ForeignKey>();
	List<ForeignKey> fkInfo = getForeignKeys(tableName);

	for (ForeignKey foreignKey : fkInfo) {
	    if (tableName.equalsIgnoreCase(foreignKey.getReferToTable())) {
		selfRefs.add(foreignKey);
	    }
	}
	fkInfo.clear();
	fkInfo = null;
	return selfRefs;
    }

    protected void printFKRelations(BufferedWriter writer, String table) throws IOException {
	writer.write("FOREIGN KEYS");
	writer.newLine();
	List<ForeignKey> fkCols = getNotSelfForeignKeys(table);
	for (ForeignKey fkKey : fkCols) {
	    writer.write(rpad("{" + fkKey.getName() + "}", 30) + rpad((fkKey.getReferByCols() == null ? "" : fkKey.getReferByCols().toString()), 50) + fkKey.getReferToTable() + " "
		    + (fkKey.getReferToCols() == null ? "" : fkKey.getReferToCols().toString()));
	    writer.newLine();
	}
    }

    protected void printInverseReferences(BufferedWriter writer, String table) throws IOException {
	writer.write("INVERSE RELATIONSHIPS");
	writer.newLine();
	List<ForeignKey> inverseCols = getExportedKeys(table);
	for (ForeignKey fkKey : inverseCols) {
	    writer.write(rpad("{" + fkKey.getName() + "}", 30) + fkKey.getReferByTable() + " " + (fkKey.getReferByCols() == null ? "" : fkKey.getReferByCols().toString()));
	    writer.newLine();
	}
    }

    protected void printSelfReferences(BufferedWriter writer, String table) throws IOException {
	writer.write("SELF REFERENCES");
	writer.newLine();
	List<ForeignKey> selfRefCols = getSelfReferences(table);
	for (ForeignKey fkKey : selfRefCols) {
	    writer.write(rpad("{" + fkKey.getName() + "}", 30) + fkKey.getReferToTable() + " " + (fkKey.getReferToCols() == null ? "" : fkKey.getReferToCols().toString()));
	    writer.newLine();
	}
    }

    public List<String> defineOrder() {
	orderedTables.clear();
	this.tableSet.clear();
	if (!this.loaded) {
	    throw new RuntimeException("DB Map not loaded");
	}
	List<String> allTables = this.getAllTables();
	for (String table : allTables) {
	    orderTable(table);
	}
	return orderedTables;
    }

    public List<String> defineOrder(String tableName) {
	orderedTables.clear();
	this.tableSet.clear();
	if (!this.loaded) {
	    throw new RuntimeException("DB Map not loaded");
	}
	orderTable(tableName.trim().toUpperCase());

	return orderedTables;
    }

    private void orderTable(String table) {
	if (!tableSet.contains(table)) {
	    tableSet.add(table);
	    List<ForeignKey> foreignKeys = this.getForeignKeys(table);
	    iterateForeignKeys(table, foreignKeys);
	    orderedTables.add(table);
	}
    }

    private void iterateForeignKeys(String table, List<ForeignKey> foreignKeys) {
	if (foreignKeys == null || foreignKeys.isEmpty()) {
	    return;
	}
	for (ForeignKey foreignKey : foreignKeys) {
	    if (foreignKey.isValid() && !foreignKey.getReferToTable().equalsIgnoreCase(table)) {
		orderTable(foreignKey.getReferToTable());
	    }
	}
    }

    public boolean isLoaded() {
	return loaded;
    }

    public void setLoaded(boolean loaded) {
	this.loaded = loaded;
    }

}
