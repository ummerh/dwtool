package com.lndb.dwtool.erm.dml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;

public class ConverterUtil {

	public static BufferedWriter getOutputFileStream(String outputDir,
			String tableName) throws IOException {
		String fileName = getDecisionSqlFileName(outputDir, tableName);
		BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
		output.write("-- Converting for " + tableName + "...");
		output.newLine();
		return output;
	}

	public static String getDecisionSqlFileName(String outputDir,
			String tableName) {
		String fileName = outputDir + tableName + ".sql";
		return fileName;
	}
	
	public static BufferedWriter getReportOutputFileStream(String outputDir, String uploadFilename) throws IOException {
		String fileName = getReportFileName(outputDir, uploadFilename);
		BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
		output.write("Summary report for loading Excel '" + uploadFilename + "'");
		output.newLine();
		output.write("----------------------------------------------------------");
		output.newLine();
		return output;
	}
	
	public static BufferedWriter getErrorOutputFileStream(String outputDir, String uploadFilename) throws IOException {
		String fileName = getErrorFileName(outputDir, uploadFilename);
		BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
		output.write("Error report for loading Excel '" + uploadFilename + "'");
		output.newLine();
		output.write("----------------------------------------------------------");
		output.newLine();
		return output;
	}

	public static String getReportFileName(String outputDir,
			String uploadFilename) {
		String fileName = outputDir + StringUtils.substringBefore(uploadFilename, ".") + "_Report.txt";
		return fileName;
	}
	
	public static String getErrorFileName(String outputDir,
			String uploadFilename) {
		String fileName = outputDir + StringUtils.substringBefore(uploadFilename, ".") + "_Error.txt";
		return fileName;
	}
	

	public static List<ColumnDescriptor> getSortedColumns(
			TableDescriptor tableDesc) {
		List<ColumnDescriptor> sortedColumns = new ArrayList<ColumnDescriptor>();
		// primary key first
		List primaryKeys = tableDesc.getPrimaryKeys();
		for (ColumnDescriptor col :  tableDesc.getColumns()) {
			if (primaryKeys.contains(col.getName())) {
				sortedColumns.add(col);
			}
		}
		
		// OBJ_ID
		sortedColumns.add(tableDesc.getColumn("OBJ_ID"));
		// VER_NBR
		sortedColumns.add(tableDesc.getColumn("VER_NBR"));
		
		// OTHERS
		for (ColumnDescriptor col :  tableDesc.getColumns()) {
			if (!sortedColumns.contains(col)) {
				sortedColumns.add(col);
			}
		}
		return sortedColumns;
	}
	
	public static String escapeSpecialChar(String colValue) {
		if (StringUtils.isBlank(colValue)) {
			return null;
		}
		String newValue = colValue.replaceAll("(')", "'$1");
		
		Pattern pattern = Pattern.compile("&");
		Matcher matcher = pattern.matcher(newValue);
		StringBuilder resultString = new StringBuilder();
		int start = 0;
		while (matcher.find()) {
		  	resultString.append(newValue.substring(start, matcher.start()));
		  	resultString.append("'|| chr(38) ||'" + matcher.group().substring(1));
		  	start = matcher.end();
		}
		
		if (resultString.length() == 0) {
			// does not contain &
			resultString.append(newValue);
		}
		else {
			resultString.append(newValue.substring(start));
		}
		return resultString.toString();
		
	}
	

	public static boolean doesDataTypeRequireSingleQuote(Integer colType, String colName, String colValue) {
		
		boolean result = colType != null && (Types.VARBINARY == colType || Types.CHAR == colType || Types.VARCHAR == colType) &&  !OracleSqlConstant.OBJ_ID.equalsIgnoreCase(colName) && !OracleSqlConstant.VER_NBR.equalsIgnoreCase(colName);
		
		if (result && StringUtils.isNotBlank(colValue)) {
			result = !(colValue.contains("SELECT ") && colValue.contains(" FROM ") && colValue.contains(" WHERE "));
		}
		
		return result;
	}
}
