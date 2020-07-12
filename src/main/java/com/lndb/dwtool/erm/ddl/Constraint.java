package com.lndb.dwtool.erm.ddl;

public class Constraint {
    private String name;
    private String table;
    private static int counter;

    public Constraint(String name, String table) {
	super();
	this.name = name;
	this.table = table;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getTable() {
	return table;
    }

    public void setTable(String table) {
	this.table = table;
    }

    public static String buildConstraintName(String prefix, String tblName, String type, boolean suffixCount) {
	String tblNameVal = tblName.length() > 20 ? tblName.substring(0, 20) : tblName;
	String constraintName = ((prefix == null || prefix.length() == 0) ? "" : (prefix + "_")) + (tblNameVal) + (tblNameVal.endsWith("_") ? "" : "_") + type;
	return constraintName + (suffixCount ? "_" + (++counter) : "");
    }

    public static String buildConstraintName(String prefix, String tblName, String type, Integer count) {
	String tblNameVal = tblName.length() > 20 ? tblName.substring(0, 20) : tblName;
	String constraintName = ((prefix == null || prefix.length() == 0) ? "" : (prefix + "_")) + (tblNameVal) + (tblNameVal.endsWith("_") ? "" : "_") + type + "_" + count.intValue();
	return constraintName;
    }

}
