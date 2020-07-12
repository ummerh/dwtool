package com.lndb.dwtool.erm.ddl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.struts2.ServletActionContext;

import com.lndb.dwtool.code.diff.FileUtil;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.db.PrimaryKey;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.db.TableRowObject;
import com.lndb.dwtool.erm.db.TableRowObjectDiff;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.util.Configuration;



public class DataDiffGenerator {
	protected DBMap baseDb;

	protected DBMap targetDb;

	protected OJBMap ojbMap;

	protected List<String> allTables = new ArrayList<String>();
	
	protected List<String> refTables;
	
	protected List<String> excludedTables = new ArrayList<String>();
	
	protected boolean initialized;

	protected List<TableRowObject> tablesInTargetNotSource = new ArrayList<TableRowObject>();

	protected List<TableRowObject> tablesInSourceNotTarget = new ArrayList<TableRowObject>();

	protected List<TableRowObjectDiff> mismatchedObjects = new ArrayList<TableRowObjectDiff>();
	
	protected Map<String, List<TableRowObject>> dataInTargetNotSourceMap = new HashMap<String, List<TableRowObject>> ();
	
	protected Map<String, List<TableRowObject>> dataInSourceNotTargetMap = new HashMap<String, List<TableRowObject>> ();
	
	protected Map<String, List<TableRowObjectDiff>> dataMismatchedMap = new HashMap<String, List<TableRowObjectDiff>> ();
	
	public static final Pattern DATA_COMPARE_EXCLUDE_PATTERN = Pattern.compile(Configuration.getProperty("data.compare.exclude.pattern"));
	public static final Pattern EXCLUDE_TBL_FROM_DATA_COMPARE_PATTERN = Pattern.compile(Configuration.getProperty("data.compare.exclude.table.pattern"));
	
	private static final String ANALYSIS_SOURCE = "LNDB Only";
	private static final String ANALYSIS_TARGET = "Foundation Only";
	private static final String ANALYSIS_CHANGED = "DIFF";
    private static final String EMPTY_STRING = "";
    private static final String UNDERSCORE = "_";
    
