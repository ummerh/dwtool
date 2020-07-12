package com.lndb.dwtool.erm.dml;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.dml.data.TupleData;
import com.lndb.dwtool.erm.util.ExcelReaderBase;

/**
 * This class is an implementation class for converting excel data to SQL script
 * 
 * @author ZHANGMA
 * 
 */
public class OneOnOneConverter extends ExcelReaderBase implements DMLConverterInterface{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(OneOnOneConverter.class);

	public void convertToSQL(FileInputStream dataInputStream, DBMap targetDb, BufferedWriter out) {
		try {
			Workbook wb = new HSSFWorkbook(new POIFSFileSystem(dataInputStream));
			int nbrOfSheets = wb.getNumberOfSheets();

			for (int sheetIndex = 0; sheetIndex < nbrOfSheets; sheetIndex++) {
				Sheet currentSheet = wb.getSheetAt(sheetIndex);

				String tableName = currentSheet.getSheetName();
				TableDescriptor tableDesc = targetDb.getTableDescriptor(tableName);
				LOG.info("Processing " + tableName);
				out.write("-- Converting for " + tableName + "...");
				out.newLine();

				Iterator<Row> rowIterator = currentSheet.iterator();
				Row headerRow = rowIterator.hasNext() ? rowIterator.next(): null;
				
				// header row
				if (headerRow != null) {
					Map<String, Integer> headerMap = readHeaderMap(headerRow);
					
					while (rowIterator.hasNext()) {
						List<String> dataCols = readDataCells(rowIterator
								.next());
						// Sheet Name: LNDB Tables; Technical Decision � Insert,Delete,Update,No_change ;
						String decision = readColValue("Technical Decision",
								headerMap, dataCols);
						if (OracleSqlConstant.UserDecision.NO_CHANGE.equalsIgnoreCase(decision) || StringUtils.equalsIgnoreCase("no_change", decision)) {
							LOG.info("No change for " + dataCols.toString());
							continue;
						}
						else if (OracleSqlConstant.UserDecision.INSERT.equalsIgnoreCase(decision)) {
							TupleData tuple = processInsert(tableDesc, headerMap, dataCols);
							OracleDecision.INSERT.generateSQL(tuple);
							postProcessData(tuple,out);
						}
						else if (OracleSqlConstant.UserDecision.DELETE.equalsIgnoreCase(decision)) {
							TupleData tuple = processDelete(tableDesc, headerMap, dataCols);
							OracleDecision.DELETE.generateSQL(tuple);
							postProcessData(tuple,out);
						}
						else if (OracleSqlConstant.UserDecision.UPDATE.equalsIgnoreCase(decision)) {
							TupleData tuple = processUpdate(tableDesc, headerMap, dataCols);
							OracleDecision.UPDATE.generateSQL(tuple);
							postProcessData(tuple,out);
						}
						else {
							out.write("--Technical Decision '" + decision + "' is not allowed for row '" + dataCols.toString() + "'");
							LOG.error("no decision defined for " + decision);
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private TupleData processUpdate(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols) {
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		int colType = -1;
		for (String key : primaryKeys) {
			if (tableDesc.getColumn(key) != null) {
				colType = tableDesc.getColumn(key).getJdbcType();
			}
			tuple.getMatchingColValMap().put(key, readColValue(key,
					headerMap, dataCols));
			tuple.getMatchingColTypeMap().put(key, colType);
		}

		List<ColumnDescriptor> columns = tableDesc.getColumns();
		for (ColumnDescriptor columnDescriptor : columns) {
			String colName = columnDescriptor.getName();
			if (!OracleSqlConstant.OBJ_ID.equalsIgnoreCase(colName) && !OracleSqlConstant.VER_NBR.equalsIgnoreCase(colName) && !tuple.getMatchingColValMap().containsKey(colName)) {
				if (tableDesc.getColumn(colName) != null) {
					colType = tableDesc.getColumn(colName).getJdbcType();
				}
				String colValue = readColValue(colName,headerMap, dataCols);
				if (colValue != null) {
					tuple.getUpdatingColValMap().put(colName, colValue.trim());
					tuple.getUpdatingColTypeMap().put(colName, colType);
				}
			}
		}
		return tuple;
	}

	private void postProcessData(TupleData tuple, BufferedWriter out) {
		if (StringUtils.isNotBlank(tuple.getErrorMessage())) {
			LOG.error(tuple.getErrorMessage());
			try {
				out.write("--" + tuple.getErrorMessage());
				out.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOG.info(tuple.getSqlString());
		try {
			out.write(tuple.getSqlString());
			out.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private TupleData processDelete(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols) {
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		int colType = -1;
		for (String key : primaryKeys) {
			if (tableDesc.getColumn(key) != null) {
				colType = tableDesc.getColumn(key).getJdbcType();
			}
			tuple.getMatchingColValMap().put(key, readColValue(key,
					headerMap, dataCols));
			tuple.getMatchingColTypeMap().put(key, colType);
		}
		return tuple;
	}

	private TupleData processInsert(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols) {
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		List<ColumnDescriptor> columns = tableDesc.getColumns();
		int colType = -1;
		for (ColumnDescriptor columnDescriptor : columns) {
			String colName = columnDescriptor.getName();
			if (!OracleSqlConstant.OBJ_ID.equalsIgnoreCase(colName) && !OracleSqlConstant.VER_NBR.equalsIgnoreCase(colName)) {
				if (tableDesc.getColumn(colName) != null) {
					colType = tableDesc.getColumn(colName).getJdbcType();
				}
				
				String colValue = readColValue(colName,headerMap, dataCols);
				if (colValue != null) {
					tuple.getUpdatingColValMap().put(colName, colValue.trim());
					tuple.getUpdatingColTypeMap().put(colName, colType);
				}
			}
		}
		
		return tuple;
	}
	

	protected Map<String, String> getColumnTypeMap(Row headerRow, TableDescriptor tableDesc) {
		List<String> headerNames = readDataCells(headerRow);
		Map<String, String> headerDataTypeMap = new HashMap<String, String>();
		for (String colName : headerNames) {
			ColumnDescriptor colDesc = tableDesc.getColumn(colName);
			if (colDesc != null) {
				headerDataTypeMap.put(colName, colDesc.getSqlDataType());
			}
		}
		
		return headerDataTypeMap;
	}

	public boolean convertToSQL(FileInputStream dataInputStream, DBMap targetDb, String outputDir, BufferedWriter report, BufferedWriter errorReport) {
		// TODO Auto-generated method stub
		return false;
	}
}
