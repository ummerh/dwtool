package com.lndb.dwtool.erm.dml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
public class SimpleConverter extends ExcelToDmlConverterBase implements DMLConverterInterface{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SimpleConverter.class);

	@Override
	protected List<TupleData> processUpdate(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		List<TupleData> results = new ArrayList<TupleData>();
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		results.add(tuple);
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		int colType = -1;
		String colValue = "";
		for (String key : primaryKeys) {
			tuple.getMatchingColumns().add(key);
			if (tableDesc.getColumn(key) != null) {
				colType = tableDesc.getColumn(key).getJdbcType();
			}
			colValue = readColValue(key,headerMap, dataCols);
			colValue = ConverterUtil.escapeSpecialChar(colValue);
			tuple.getMatchingColValMap().put(key, colValue);
			tuple.getMatchingColTypeMap().put(key, colType);
		}

		List<ColumnDescriptor> columns = ConverterUtil.getSortedColumns(tableDesc);
		for (ColumnDescriptor columnDescriptor : columns) {
			String colName = columnDescriptor.getName();
			
			if (!OracleSqlConstant.OBJ_ID.equalsIgnoreCase(colName) && !OracleSqlConstant.VER_NBR.equalsIgnoreCase(colName) && !tuple.getMatchingColValMap().containsKey(colName)) {
				tuple.getUpdatingColumns().add(colName);
				if (tableDesc.getColumn(colName) != null) {
					colType = tableDesc.getColumn(colName).getJdbcType();
				}
				
				if (colName.matches(OracleSqlConstant.LAST_UPDATE_PATTERN)) {
					colValue = OracleSqlConstant.SYSDATE;
				}
				else {
					colValue = readColValue(colName,headerMap, dataCols);
					colValue = ConverterUtil.escapeSpecialChar(colValue);
				}
				if (colValue != null) {
					tuple.getUpdatingColValMap().put(colName, colValue.trim());
					tuple.getUpdatingColTypeMap().put(colName, colType);
				}
			}
		}
		return results;
	}


	@Override
	protected List<TupleData> processDelete(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		List<TupleData> results = new ArrayList<TupleData>();
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		results.add(tuple);
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		int colType = -1;
		String colValue = "";
		for (String key : primaryKeys) {
			tuple.getMatchingColumns().add(key);
			if (tableDesc.getColumn(key) != null) {
				colType = tableDesc.getColumn(key).getJdbcType();
			}
			colValue = readColValue(key,headerMap, dataCols);
			colValue = ConverterUtil.escapeSpecialChar(colValue);
			tuple.getMatchingColValMap().put(key, colValue);
			tuple.getMatchingColTypeMap().put(key, colType);
		}
		return results;
	}

	@Override
	protected List<TupleData> processInsert(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		List<TupleData> results = new ArrayList<TupleData>();
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		results.add(tuple);
		List<ColumnDescriptor> columns = ConverterUtil.getSortedColumns(tableDesc);
		int colType = -1;
		for (ColumnDescriptor columnDescriptor : columns) {
			String colName = columnDescriptor.getName();
			tuple.getUpdatingColumns().add(colName);
			if (OracleSqlConstant.OBJ_ID.equalsIgnoreCase(colName)) {
				tuple.getUpdatingColValMap().put(colName, OracleSqlConstant.SYS_GUID);
				tuple.getUpdatingColTypeMap().put(colName, colType);
			}else if (OracleSqlConstant.VER_NBR.equalsIgnoreCase(colName)) {
				tuple.getUpdatingColValMap().put(colName, OracleSqlConstant.DEFAULT_VERSION_NBR);
				tuple.getUpdatingColTypeMap().put(colName, colType);
			}else {
				if (tableDesc.getColumn(colName) != null) {
					colType = tableDesc.getColumn(colName).getJdbcType();
				}
				String colValue = "";
				if (colName.matches(OracleSqlConstant.LAST_UPDATE_PATTERN)) {
					colValue = OracleSqlConstant.SYSDATE;
				}
				else {
					colValue = readColValue(colName,headerMap, dataCols);
					colValue = ConverterUtil.escapeSpecialChar(colValue);
				}
				if (colValue != null) {
					tuple.getUpdatingColValMap().put(colName, colValue.trim());
					tuple.getUpdatingColTypeMap().put(colName, colType);
				}
			}
		}
		
		return results;
	}

}
