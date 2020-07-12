package com.lndb.dwtool.erm.util;

public final class JavaSourceUtil {
    public static String buildJavaFieldName(String dbField, String delimiter) {
	if (dbField == null) {
	    return "";
	}
	String val = "";
	String[] split = dbField.trim().split(delimiter);
	int pos = 0;
	for (String string : split) {
	    if (pos == 0) {
		val = string.toLowerCase();
	    } else {
		if (string.length() > 1) {
		    val = val + Character.toTitleCase(string.charAt(0)) + string.substring(1).toLowerCase();
		} else {
		    val = val + string.toUpperCase();
		}
	    }
	    pos++;
	}
	return val;
    }

    public static String buildJavaClassName(String tableName, String delimiter) {
	if (tableName == null) {
	    return "";
	}
	String val = "";
	String name = tableName.trim().toUpperCase();
	if (name.endsWith(delimiter + "T")) {
	    name = name.replace(delimiter + "T", "");
	}
	String[] split = name.split(delimiter);
	int pos = 0;
	for (String string : split) {
	    // ignores the module prefix
	    if (pos > 0) {
		if (string.length() > 1) {
		    val = val + Character.toTitleCase(string.charAt(0)) + string.substring(1).toLowerCase();
		} else {
		    val = val + string.toUpperCase();
		}
	    }
	    pos++;
	}
	return val;
    }
}
