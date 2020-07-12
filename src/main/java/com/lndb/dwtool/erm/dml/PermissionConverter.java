package com.lndb.dwtool.erm.dml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.dml.data.ExcelMetadata;
import com.lndb.dwtool.erm.dml.data.PermissionAttributeExcel;
import com.lndb.dwtool.erm.dml.data.RoleMemberExcel;
import com.lndb.dwtool.erm.dml.data.RolePermissionExcel;
import com.lndb.dwtool.erm.dml.data.TableReference;
import com.lndb.dwtool.erm.dml.data.TupleData;
import com.lndb.dwtool.erm.dml.data.TableReference.ReferenceData;

/**
 * This class is an implementation class for converting excel data to SQL script
 * 
 * @author ZHANGMA
 * 
 */
public class PermissionConverter extends ComplexConverter implements DMLConverterInterface{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PermissionConverter.class);

	@Override
	protected ProcessResult processWorkSheet(BufferedWriter report, Sheet currentSheet,
			BufferedWriter output, DBMap targetDb, BufferedWriter errorReport, String tableName)
			throws IOException {
		TableDescriptor tableDesc = targetDb.getTableDescriptor(tableName);
		if (tableDesc == null) {
			errorReport.write("  ERROR: work sheet name is not specified and can't decide table name");
			return new ProcessResult(false, true);
		}
		Iterator<Row> rowIterator = currentSheet.iterator();
		Row headerRow = rowIterator.hasNext() ? rowIterator.next(): null;
		boolean dmlExist = false;
		boolean errorExist = false;
		// header row
		if (headerRow != null) {
			Map<String, Integer> headerMap = readCustHeaderMap(headerRow);
			int loadCounter = 0;
			int failCounter = 0;
			int noChangeCnt = 0;
			int rowCnt = 1;
			
			while (rowIterator.hasNext()) {
				List<String> dataCols = readDataCells(rowIterator
						.next());
				rowCnt++;
				// Sheet Name: LNDB Tables; Technical Decision � Insert,Delete,Update,No_change ;
				try {
					if (dataCols == null || dataCols.isEmpty()) {
						continue;
					}
					String decision = readColValue("Technical Decision",
							headerMap, dataCols);
					if (StringUtils.isBlank(decision)) {
						decision = readColValue("Functional Decision",
							headerMap, dataCols);
					}
					if (StringUtils.isBlank(decision)) {
						errorReport.write("  ERROR - Row " + rowCnt + " : No Decision for " + dataCols.toString());
						errorReport.newLine();
						report.write("  ERROR - Row " + rowCnt + " : No Decision for " + dataCols.toString());
						report.newLine();
						failCounter++;
						errorExist = true;
						continue;
					}
					if (OracleSqlConstant.UserDecision.NO_CHANGE.equalsIgnoreCase(decision) || StringUtils.equalsIgnoreCase("no_change", decision)) {
						LOG.debug("No change for " + dataCols.toString());
						noChangeCnt++;
						continue;
					}
					else if (OracleSqlConstant.UserDecision.INSERT.equalsIgnoreCase(decision)) {
						dmlExist = true;
						List<TupleData> tuples = processInsert(tableDesc, headerMap, dataCols, targetDb);
						boolean exeResult = true;
						for (TupleData tupleData : tuples) {
							OracleDecision.INSERT.generateSQL(tupleData);
							exeResult &= postProcessData(tupleData,report,output, rowCnt, errorReport);
						}
						if (exeResult) {
							loadCounter++;
						}
						else {
							failCounter++;
						}
					}
					else if (OracleSqlConstant.UserDecision.DELETE.equalsIgnoreCase(decision)) {
						dmlExist = true;
						List<TupleData> tuples = processDelete(tableDesc, headerMap, dataCols, targetDb);
						boolean exeResult = true;
						for (TupleData tupleData : tuples) {
							OracleDecision.DELETE.generateSQL(tupleData);
							exeResult &= postProcessData(tupleData,report,output, rowCnt, errorReport);
						}
						if (exeResult) {
							loadCounter++;
						}
						else {
							failCounter++;
						}
					}
					else if (OracleSqlConstant.UserDecision.UPDATE.equalsIgnoreCase(decision)) {
						dmlExist = true;
						List<TupleData> tuples = processUpdate(tableDesc, headerMap, dataCols, targetDb);
						boolean exeResult = true;
						TupleData tupleData = tuples.get(0);
						OracleDecision.UPDATE.generateSQL(tupleData);
						exeResult &= postProcessData(tupleData,report,output, rowCnt, errorReport);
						
						for (int i=1;i<tuples.size();i++) {
							tupleData = tuples.get(i);
							OracleDecision.INSERT.generateSQL(tupleData);
							exeResult &= postProcessData(tupleData,report,output, rowCnt, errorReport);
						}
						if (exeResult) {
							loadCounter++;
						}
						else {
							failCounter++;
						}
					}
					else {
						errorReport.write("  ERROR - Row " + rowCnt + " : Functional Decision '" + decision + "' is not allowed for row '" + dataCols.toString() + "'");
						errorReport.newLine();
						report.write("  ERROR - Row " + rowCnt + " : Functional Decision '" + decision + "' is not allowed for row '" + dataCols.toString() + "'");
						report.newLine();
						LOG.error("no decision defined for " + decision);
						failCounter++;
						errorExist = true;
					}
				}catch (Exception e) {
					LOG.error("error processing row " + rowCnt + " :" + dataCols);
					throw new RuntimeException(e);
				}
			}
			
			output.write("COMMIT;");
			report.newLine();
			report.write("[SUMMARY]\t");
			report.write("\tSucceed: " + loadCounter);
			report.write("\tFailed: " + failCounter);
			report.write("\tNo change and skipped: " + noChangeCnt );
			report.newLine();
		}
		return new ProcessResult(dmlExist, errorExist);
	}

	
	@Override
	protected List<TupleData> processUpdate(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		if (tableDesc == null) {
			tableDesc = targetDb.getTableDescriptor("KRIM_PERM_T");
		}
		// process krim_perm_t
		List<TupleData> results = super.processUpdate(tableDesc, headerMap, dataCols, targetDb);
		
		// process krim_role_perm_t
//		tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_PERM_T");
//		RolePermissionExcel rolePerm = parseRolePermData(headerMap, dataCols, targetDb);
//		
//		generateRolePermTuple(rolePerm, results, tableDesc, false);
//		
		// process for krim_role_mbr_t
		tableDesc = targetDb.getTableDescriptor("KRIM_PERM_ATTR_DATA_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		
		List<PermissionAttributeExcel> permAttrs = parsePermAttrData(headerMap, dataCols, targetDb);
		generatePermAttrTuples(permAttrs, results, tableDesc, false);
		
		return results;
	}


	private void generatePermAttrTuples(
			List<PermissionAttributeExcel> permAttrs, List<TupleData> results,
			TableDescriptor tableDesc, boolean insert) {
		String colValue = "";
		int colType = -1;
		String colName = "";
		TupleData tuple;

		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);

		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		
		for (PermissionAttributeExcel attribute : permAttrs) {
			tuple = new TupleData(true, true, tableDesc.getTableName());
			results.add(tuple);
			
			for (String key : primaryKeys) {
				tuple.getMatchingColumns().add(key);
				if (tableDesc.getColumn(key) != null) {
					colType = tableDesc.getColumn(key).getJdbcType();
				}
				colValue = getValueFromExcel(PermissionAttributeExcel.class, attribute,
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
					colValue = getValueFromExcel(PermissionAttributeExcel.class, attribute, excelMeta, colName);
					if (colValue != null) {
						tuple.getUpdatingColumns().add(colName);
						tuple.getUpdatingColValMap().put(colName, colValue);
						tuple.getUpdatingColTypeMap().put(colName, colType);
					}
				}
			}
		}
	}


	private List<PermissionAttributeExcel> parsePermAttrData(
			Map<String, Integer> headerMap, List<String> dataCols,
			DBMap targetDb) {
		List<PermissionAttributeExcel> results = new ArrayList<PermissionAttributeExcel>();

		String colValue = readColValue("PERM_ATTR",headerMap, dataCols);
		
		if (StringUtils.isBlank(colValue)) {
			return results;
		}
		
		colValue = StringUtils.substringBetween(colValue, "{", "}");
		String[] attrs = colValue.split(",");
		
		for (int i=0; i < attrs.length; i++) {
			PermissionAttributeExcel attribute = new PermissionAttributeExcel();
			results.add(attribute);
			String[] attrDetails = attrs[i].split(";");
			String attrDefnValue = attrDetails[0];
			String attrDataId = attrDetails[1];
			String kimTypeId = attrDetails[2];
			attribute.setAttributeDefnId(StringUtils.trim(StringUtils.substringBefore(attrDefnValue, "=")));
			attribute.setValue(StringUtils.trim(StringUtils.substringAfter(attrDefnValue, "=")));
			attribute.setTypeId(StringUtils.trim(StringUtils.substringAfter(kimTypeId, "=")));
			attribute.setPermAttributeId(StringUtils.trim(StringUtils.substringAfter(attrDataId, "=")));
			attribute.setPermId(readColValue("PERM_ID",headerMap, dataCols));
		}
		
		return results;
	}




	@Override
	protected List<TupleData> processDelete(TableDescriptor roleTableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		List<TupleData> results = new ArrayList<TupleData>();
		// process KRIM_PERM_ATTR_DATA_T
		TableDescriptor tableDesc = targetDb.getTableDescriptor("KRIM_PERM_ATTR_DATA_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		List<PermissionAttributeExcel> permAttrs = parsePermAttrData(headerMap, dataCols, targetDb);
		if (permAttrs == null || permAttrs.isEmpty()) {
			return results;
		}
		
		
//		for (PermissionAttributeExcel attribute : permAttrs) {
//			generateDelateTuple(results, tableDesc, excelMeta,
//					  PermissionAttributeExcel.class, attribute);
//		}
//		// process krim_role_perm_t
//		tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_PERM_T");
//		RolePermissionExcel rolePerm = parseRolePermData(headerMap, dataCols, targetDb);
//		generateDelateTuple(results, tableDesc, excelMeta,
//				RolePermissionExcel.class, rolePerm);
		
		// process krim_perm_t
//		tableDesc = targetDb.getTableDescriptor("KRIM_PERM_T");
//		results.addAll(super.processDelete(roleTableDesc, headerMap, dataCols, targetDb));
		generatePermAttrTuples(permAttrs, results, tableDesc, false);
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
			tableDesc = targetDb.getTableDescriptor("KRIM_PERM_T");
		}
		// process krim_perm_t
		List<TupleData> results = super.processInsert(tableDesc, headerMap, dataCols, targetDb);
				
		// process krim_role_perm_t
//		tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_PERM_T");
//		RolePermissionExcel rolePerm = parseRolePermData(headerMap, dataCols, targetDb);
//			
//		generateRolePermTuple(rolePerm, results, tableDesc, true);
				
		// process for krim_role_mbr_t
		tableDesc = targetDb.getTableDescriptor("KRIM_PERM_ATTR_DATA_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
				
		List<PermissionAttributeExcel> permAttrs = parsePermAttrData(headerMap, dataCols, targetDb);
		
//		Map<String, String> roleRefValueMap = getReferenceValueMap(tableDesc,
//				headerMap, dataCols, targetDb);
//		String permId = roleRefValueMap.get("PERM_ID");
//		for (PermissionAttributeExcel permAttr: permAttrs) {
//			permAttr.setPermId(OracleSqlConstant.LEFT_PARENTHESE + permId + OracleSqlConstant.RIGHT_PARENTHESE);
//		}
		generatePermAttrTuples(permAttrs, results, tableDesc, true);
		return results;
	}
	
	
}
