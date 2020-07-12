package com.lndb.dwtool.erm.dml;

import java.io.BufferedWriter;
import java.io.File;
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
import com.lndb.dwtool.erm.dml.data.ExcelMetadata;
import com.lndb.dwtool.erm.dml.data.TableReference;
import com.lndb.dwtool.erm.dml.data.TupleData;
import com.lndb.dwtool.erm.dml.data.TableReference.ReferenceData;
import com.lndb.dwtool.erm.util.ExcelReaderBase;

/**
 * This class is an implementation class for converting excel data to SQL script
 * 
 * @author ZHANGMA
 * 
 */
public abstract class ExcelToDmlConverterBase extends ExcelReaderBase implements DMLConverterInterface{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(ExcelToDmlConverterBase.class);

	public class ProcessResult {
		private boolean errorExist;
		private boolean dmlExist;
		
		public ProcessResult(boolean dmlExist, boolean errorExist) {
			this.dmlExist = dmlExist; 
			this.errorExist = errorExist;
		}

		/**
		 * @return the errorExist
		 */
		public boolean isErrorExist() {
			return errorExist;
		}

		/**
		 * @param errorExist the errorExist to set
		 */
		public void setErrorExist(boolean errorExist) {
			this.errorExist = errorExist;
		}

		/**
		 * @return the dmlExist
		 */
		public boolean isDmlExist() {
			return dmlExist;
		}

		/**
		 * @param dmlExist the dmlExist to set
		 */
		public void setDmlExist(boolean dmlExist) {
			this.dmlExist = dmlExist;
		}
		
	}
	