	public void init(DBMap baseDb, DBMap targetDb, OJBMap ojbMap) {
		this.baseDb = baseDb;
		this.targetDb = targetDb;
		this.ojbMap = ojbMap;
		
		try {
			//Getting the list of allTables from the file containing the bootstrap list.
		    BufferedReader reader = new BufferedReader(new FileReader(Configuration.getProperty("tables.input.for.data.compare")));
		    String content = reader.readLine();
		    while (content != null) {
			    String[] tableNames = content.split(",");
			    for (int i=0; i < tableNames.length; i++) {
			    	String cleanTableName = tableNames[i].replace("'", "");
			    	allTables.add(cleanTableName);
			    }		    	
                content = reader.readLine();
		    }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	    this.refTables = getReferenceTables(ojbMap, baseDb, allTables);
	    
		this.initialized = true;
	}

	public void compareData(OutputStream os) throws Exception {
		Connection conSource =  null;
	    Connection conTarget = null;
    	Statement stmtSource = null;
    	Statement stmtTarget = null;
    	PrintWriter writer = new PrintWriter(os);
	    try {
  	        conSource = DatabaseConnection.newConnection(baseDb.getConnectionDetail());
	        conTarget = DatabaseConnection.newConnection(targetDb.getConnectionDetail());
	        stmtSource = conSource.createStatement();
	        stmtTarget = conTarget.createStatement();
	        //Go through a list of the reference tables and compare the tables between the source and
	        //the target database
	        for (String refTbl : refTables) {
	        	TableDescriptor baseTbl = baseDb.getTableDescriptor(refTbl);
	        	if (baseTbl != null) {
		            List<String> primaryKeys = baseTbl.getPrimaryKeys();
		            TableDescriptor tgtTbl = targetDb.getTableDescriptor(refTbl);
		            if (tgtTbl != null) {
		        	    //We can only compare if the table also existing in the target db, otherwise we'll get some exception
		        	    compareTableRowObjects(os, refTbl, primaryKeys, stmtSource, stmtTarget);
		            }
	        	}
	        }

	        //New Format where each module has its own workbook
	        writeDiffPerModuleReport(os);
	    }
	    
	    finally {
    		DatabaseConnection.release(null, stmtSource, conSource);
    		DatabaseConnection.release(null, stmtTarget, conTarget);
    		writer.flush();
    		writer.close();
    	}
	}

    private List<String> getReferenceTables(OJBMap ojbMap, DBMap source, List<String> allTables) {
  	    HashSet<String> excludes = new HashSet<String>();
    	List<String> refTables = new ArrayList<String>();
	    for (String td : allTables) {
	    	if (!EXCLUDE_TBL_FROM_DATA_COMPARE_PATTERN.matcher(td).matches()) {
		        refTables.add(td);
	    	}
	    	else {
	    		this.excludedTables.add(td);
	    	}
	    }
	    Collections.sort(refTables);
    	return refTables;
    }
    
    private void compareTableRowObjects(OutputStream os, String tableName, List<String>primaryKeysString, Statement stmtSource, Statement stmtTarget) throws Exception {

    	ResultSet rsSource = null;
    	ResultSet rsTarget = null;
    	String query = "SELECT * FROM " + tableName;
    	
	    try {
	        rsSource = stmtSource.executeQuery(query);
	        Map<String, TableRowObject> sourceObjects = createTableRowObjectMapFromResultSet(os, rsSource, tableName, primaryKeysString );
	        
	        rsTarget = stmtTarget.executeQuery(query);
	        Map<String, TableRowObject> targetObjects = createTableRowObjectMapFromResultSet(os, rsTarget, tableName, primaryKeysString );
	        
	        //Find Differences
	        findDifferences(os, sourceObjects, targetObjects);
    	} 
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	    finally {
    		rsSource.close();
    		rsTarget.close();
    	}
    }
    
    private Map<String, TableRowObject> createTableRowObjectMapFromResultSet(OutputStream os, ResultSet rs, String tableName, List<String>primaryKeysString ) throws SQLException {
        Map<String, TableRowObject> resultObjects = new HashMap<String,TableRowObject>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        while (rs.next()) {
        	TableRowObject resultObject = new TableRowObject(tableName);
        	PrimaryKey primaryKey = new PrimaryKey(tableName);
        	resultObject.setPrimaryKey(primaryKey);
    	    for (int columnIndex=1; columnIndex <= numberOfColumns; columnIndex++) {
    		    String colName = rsmd.getColumnLabel(columnIndex);
    	        Object colValue = rs.getObject(columnIndex);
    	        if (primaryKeysString.contains(colName)) {
    	        	//This is a primary key column, so let's store it in the PrimaryKey object for this table.
    	        	if (!primaryKey.getKeyValues().containsKey(colName)) {
    	        		primaryKey.addPrimaryKeyValues(colName, colValue);	    	        		
    	        	}
    	        }
    	        resultObject.addAttribute(colName, colValue);
    	    }
    	    resultObjects.put(primaryKey.toString(), resultObject);
        }
        return resultObjects;
    }

    private void findDifferences(OutputStream os, Map<String, TableRowObject> sourceObjects, Map<String, TableRowObject> targetObjects) {
    	//For each of the rows in the source table, find the corresponding row (if exists) in the target table.
        for (String sourcePrimaryKey : sourceObjects.keySet()) {	        	
        	if (!targetObjects.containsKey(sourcePrimaryKey)) {
        	    //The case when this object is missing entirely from the target database table.
        		TableRowObject tro = (TableRowObject) sourceObjects.get(sourcePrimaryKey);
        		this.tablesInSourceNotTarget.add(sourceObjects.get(sourcePrimaryKey));
        		
        		// Now add this to the dataInSourceNotTargetMap that we can use to print
        		// a friendly report to the excel spreadsheet
        		if (this.dataInSourceNotTargetMap.containsKey(tro.getTableName())) {
        		    this.dataInSourceNotTargetMap.get(tro.getTableName()).add(tro);
        		}
        		else {
        			List<TableRowObject> troList = new ArrayList<TableRowObject>();
        			troList.add(tro);
        			this.dataInSourceNotTargetMap.put(tro.getTableName(), troList);
        		}
        	}
        	else {
        		TableRowObject sourceObject = (TableRowObject)sourceObjects.get(sourcePrimaryKey);
        		TableRowObject targetObject = (TableRowObject)targetObjects.get(sourcePrimaryKey);
        		if (!sourceObject.equals(targetObject)) {
                    // The case when the source object and target object are different
        			TableRowObjectDiff troDiff = new TableRowObjectDiff(sourceObject, targetObject, sourcePrimaryKey);
        			this.mismatchedObjects.add(troDiff);
        			
        			// Now add this to the dataMismatchedMap that we can use to print
            		// a friendly report to the excel spreadsheet
        			if (this.dataMismatchedMap.containsKey(troDiff.getTableName())) {
        				this.dataMismatchedMap.get(troDiff.getTableName()).add(troDiff);
        			}
        			else {
        				List<TableRowObjectDiff> troDiffList = new ArrayList<TableRowObjectDiff>();
        				troDiffList.add(troDiff);
        				this.dataMismatchedMap.put(troDiff.getTableName(), troDiffList);
        			}
        		}
        	}
        }
        //For each of the rows in the target table, find the corresponding row (if exists) in the source table.
        for (String targetPrimaryKey : targetObjects.keySet()) {
       		if (!sourceObjects.containsKey(targetPrimaryKey)) {
       			//The case when this object is missing entirely from the source database table.
       			TableRowObject tro = (TableRowObject) targetObjects.get(targetPrimaryKey);
	       		this.tablesInTargetNotSource.add(targetObjects.get(targetPrimaryKey));
	       		
        		// Now add this to the dataInSourceNotTargetMap that we can use to print
        		// a friendly report to the excel spreadsheet
        		if (this.dataInTargetNotSourceMap.containsKey(tro.getTableName())) {
        		    this.dataInTargetNotSourceMap.get(tro.getTableName()).add(tro);
        		}
        		else {
        			List<TableRowObject> troList = new ArrayList<TableRowObject>();
        			troList.add(tro);
        			this.dataInTargetNotSourceMap.put(tro.getTableName(), troList);
        		}
       		}
       	}

    }

	private int printHeader(String refTbl, HSSFSheet sheet, TableRowObject firstTro, HSSFFont headerFont, HSSFCellStyle headerStyle1, HSSFCellStyle headerStyle2) {
		int rowCount = 0;
		TableDescriptor tableDesc = baseDb.getTableDescriptor(firstTro.getTableName());

		Row newRow = sheet.createRow(rowCount);
	    rowCount++;
	    int headerIndex = 0;
	    //Print the columns in the same order as the database 
	    for (ColumnDescriptor colDesc : tableDesc.getColumns()) {
	    	if (!DATA_COMPARE_EXCLUDE_PATTERN.matcher(colDesc.getName()).matches()) {
	    		//We only need to print the cell if it's not an excluded column
	    	    Cell headerCell = newRow.createCell(headerIndex);
	    	    headerCell.setCellStyle(headerStyle1);
	    	    HSSFRichTextString colHeader = new HSSFRichTextString(colDesc.getName());
	    	    colHeader.applyFont(headerFont);
	    	    headerCell.setCellValue(colHeader);
	    	    headerIndex++;
	    	}
	    }
    	Cell analysisCell = newRow.createCell(headerIndex);
		analysisCell.setCellStyle(headerStyle2);
		HSSFRichTextString analysisHeader = new HSSFRichTextString("Auto-Classify");
		analysisHeader.applyFont(headerFont);
    	analysisCell.setCellValue(analysisHeader);
    	headerIndex++;
    	//Print the columns in the same order as the database 
	    for (ColumnDescriptor colDesc : tableDesc.getColumns()) {
	    	if (!DATA_COMPARE_EXCLUDE_PATTERN.matcher(colDesc.getName()).matches()) {
	    		//We only need to print the cell if it's not an excluded column
	    	    Cell headerCell = newRow.createCell(headerIndex);
	    	    headerCell.setCellStyle(headerStyle1);
	    	    HSSFRichTextString colHeader = new HSSFRichTextString(colDesc.getName());
	    	    colHeader.applyFont(headerFont);
	    	    headerCell.setCellValue(colHeader);
	    	    headerIndex++;
	    	}
	    }
	    Cell functionalCell = newRow.createCell(headerIndex);
		functionalCell.setCellStyle(headerStyle2);
		HSSFRichTextString functionalHeader = new HSSFRichTextString("Functional Decision");
		functionalHeader.applyFont(headerFont);
	    functionalCell.setCellValue(functionalHeader);
	    headerIndex++;
	    Cell technicalCell = newRow.createCell(headerIndex);
		technicalCell.setCellStyle(headerStyle2);
		HSSFRichTextString technicalHeader = new HSSFRichTextString("Technical Decision");
		technicalHeader.applyFont(headerFont);
	    technicalCell.setCellValue(technicalHeader);
	    headerIndex++;
	    return rowCount;
	}
	
	private int printContent(HSSFSheet sheet, List<TableRowObject> troList, int rowCount, String analysis, HSSFCellStyle contentStyle, HSSFCellStyle analysisStyle, HSSFCellStyle emptyStyle) {
		boolean hasSetColumnWidth = false;
		for (TableRowObject tro : troList) {
    		TableDescriptor tableDesc = baseDb.getTableDescriptor(tro.getTableName());
    		Row newRowData = sheet.createRow(rowCount);
    		rowCount++;
    		int cellIndex = 0;
    		//Have to print out each attributes value on the sheet
		    //Print the columns in the same order as the database 
		    for (ColumnDescriptor colDesc : tableDesc.getColumns()) {
		    	if (!hasSetColumnWidth) {
		    		int size = colDesc.getSize();
		    		if (size <=2 ) {
		    			//If size <= 2, setting it to 2 * 256 will be too small,
		    			//so I'm setting it to 3 * 256.
		    			sheet.setColumnWidth(cellIndex, 3 * 256);
		    		}		    		
		    		else if (size < 20) {
		    			//If size <20, set the column width to the actual size.
		    			sheet.setColumnWidth(cellIndex, size * 256);
		    		}
		    		else if (size < 50) {
		    			//If 20 <= size < 50, set the column width to 20.
		    			sheet.setColumnWidth(cellIndex, 20 * 256);
		    		}
		    		else {
		    			//If size >= 50, set the column width to 50.
		    			sheet.setColumnWidth(cellIndex, 50 * 256);
		    		}
		    	}
			    if (!DATA_COMPARE_EXCLUDE_PATTERN.matcher(colDesc.getName()).matches()) {
				    Object value = tro.getAttributes().get(colDesc.getName());
    			    Cell newCell = newRowData.createCell(cellIndex);
    			    if (contentStyle != null) {
    			    	newCell.setCellStyle(contentStyle);
    			    }
    			    newCell.setCellValue(new HSSFRichTextString(value != null ? value.toString() : EMPTY_STRING));
    			    cellIndex++;				    				
			    }
		    }
    		Cell analysisCell = newRowData.createCell(cellIndex);
    		analysisCell.setCellStyle(analysisStyle);
    		analysisCell.setCellValue(new HSSFRichTextString(analysis));
    		cellIndex++;
    		//Now we have to print the border for those empty columns on the right side of "Auto-Analysis".
    		for (ColumnDescriptor colDesc : tableDesc.getColumns()) {
    			Cell newCell = newRowData.createCell(cellIndex);
    			newCell.setCellStyle(emptyStyle);
    			cellIndex++;
    		}
    		hasSetColumnWidth = true;
    	}

    	return rowCount;
	}
	
	private int printContentForSourceTargetDiff(HSSFSheet sheet, List<TableRowObjectDiff> trodList, int rowCount, String analysis, HSSFCellStyle contentStyleGreen, HSSFCellStyle contentStyleOrange, HSSFCellStyle analysisStyle, HSSFCellStyle diffStyle, HSSFCellStyle emptyStyle) {
		boolean hasSetColumnWidth = false;
		for (TableRowObjectDiff trod : trodList) {
    		TableRowObject sourceTro = trod.getSourceTableRowObject();
    		TableRowObject targetTro = trod.getTargetTableRowObject();
    		Row newRowData = sheet.createRow(rowCount);
    		rowCount++;
    		//Have to print out each attributes value on the sheet
    		int cellIndex = 0;
    		//Print the columns in the same order as the database 
    		TableDescriptor tableDesc = baseDb.getTableDescriptor(sourceTro.getTableName());
    		
    		for (ColumnDescriptor colDesc : tableDesc.getColumns()) {
		    	if (!hasSetColumnWidth) {
		    		int size = colDesc.getSize();
                    if (size <=2 ) {
		    			//If size <= 2, setting it to 2 * 256 will be too small,
		    			//so I'm setting it to 3 * 256.
		    			sheet.setColumnWidth(cellIndex, 3 * 256);
		    		}
		    		else if (size < 20) {
		    			//If 2 < size <20, set the column width to the actual size.
		    			sheet.setColumnWidth(cellIndex, size * 256);
		    		}
		    		else if (size < 50) {
		    			//If 20 <= size < 50, set the column width to 20.
		    			sheet.setColumnWidth(cellIndex, 20 * 256);
		    		}
		    		else {
		    			//If size >= 50, set the column width to 50.
		    			sheet.setColumnWidth(cellIndex, 50 * 256);
		    		}
		    	}

    			if (!DATA_COMPARE_EXCLUDE_PATTERN.matcher(colDesc.getName()).matches()) {
    				Object value = sourceTro.getAttributes().get(colDesc.getName());
	    			Cell newCell = newRowData.createCell(cellIndex);
	    		    newCell.setCellStyle(contentStyleGreen);
	    			newCell.setCellValue(new HSSFRichTextString(value != null ? value.toString() : EMPTY_STRING));
	    			cellIndex++;				    				
    			}
    		}
    		Cell analysisRowCell = newRowData.createCell(cellIndex);
    		analysisRowCell.setCellStyle(analysisStyle);
    		analysisRowCell.setCellValue(new HSSFRichTextString(analysis));
    		cellIndex++;
    		for (ColumnDescriptor colDesc : tableDesc.getColumns()) {
		    	if (!hasSetColumnWidth) {
		    		int size = colDesc.getSize();
		    		if (size <=2 ) {
		    			//If size <= 2, setting it to 2 * 256 will be too small,
		    			//so I'm setting it to 3 * 256.
		    			sheet.setColumnWidth(cellIndex, 3 * 256);
		    		}
		    		else if (size < 20) {
		    			//If 2 < size <20, set the column width to the actual size.
		    			sheet.setColumnWidth(cellIndex, size * 256);
		    		}
		    		else if (size < 50) {
		    			//If 20 <= size < 50, set the column width to 20.
		    			sheet.setColumnWidth(cellIndex, 20 * 256);
		    		}
		    		else {
		    			//If size >= 50, set the column width to 50.
		    			sheet.setColumnWidth(cellIndex, 50 * 256);
		    		}
		    	}
    			if (!DATA_COMPARE_EXCLUDE_PATTERN.matcher(colDesc.getName()).matches()) {
    				Object value = targetTro.getAttributes().get(colDesc.getName());
    				Object sourceValue = sourceTro.getAttributes().get(colDesc.getName());
	    			Cell newCell = newRowData.createCell(cellIndex);

    				if (value != null && sourceValue != null && !StringUtils.equals(value.toString().trim(), sourceValue.toString().trim())) {
    					//This is a column that is different between source and target.
    	    		    newCell.setCellStyle(diffStyle);
    				}
    				else if ((value == null && sourceValue != null) || (value != null && sourceValue == null)) {
    					newCell.setCellStyle(diffStyle);
    				}
    				else {
    					newCell.setCellStyle(contentStyleOrange);
    				}
    				HSSFRichTextString contentString = new HSSFRichTextString(value != null ? value.toString() : EMPTY_STRING);
    				newCell.setCellValue(contentString);
	    			cellIndex++;				    				
    			}
    		}
    		//Add 2 columns of empty cells as placeholders under "Functional Decision" and "Technical Decision" columns
    		//and set their borders.
    		for (int i=0; i < 2; i++) {
        		Cell newCell = newRowData.createCell(cellIndex);
        		newCell.setCellStyle(emptyStyle);
        		cellIndex++;
    		}
    		hasSetColumnWidth = true;
    	}
    	return rowCount;
	}
	
	public void writeDiffPerModuleReport(OutputStream os) {
		try {
			if (!initialized) {
				throw new RuntimeException("Data mapping not initialized....");
			}			
			
			Map<String, HSSFWorkbook> moduleWorkbookMap = new HashMap<String, HSSFWorkbook>();
			
			//For each refTable that has a difference, arrange by modules, 
			//each module has its own workbook, then print a sheet to show the differences.
			for (String refTbl : refTables) {
				if (this.dataInSourceNotTargetMap.containsKey(refTbl) ||
					this.dataInTargetNotSourceMap.containsKey(refTbl) ||
					this.dataMismatchedMap.containsKey(refTbl)) {
				
					String prefix = refTbl.substring(0, refTbl.indexOf(UNDERSCORE));
					if (moduleWorkbookMap.get(prefix) == null) {
						HSSFWorkbook report = new HSSFWorkbook();
						moduleWorkbookMap.put(prefix, report);
					}
					
					HSSFWorkbook report = moduleWorkbookMap.get(prefix);
					HSSFFont headerFont = report.createFont();
					headerFont.setColor(Font.COLOR_NORMAL);
					headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

					HSSFCellStyle headerStyle1 = report.createCellStyle();
					headerStyle1 = this.createBorderStyle(headerStyle1);
					headerStyle1.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
					headerStyle1.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
					
					HSSFCellStyle headerStyle2 = report.createCellStyle();
					headerStyle2 = this.createBorderStyle(headerStyle2);
					headerStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
					headerStyle2.setFillForegroundColor(new HSSFColor.LIGHT_YELLOW().getIndex());
					
					HSSFCellStyle contentStyleGreen = report.createCellStyle();
					contentStyleGreen = this.createBorderStyle(contentStyleGreen);
					contentStyleGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
					contentStyleGreen.setFillForegroundColor(new HSSFColor.LIGHT_GREEN().getIndex());
					
					HSSFCellStyle contentStyleOrange = report.createCellStyle();
					contentStyleOrange = this.createBorderStyle(contentStyleOrange);
					contentStyleOrange.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
					contentStyleOrange.setFillForegroundColor(new HSSFColor.LIGHT_ORANGE().getIndex());
					
					HSSFCellStyle diffStyle = report.createCellStyle();
					diffStyle = this.createBorderStyle(diffStyle);
					diffStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
					diffStyle.setFillForegroundColor(new HSSFColor.PALE_BLUE().getIndex());
					
					HSSFCellStyle emptyStyle = report.createCellStyle();
					emptyStyle = this.createBorderStyle(emptyStyle);
					
					//The rest of the stuff		
				    HSSFSheet sheet = report.createSheet(refTbl);				    		
				    boolean alreadyHasHeaders = false;
				    
				    int rowCount = 0;
				    if (dataInTargetNotSourceMap.containsKey(refTbl)) {
				    	TableRowObject firstTro = dataInTargetNotSourceMap.get(refTbl).get(0);
				    	//Print the header if not alreadyHasHeaders
				    	if (!alreadyHasHeaders) {
						    rowCount = printHeader(refTbl, sheet, firstTro, headerFont, headerStyle1, headerStyle2);
						    alreadyHasHeaders = true;
				    	}
				    	//Print the content
				    	List<TableRowObject> troList = dataInTargetNotSourceMap.get(refTbl);
				    	rowCount = printContent(sheet, troList, rowCount, ANALYSIS_TARGET, contentStyleOrange, headerStyle2, emptyStyle);
				    }
				    
				    if (dataInSourceNotTargetMap.containsKey(refTbl)) {
					    //Print the header
					    TableRowObject firstTro = dataInSourceNotTargetMap.get(refTbl).get(0);
					    if (!alreadyHasHeaders) {
						    rowCount = printHeader(refTbl, sheet, firstTro, headerFont, headerStyle1, headerStyle2);
						    alreadyHasHeaders = true;
					    }
				    	//Print the content
				    	List<TableRowObject> troList = dataInSourceNotTargetMap.get(refTbl);
				    	rowCount = printContent(sheet, troList, rowCount, ANALYSIS_SOURCE, contentStyleGreen, headerStyle2, emptyStyle);
				    }
				    
				    if (dataMismatchedMap.containsKey(refTbl)) {
				    	//Print the header if not already has Headers
				    	if (!alreadyHasHeaders) {
						    TableRowObjectDiff firstTrod = dataMismatchedMap.get(refTbl).get(0);
						    TableRowObject firstSourceTro = firstTrod.getSourceTableRowObject();
						    rowCount = printHeader(refTbl, sheet, firstSourceTro, headerFont, headerStyle1, headerStyle2);
						    alreadyHasHeaders = true;
				    	}
				    	List<TableRowObjectDiff> trodList = dataMismatchedMap.get(refTbl);
				    	printContentForSourceTargetDiff(sheet, trodList, rowCount, ANALYSIS_CHANGED, contentStyleGreen, contentStyleOrange, headerStyle2, diffStyle, emptyStyle);
				    }
				}
			}
			
			writeToFilesPerModules(moduleWorkbookMap);
			// Zip all of the output files now.
			createZipFile();
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("error", e);
		}
	}

	private void writeToFilesPerModules(Map<String, HSSFWorkbook> moduleWorkbookMap)  {
		try {
			String directory =  Configuration.getProperty("data.compare.output.dir");
			List<String> statsLines = new ArrayList<String>();
			File statsFile = new File(directory + "Statistics.txt");
			statsLines.add("Statistics : ");
			statsLines.add("*****************************************************************");
			statsLines.add("\n");
			for (String prefix : moduleWorkbookMap.keySet()) {
				String fileName = directory + prefix + "_data_diff.xls";
				FileOutputStream fos = new FileOutputStream(fileName);
				HSSFWorkbook report = (HSSFWorkbook)moduleWorkbookMap.get(prefix);
                statsLines.add(prefix + " module has " + report.getNumberOfSheets() + " tables in the spreadsheet.");
                for (int i=0;i < report.getNumberOfSheets(); i++) {
                	HSSFSheet sheet = report.getSheetAt(i);
                	int rowNum = sheet.getLastRowNum();
                	String name = sheet.getSheetName();
                	statsLines.add("   Table " + name + " has " + rowNum + " rows. \n");
                	List<TableRowObject> sourceTroList = this.dataInSourceNotTargetMap.get(name);
                	if (sourceTroList != null && sourceTroList.size() > 0) {
                		statsLines.add("      " + sourceTroList.size() + " rows exist in LNDB Only");
                	}
                	List<TableRowObject> targetTroList = this.dataInTargetNotSourceMap.get(name);
                	if (targetTroList != null && targetTroList.size() > 0) {
                		statsLines.add("      " + targetTroList.size() + " rows exist in Foundation Only");
                	}
                	List<TableRowObjectDiff> troDiffList = this.dataMismatchedMap.get(name);
                	if (troDiffList != null && troDiffList.size() > 0) {
                		statsLines.add("      " + troDiffList.size() + " rows are different between LNDB and Foundation");
                	}
                }
				report.write(fos);
				fos.flush();
				fos.close();
				statsLines.add("\n");
			}
            statsLines.add("Bootstrap tables that are not being included for this comparison: ");

            for (String tableName : this.excludedTables) {
            	statsLines.add(tableName + "\n");
            }
            statsLines.add("\n");
            
            //Find bootstrap tables that are included in this comparison but has no differences at all.
            List<String> tablesWithNoDiff = new ArrayList<String>();
            for (String tableName : this.refTables) {
            	if (!this.dataInSourceNotTargetMap.containsKey(tableName) &&
            		!this.dataInTargetNotSourceMap.containsKey(tableName) &&
            		!this.dataMismatchedMap.containsKey(tableName)) {
            		
            		tablesWithNoDiff.add(tableName);
            	}
            }
            if (tablesWithNoDiff.size() > 0) {
                statsLines.add("Bootstrap tables that are included in this comparison but has no differences at all: ");
                for (String tableName : tablesWithNoDiff) {
                	statsLines.add(tableName + "\n");
                }
            }
            FileUtil.writeLines(statsLines, statsFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public String createZipFile() throws Exception {
    	File dataDiffDir = new File(Configuration.getProperty("data.compare.output.dir"));
    	File[] dataDiffFiles = dataDiffDir.listFiles();
    	if (dataDiffFiles == null || dataDiffFiles.length == 0) {
    	    // no files to download
    		return null;
    	}
    	HttpServletResponse response = ServletActionContext.getResponse();
    	response.setContentType("application/zip");
    	response.setHeader("Content-disposition", "attachment; filename=\"all-data-diffs.zip\"");
    	ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
    	for (File file : dataDiffFiles) {
    	    ZipEntry entry = new ZipEntry(file.getName());
    	    zos.putNextEntry(entry);
    	    FileInputStream is = new FileInputStream(file);
    	    FileUtil.streamOut(is, zos, false);
    	    is.close();
    	    zos.closeEntry();
    	    zos.flush();
    	}
    	zos.finish();
    	zos.close();
    	return null;
    }
    

	protected HSSFCellStyle createBorderStyle(HSSFCellStyle style) {
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setWrapText(true);
		return style;
	}

}
