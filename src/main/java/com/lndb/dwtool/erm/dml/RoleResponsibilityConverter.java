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
public class RoleResponsibilityConverter extends ComplexConverter implements DMLConverterInterface{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(RoleResponsibilityConverter.class);

	@Override
	protected List<TupleData> processUpdate(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		if (tableDesc == null) {
			tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_RSP_T");
		}
		// process KRIM_ROLE_RSP_T
		List<TupleData> results = super.processUpdate(tableDesc, headerMap, dataCols, targetDb);
		
		// process KRIM_ROLE_RSP_ACTN_T
		tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_RSP_ACTN_T");
		results.addAll(super.processUpdate(tableDesc, headerMap, dataCols, targetDb));
		return results;
	}

	@Override
	protected List<TupleData> processDelete(TableDescriptor roleTableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		// process KRIM_RSP_ATTR_DATA_T
		TableDescriptor tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_RSP_ACTN_T");
		List<TupleData> results = super.processDelete(tableDesc, headerMap, dataCols, targetDb);
		// process krim_rsp_t
		tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_RSP_T");
		results.addAll(super.processDelete(tableDesc, headerMap, dataCols, targetDb));
				
		return results;
	}

	@Override
	protected List<TupleData> processInsert(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		if (tableDesc == null) {
			tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_RSP_T");
		}
		// process KRIM_ROLE_RSP_T
		List<TupleData> results = super.processInsert(tableDesc, headerMap, dataCols, targetDb);
				
		// process KRIM_ROLE_RSP_ACTN_T
		tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_RSP_ACTN_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
				
		results.addAll(super.processInsert(tableDesc, headerMap, dataCols, targetDb));
		return results;
	}
	
	
}