	public boolean convertToSQL(FileInputStream dataInputStream, DBMap targetDb, String outputDir, BufferedWriter report, BufferedWriter errorReport) {
		boolean hasError = false;
		try {
			Workbook wb = new HSSFWorkbook(new POIFSFileSystem(dataInputStream));
			int nbrOfSheets = wb.getNumberOfSheets();
			
			for (int sheetIndex = 0; sheetIndex < nbrOfSheets; sheetIndex++) {
				Sheet currentSheet = wb.getSheetAt(sheetIndex);

				String tableName = currentSheet.getSheetName();
				
				BufferedWriter output = ConverterUtil.getOutputFileStream(outputDir,
						tableName);
				writeHeaderInfo(report, tableName);
				writeHeaderInfo(errorReport, tableName);
				
				ProcessResult result = processWorkSheet(report, currentSheet,
						output, targetDb, errorReport, tableName);
				
				output.close();
				
				if (!result.isDmlExist()) {
					// remove generated sql file
					File sqlFile = new File(ConverterUtil.getDecisionSqlFileName(outputDir, tableName));
					if (sqlFile.delete()) {
						System.out.println("Deleted empty sql" + sqlFile.getPath());
					}
				}
				
				if (result.isErrorExist()) {
					hasError = true;
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hasError;
	}

	private void writeHeaderInfo(BufferedWriter report, String tableName)
			throws IOException {
		report.newLine();
		report.write("*********************************************************");
		report.newLine();
		report.write("***\t Converting for " + tableName + " \t***");
		report.newLine();
		report.write("*********************************************************");
		report.newLine();
	}

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
						for (TupleData tupleData : tuples) {
							OracleDecision.UPDATE.generateSQL(tupleData);
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

	protected Map<String, Integer> readCustHeaderMap(Row row) {
		List<String> data = readDataCells(row);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int index = 0;
		for (String string : data) {
			if (!map.containsKey(string)) {
				map.put(string, index++);
			}else {
				index++;
			}
		}
		return map;
	}
	
	protected abstract List<TupleData> processUpdate(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb);


	protected boolean postProcessData(TupleData tuple, BufferedWriter report, BufferedWriter out, int rowCnt, BufferedWriter errorReport) {
		if (StringUtils.isNotBlank(tuple.getErrorMessage())) {
			LOG.error(tuple.getErrorMessage());
			try {
				report.write("  ERROR - Row " + rowCnt + " : " + tuple.getErrorMessage());
				report.newLine();
				errorReport.write("  ERROR - Row " + rowCnt + " : " + tuple.getErrorMessage());
				errorReport.newLine();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		LOG.debug(tuple.getSqlString());
		try {
			out.write(tuple.getSqlString());
			out.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("error",e);
		}
		return true;
	}

	protected abstract List<TupleData> processDelete(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb);

	protected abstract List<TupleData> processInsert(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb);
	

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
	
	protected List<ReferenceData> getReferenceTableList(
			TableDescriptor tableDesc) {
		List<ReferenceData> refList = null;
		String tableName = tableDesc.getTableName();
		
		if ("krim_perm_tmpl_t".equalsIgnoreCase(tableName)) {
			refList = TableReference.PERM_TMPL.getReferences();
		}
		else if ("KRIM_TYP_ATTR_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.ROLE_ATTR.getReferences();
		}
		else if ("KRIM_ROLE_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.ROLE.getReferences();
		}
		else if ("KRIM_ROLE_MBR_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.ROLE_MBR.getReferences();
		}
		else if ("KRIM_PERM_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.PERM.getReferences();
		}
		else if ("KRIM_PERM_ATTR_DATA_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.PERM_ATTR.getReferences();
		}
		else if ("KRIM_ROLE_PERM_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.ROLE_PERM.getReferences();
		}
		else if ("KRIM_RSP_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.RSP.getReferences();
		}
		else if ("KRIM_ROLE_RSP_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.RSP.getReferences();
		}
		else if ("KRIM_ROLE_RSP_ACTN_T".equalsIgnoreCase(tableName)) {
			refList = TableReference.ROLE_RSP_ACT.getReferences();
		}
		return refList;
	}
	
	protected ExcelMetadata getExcelMetadata(
			TableDescriptor tableDesc) {
		String tableName = tableDesc.getTableName();
		
		if ("krim_perm_tmpl_t".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.PERM_TMPL;
		}
		else if ("KRIM_TYP_ATTR_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.ROLE_ATTR;
		}
		else if ("KRIM_ROLE_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.ROLE;
		}
		else if ("KRIM_ROLE_MBR_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.ROLE_MBR;
		}
		else if ("KRIM_PERM_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.PERM;
		}
		else if ("KRIM_PERM_ATTR_DATA_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.PERM_ATTR;
		}
		else if ("KRIM_ROLE_PERM_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.ROLE_PERM;
		}
		else if ("KRIM_RSP_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.RSP;
		}
		else if ("KRIM_RSP_ATTR_DATA_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.RSP_ATTR;
		}
		else if ("KRIM_ROLE_RSP_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.ROLE_RSP;
		}
		else if ("KRIM_ROLE_RSP_ACTN_T".equalsIgnoreCase(tableName)) {
			return ExcelMetadata.ROLE_RSP_ACT;
		}
		return null;
	}
	
	protected Map<String, String> getReferenceValueMap(TableDescriptor tableDesc,
			Map<String, Integer> headerMap, List<String> dataCols,
			DBMap targetDb) {
		Map<String, String> refValueMap = new HashMap<String, String>();
		List<ReferenceData> refList = getReferenceTableList(tableDesc);
		for (ReferenceData refData : refList) {
			Map<String, Integer> targetColTypes = getReferToColTypes(
					headerMap, dataCols, targetDb, refData);
			String refValue = getSelectSql(refData, targetColTypes);
			refValueMap.put(refData.getSelectReferByColName(), refValue);
		}
		return refValueMap;
	}

	protected Map<String, Integer> getReferToColTypes(
			Map<String, Integer> headerMap, List<String> dataCols,
			DBMap targetDb, ReferenceData refData) {
		String excelColValue;
		String colName;
		TableDescriptor targetTableDesc = targetDb.getTableDescriptor(refData.getTargetTableName());
		Map<String, Integer> targetColTypes = new HashMap<String, Integer>();
		
		for (String excelColName : refData.getExcelToTableCols().keySet()) {
			excelColValue = readColValue(excelColName,headerMap, dataCols);
			colName = refData.getExcelToTableCols().get(excelColName);
			refData.getSearchingCriterias().put(colName, excelColValue);
			targetColTypes.put(colName, targetTableDesc.getColumn(colName).getJdbcType());
		}
		return targetColTypes;
	}



	protected String getSelectSql(ReferenceData refData,
			Map<String, Integer> targetColTypes) {
		StringBuilder selectSql = new StringBuilder(OracleSqlConstant.SELECT_STATEMENT);
		
		selectSql.append(refData.getSelectReferToColName() + OracleSqlConstant.FROM_CLAUSE);
		
		selectSql.append(refData.getTargetTableName() + OracleSqlConstant.WHERE_CLAUSE);
		
		int counter = 1;
		for (String colName: refData.getSearchingCriterias().keySet()) {
			String colValue = refData.getSearchingCriterias().get(colName);
			colValue = ConverterUtil.escapeSpecialChar(colValue);
			selectSql.append(colName + OracleSqlConstant.EQUAL_SIGN);
			
			if (ConverterUtil.doesDataTypeRequireSingleQuote(targetColTypes.get(colName), colName, colValue)) {
				selectSql.append(OracleSqlConstant.SINGLE_QUOTE);
				selectSql.append(colValue);
				selectSql.append(OracleSqlConstant.SINGLE_QUOTE);
				selectSql.append(counter != refData.getSearchingCriterias().size() ? OracleSqlConstant.AND_CLAUSE : "");
			}
			else {
				selectSql.append(colValue+ (counter != refData.getSearchingCriterias().size() ? OracleSqlConstant.AND_CLAUSE : ""));
			}
			counter++;
		}
		
		return selectSql.toString();
	}


	protected String getValueFromExcelWithMismatchName(
			Map<String, Integer> headerMap, List<String> dataCols,
			ExcelMetadata excelMeta, String colName) {
		String colValue;
		String excelColName = "";
		if (excelMeta.getTableColMap().containsKey(colName)) {
			excelColName = excelMeta.getTableColMap().get(colName);
		}
		else {
			excelColName = colName;
		}
		colValue = readColValue(excelColName,headerMap, dataCols);
		colValue = ConverterUtil.escapeSpecialChar(colValue);
		return colValue;
	}
}
