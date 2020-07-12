package com.lndb.dwtool.erm.dml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.dml.data.ExcelMetadata;
import com.lndb.dwtool.erm.dml.data.ResponsibilityAttributeExcel;
import com.lndb.dwtool.erm.dml.data.TupleData;

/**
 * This class is an implementation class for converting excel data to SQL script
 * 
 * @author ZHANGMA
 * 
 */
public class ResponsibilityConverter extends PermissionConverter implements DMLConverterInterface{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(ResponsibilityConverter.class);

	@Override
	protected List<TupleData> processUpdate(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		if (tableDesc == null) {
			tableDesc = targetDb.getTableDescriptor("KRIM_RSP_T");
		}
		// process krim_rsp_t
		List<TupleData> results = super.processUpdate(tableDesc, headerMap, dataCols, targetDb);
		
		// process KRIM_RSP_ATTR_DATA_T
		tableDesc = targetDb.getTableDescriptor("KRIM_RSP_ATTR_DATA_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		
		List<ResponsibilityAttributeExcel> rspAttrs = parseRspAttrData(headerMap, dataCols, targetDb);
		generateRspAttrTuples(rspAttrs, results, tableDesc, false);
		
		return results;
	}


	private void generateRspAttrTuples(
			List<ResponsibilityAttributeExcel> rspAttrs, List<TupleData> results,
			TableDescriptor tableDesc, boolean insert) {
		String colValue = "";
		int colType = -1;
		String colName = "";
		TupleData tuple;

		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);

		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		
		for (ResponsibilityAttributeExcel attribute : rspAttrs) {
			tuple = new TupleData(true, true, tableDesc.getTableName());
			results.add(tuple);
			
			for (String key : primaryKeys) {
				tuple.getMatchingColumns().add(key);
				if (tableDesc.getColumn(key) != null) {
					colType = tableDesc.getColumn(key).getJdbcType();
				}
				colValue = getValueFromExcel(ResponsibilityAttributeExcel.class, attribute,
						excelMeta, key);
				tuple.getMatchingColValMap().put(key, colValue);
				tuple.getMatchingColTypeMap().put(key, colType);
			}
	
			List<ColumnDescriptor> columns = ConverterUtil
					.getSortedColumns(tableDesc);
			for (ColumnDescriptor columnDescriptor : columns) {
				colName = columnDescriptor.getName();
				if (OracleSqlConstant.OBJ_ID.equalsIgnoreCase(colName)) {
					tuple.getUpdatingColumns().add(colName);
					tuple.getUpdatingColValMap().put(colName, OracleSqlConstant.SYS_GUID);
					tuple.getUpdatingColTypeMap().put(colName, colType);
				}else if (OracleSqlConstant.VER_NBR.equalsIgnoreCase(colName)) {
					tuple.getUpdatingColumns().add(colName);
					tuple.getUpdatingColValMap().put(colName, OracleSqlConstant.DEFAULT_VERSION_NBR);
					tuple.getUpdatingColTypeMap().put(colName, colType);
				}else {
					if (tableDesc.getColumn(colName) != null) {
						colType = tableDesc.getColumn(colName).getJdbcType();
					}
					
					if (insert && OracleSqlConstant.ACTV_IND.equalsIgnoreCase(colName)) {
						colValue = OracleSqlConstant.ACTIVE_YES;
					}
					colValue = getValueFromExcel(ResponsibilityAttributeExcel.class, attribute, excelMeta, colName);
					if (colValue != null) {
						tuple.getUpdatingColumns().add(colName);
						tuple.getUpdatingColValMap().put(colName, colValue);
						tuple.getUpdatingColTypeMap().put(colName, colType);
					}
				}
			}
		}
	}


	private List<ResponsibilityAttributeExcel> parseRspAttrData(
			Map<String, Integer> headerMap, List<String> dataCols,
			DBMap targetDb) {
		List<ResponsibilityAttributeExcel> results = new ArrayList<ResponsibilityAttributeExcel>();

		String colValue = readColValue("ATTRS",headerMap, dataCols);
		
		if (StringUtils.isBlank(colValue)) {
			return results;
		}
		
		String[] attrs = colValue.split(",");
		
		for (int i=0; i < attrs.length; i=i+2) {
			ResponsibilityAttributeExcel attribute = new ResponsibilityAttributeExcel();
			results.add(attribute);
			String attrDefnValue = attrs[i];
			String attrId = attrs[i+1];
			attribute.setAttributeDefnId(StringUtils.trim(StringUtils.substringBefore(attrDefnValue, "=")));
			attribute.setValue(StringUtils.trim(StringUtils.substringAfter(attrDefnValue, "=")));
			attribute.setRspAttributeId(StringUtils.trim(StringUtils.substringAfter(attrId, "=")));
			attribute.setRspId(readColValue("RSP_ID",headerMap, dataCols));
		}
		
		return results;
	}


	@Override
	protected List<TupleData> processDelete(TableDescriptor roleTableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		List<TupleData> results = new ArrayList<TupleData>();
		// process KRIM_RSP_ATTR_DATA_T
		TableDescriptor tableDesc = targetDb.getTableDescriptor("KRIM_RSP_ATTR_DATA_T");
		List<ResponsibilityAttributeExcel> rspAttrs = parseRspAttrData(headerMap, dataCols, targetDb);
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		for (ResponsibilityAttributeExcel attribute : rspAttrs) {
			generateDelateTuple(results, tableDesc, excelMeta,
					ResponsibilityAttributeExcel.class, attribute);
		}		
		
		// process krim_rsp_t
//		tableDesc = targetDb.getTableDescriptor("KRIM_RSP_T");
//		results.addAll(super.processDelete(tableDesc, headerMap, dataCols, targetDb));
				
		return results;
	}


	private void generateDelateTuple(List<TupleData> results,
			TableDescriptor tableDesc, ExcelMetadata excelMeta, Class tableMappingClass,
			Object mappingObj) {
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		int colType = -1;
		TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
		results.add(tuple);
		for (String key : primaryKeys) {
			tuple.getMatchingColumns().add(key);
			if (tableDesc.getColumn(key) != null) {
				colType = tableDesc.getColumn(key).getJdbcType();
			}
			String colValue = getValueFromExcel(tableMappingClass, mappingObj, excelMeta, key);
			tuple.getMatchingColValMap().put(key, colValue);
			tuple.getMatchingColTypeMap().put(key, colType);
		}
	}

	@Override
	protected List<TupleData> processInsert(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		if (tableDesc == null) {
			tableDesc = targetDb.getTableDescriptor("KRIM_RSP_T");
		}
		// process krim_rsp_t
		List<TupleData> results = super.processInsert(tableDesc, headerMap, dataCols, targetDb);
				
		// process KRIM_RSP_ATTR_DATA_T
		tableDesc = targetDb.getTableDescriptor("KRIM_RSP_ATTR_DATA_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
				
		List<ResponsibilityAttributeExcel> rspAttrs = parseRspAttrData(headerMap, dataCols, targetDb);
		generateRspAttrTuples(rspAttrs, results, tableDesc, true);
				
//		// process KRIM_ROLE_RSP_T
//		tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_RSP_T");
//		results.addAll(super.processUpdate(tableDesc, headerMap, dataCols, targetDb));
//				
//		// process KRIM_ROLE_RSP_ACTN_T
//		tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_RSP_ACTN_T");
//		results.addAll(super.processUpdate(tableDesc, headerMap, dataCols, targetDb));
		return results;
	}
	
	
}
