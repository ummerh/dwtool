package com.lndb.dwtool.erm.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    private static final char QUOTE = '\"';
    private static final char WHITESPACE = ' ';
    public static final String TAB = "\r\t";
    public static final String LINE_BREAK = System.getProperty("line.separator");

    public static String rpad(String val, int padSize) {
	String newVal = val;
	if (newVal != null) {
	    int valLen = newVal.length();
	    if (valLen < padSize) {
		int diff = padSize - valLen;
		while (diff > 0) {
		    newVal += " ";
		    diff--;
		}
	    }
	}
	return newVal;
    }

    public static String[] parseQuoted(char delimiter, String data) {
	if (data == null)
	    return null;
	char[] dataChars = data.toCharArray();
	boolean withinQuote = false;
	boolean lastTokenChar = false;
	int startIndex = -1;
	int currIndex = 0;
	// initialize with a reasonable size
	List<String> tokens = new ArrayList<String>(1 + data.lastIndexOf(delimiter));
	for (char c : dataChars) {
	    if (c == delimiter) {
		if (!withinQuote || lastTokenChar) {
		    if (currIndex - startIndex > 1) {
			tokens.add(new String(dataChars, startIndex + 1, (currIndex - startIndex - 1)).replace('\"', ' ').trim());
		    } else {
			tokens.add("");
		    }
		    startIndex = currIndex;
		}
		if (currIndex == dataChars.length - 1) {
		    tokens.add("");
		}
		if (!withinQuote) {
		    lastTokenChar = true;
		}
	    } else if (c == WHITESPACE && lastTokenChar && currIndex == dataChars.length - 1) {
		tokens.add("");
	    } else {
		lastTokenChar = false;
	    }
	    if (c == QUOTE) {
		if (!withinQuote) {
		    startIndex = currIndex;
		}
		withinQuote = !withinQuote;
	    }
	    if (c != delimiter && !withinQuote && currIndex == data.length() - 1) {
		tokens.add(new String(dataChars, startIndex + 1, (currIndex - startIndex)).replace('\"', ' ').trim());
	    }
	    currIndex++;
	}
	String[] returnValues = new String[] {};
	return tokens.toArray(returnValues);
    }

    /**
     * If the value contains only the filler characters, then return blank
     * 
     * @param val
     *            Value
     * @return blank if value if a filler
     */
    public static String replaceFiller(String val) {
	if (val == null) {
	    return "";
	}
	char[] charArray = val.trim().toCharArray();
	for (char c : charArray) {
	    if (c != '-') {
		return val;
	    }
	}
	return "";
    }
}
