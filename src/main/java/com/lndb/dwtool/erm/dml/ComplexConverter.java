package com.lndb.dwtool.erm.dml;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.dml.data.ExcelMetadata;
import com.lndb.dwtool.erm.dml.data.RoleMemberExcel;
import com.lndb.dwtool.erm.dml.data.TupleData;

/**
 * This class is an implementation class for converting excel data to SQL script
 * 
 * @author ZHANGMA
 * 
 */
public class ComplexConverter extends ExcelToDmlConverterBase implements DMLConverterInterface{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(ComplexConverter.class);

	
	@Override
	protected List<TupleData> processUpdate(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		Map<String, String> refValueMap = getReferenceValueMap(tableDesc,
				headerMap, dataCols, targetDb);
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		
		List<TupleData> results = new ArrayList<TupleData>();
		String colValue = "";
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		results.add(tuple);
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		int colType = -1;
		for (String key : primaryKeys) {
			tuple.getMatchingColumns().add(key);
			if (tableDesc.getColumn(key) != null) {
				colType = tableDesc.getColumn(key).getJdbcType();
			}
			
			colValue = getValueFromExcelWithMismatchName(headerMap,
					dataCols, excelMeta, key);
			tuple.getMatchingColValMap().put(key, colValue);
			tuple.getMatchingColTypeMap().put(key, colType);
		}
			
		String colName = "";
		List<ColumnDescriptor> columns = ConverterUtil.getSortedColumns(tableDesc);
		for (ColumnDescriptor columnDescriptor : columns) {
			colName = columnDescriptor.getName();
			if (!OracleSqlConstant.OBJ_ID.equalsIgnoreCase(colName) && !OracleSqlConstant.VER_NBR.equalsIgnoreCase(colName) && !tuple.getMatchingColValMap().containsKey(colName)) {
				tuple.getUpdatingColumns().add(colName);
				if (tableDesc.getColumn(colName) != null) {
					colType = tableDesc.getColumn(colName).getJdbcType();
				}
				if (colName.matches(OracleSqlConstant.LAST_UPDATE_PATTERN)) {
					colValue = OracleSqlConstant.SYSDATE;
				}
				else if (refValueMap.containsKey(colName)) {
					colValue = OracleSqlConstant.LEFT_PARENTHESE + refValueMap.get(colName) + OracleSqlConstant.RIGHT_PARENTHESE;
				}
				else {
					colValue = getValueFromExcelWithMismatchName(headerMap,
							dataCols, excelMeta, colName);
				}
				if (OracleSqlConstant.ACTV_IND.equalsIgnoreCase(colName) && StringUtils.isBlank(colValue)) {
					colValue = OracleSqlConstant.ACTIVE_YES;
				}
				if (colValue != null) {
					tuple.getUpdatingColValMap().put(colName, colValue);
					tuple.getUpdatingColTypeMap().put(colName, colType);
				}
			}
		}
		return results;
	}


	@Override
	protected List<TupleData> processDelete(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		List<TupleData> results = new ArrayList<TupleData>();
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		results.add(tuple);
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		int colType = -1;
		for (String key : primaryKeys) {
			tuple.getMatchingColumns().add(key);
			if (tableDesc.getColumn(key) != null) {
				colType = tableDesc.getColumn(key).getJdbcType();
			}
			String colValue = getValueFromExcelWithMismatchName(headerMap,
					dataCols, excelMeta, key);
			tuple.getMatchingColValMap().put(key, colValue);
			tuple.getMatchingColTypeMap().put(key, colType);
		}
		return results;
	}

	@Override
	protected List<TupleData> processInsert(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		Map<String, String> refValueMap = getReferenceValueMap(tableDesc,
				headerMap, dataCols, targetDb);
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		List<TupleData> results = new ArrayList<TupleData>();
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		results.add(tuple);
		List<ColumnDescriptor> columns = tableDesc.getColumns();
		int colType = -1;
		String colValue = "";
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
				if (colName.matches(OracleSqlConstant.LAST_UPDATE_PATTERN)) {
					colValue = OracleSqlConstant.SYSDATE;
				}
				else if (refValueMap.containsKey(colName)) {
					colValue = OracleSqlConstant.LEFT_PARENTHESE + refValueMap.get(colName) + OracleSqlConstant.RIGHT_PARENTHESE;
				}
				else {
					colValue = getValueFromExcelWithMismatchName(headerMap,
							dataCols, excelMeta, colName);
				}
				if (OracleSqlConstant.ACTV_IND.equalsIgnoreCase(colName) && StringUtils.isBlank(colValue)) {
					colValue = OracleSqlConstant.ACTIVE_YES;
				}
				if (colValue != null) {
					tuple.getUpdatingColValMap().put(colName, colValue);
					tuple.getUpdatingColTypeMap().put(colName, colType);
				}
			}
		}
		
		return results;
	}
	


	protected String getValueFromExcel(Class sourceClass, Object sourceObj,ExcelMetadata excelMeta, String colName) {
		Class<?>[] emptyParams = new Class[] {};
//		 Object[] emptyObjParams = new Object[] {null};
		try {
//			PropertyDescriptor propDesc = PropertyUtils.getPropertyDescriptor(sourceClass, excelMeta.getColToPropertyMap().get(colName));
//			Method getter = propDesc.getReadMethod();
			String propertyName = excelMeta.getColToPropertyMap().get(colName);
			propertyName = Character.toString(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1);
			Method getter = sourceClass.getMethod("get" + propertyName, emptyParams);
			return (String)getter.invoke(sourceObj);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}
