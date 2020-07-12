package com.lndb.dwtool.erm.dml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.dml.data.ExcelMetadata;
import com.lndb.dwtool.erm.dml.data.RoleMemberExcel;
import com.lndb.dwtool.erm.dml.data.TableReference;
import com.lndb.dwtool.erm.dml.data.TupleData;
import com.lndb.dwtool.erm.dml.data.TableReference.ReferenceData;

/**
 * This class is an implementation class for converting excel data to SQL script
 * 
 * @author ZHANGMA
 * 
 */
public class RoleConverter extends ComplexConverter implements DMLConverterInterface{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(RoleConverter.class);

	private static Map<String, String> roleMemberTypes = new HashMap<String, String>();
	static {
		roleMemberTypes.put("ROLE", "R");
		roleMemberTypes.put("PRICIPAL", "P");
	}
	
	@Override
	protected List<TupleData> processUpdate(TableDescriptor roleTableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		if (roleTableDesc == null) {
			roleTableDesc = targetDb.getTableDescriptor("KRIM_ROLE_T");
		}
		// process krim_role_t
		List<TupleData> results = super.processUpdate(roleTableDesc, headerMap, dataCols, targetDb);
		
		String colValue = "";
		int colType = -1;
		String colName = "";
		TupleData tuple;
		
		// process for krim_role_mbr_t
		TableDescriptor tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_MBR_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		
		List<RoleMemberExcel> roleMembers = parseRoleMemberData(headerMap, dataCols, targetDb);
		if (roleMembers == null || roleMembers.isEmpty()) {
			return results;
		}
		// process krim_role_mbr_t
		Map<String, String> roleRefValueMap = getReferenceValueMap(tableDesc,
				headerMap, dataCols, targetDb);
		String roleId = roleRefValueMap.get("ROLE_ID");
		for (RoleMemberExcel roleMbr: roleMembers) {
			roleMbr.setRoleId(OracleSqlConstant.LEFT_PARENTHESE + roleId + OracleSqlConstant.RIGHT_PARENTHESE);
		}
		
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		for (RoleMemberExcel roleMbr: roleMembers) {
			tuple = new TupleData(true, true, tableDesc.getTableName());
			results.add(tuple);
			for (String key : primaryKeys) {
				tuple.getMatchingColumns().add(key);
				if (tableDesc.getColumn(key) != null) {
					colType = tableDesc.getColumn(key).getJdbcType();
				}
				
				colValue = getValueFromExcel(RoleMemberExcel.class, roleMbr, excelMeta, key);
				tuple.getMatchingColValMap().put(key, colValue);
				tuple.getMatchingColTypeMap().put(key, colType);
			}
				
			List<ColumnDescriptor> columns = ConverterUtil.getSortedColumns(tableDesc);
			for (ColumnDescriptor columnDescriptor : columns) {
				colName = columnDescriptor.getName();
				if (!OracleSqlConstant.OBJ_ID.equalsIgnoreCase(colName) && !OracleSqlConstant.VER_NBR.equalsIgnoreCase(colName) && !tuple.getMatchingColValMap().containsKey(colName)) {
					if (tableDesc.getColumn(colName) != null) {
						colType = tableDesc.getColumn(colName).getJdbcType();
					}
					
					colValue = getValueFromExcel(RoleMemberExcel.class, roleMbr, excelMeta, colName);
					if (colValue != null) {
						tuple.getUpdatingColumns().add(colName);
						tuple.getUpdatingColValMap().put(colName, colValue);
						tuple.getUpdatingColTypeMap().put(colName, colType);
					}
				}
			}
		}
		return results;
	}


	private List<RoleMemberExcel> parseRoleMemberData(Map<String, Integer> headerMap,
			List<String> dataCols, DBMap targetDb) {
		List<RoleMemberExcel> results = new ArrayList<RoleMemberExcel>();
		
		String colValue = readColValue("MEMBERS",headerMap, dataCols);
		
		if (StringUtils.isBlank(colValue)) {
			return results;
		}
		
		colValue = StringUtils.substringAfter(colValue, "[");
		colValue = StringUtils.substringBeforeLast(colValue, "]");
		
		String[] members = colValue.split("]");
		
		String type;
		String value;
		for (String mem : members) {
			if (mem.startsWith(",")) {
				mem = StringUtils.substringAfter(mem, ", ");
			}
			RoleMemberExcel roleMember = new RoleMemberExcel();
			results.add(roleMember);
			type = StringUtils.trim(StringUtils.substringBefore(mem, "["));
			
			if (roleMemberTypes.containsKey(StringUtils.upperCase(type))) {
				roleMember.setMemberTypeCode(roleMemberTypes.get(StringUtils.upperCase(type)));
			}
			
			value = StringUtils.substringAfter(mem, "[");
			
			String[] memberDetails = value.split(",");
			
			Map<String, String> memberValueMap = new HashMap<String, String>();
			
			for (String detail : memberDetails) {
				String detailType = StringUtils.trim(StringUtils.substringBefore(detail, "="));
				String detailValue = StringUtils.trim(StringUtils.substringAfter(detail, "="));
				memberValueMap.put(detailType, detailValue);
			}
			
			roleMember.setRoleMemberId(memberValueMap.get("role_mbr_id"));
			ReferenceData roleRefData = TableReference.ROLE_MBR.getReferences().get(0);
			TableDescriptor targetTableDesc = targetDb.getTableDescriptor(roleRefData.getTargetTableName());
			Map<String, Integer> targetColTypes = new HashMap<String, Integer>();
			
			for (String excelColName : roleRefData.getExcelToTableCols().keySet()) {
				String excelColValue = memberValueMap.get(excelColName);
				String colName = roleRefData.getExcelToTableCols().get(excelColName);
				roleRefData.getSearchingCriterias().put(colName, excelColValue);
				targetColTypes.put(colName, targetTableDesc.getColumn(colName).getJdbcType());
			}
			
			String memberIdSql = OracleSqlConstant.LEFT_PARENTHESE + getSelectSql(roleRefData, targetColTypes) + OracleSqlConstant.RIGHT_PARENTHESE;
			roleMember.setMemberId(memberIdSql);
			
		}
		return results;
	}


	@Override
	protected List<TupleData> processDelete(TableDescriptor roleTableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		// process krim_role_t
		List<TupleData> results = super.processDelete(roleTableDesc, headerMap, dataCols, targetDb);
		TableDescriptor tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_MBR_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		List<RoleMemberExcel> roleMembers = parseRoleMemberData(headerMap, dataCols, targetDb);
		if (roleMembers == null || roleMembers.isEmpty()) {
			return results;
		}
		// process krim_role_mbr_t
		List<String> primaryKeys = tableDesc.getPrimaryKeys();
		
		int colType = -1;
		for (RoleMemberExcel roleMember : roleMembers) {
			TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
			results.add(tuple);
			for (String key : primaryKeys) {
				tuple.getMatchingColumns().add(key);
				if (tableDesc.getColumn(key) != null) {
					colType = tableDesc.getColumn(key).getJdbcType();
				}
				String colValue = getValueFromExcel(RoleMemberExcel.class, roleMember, excelMeta, key);
				tuple.getMatchingColValMap().put(key, colValue);
				tuple.getMatchingColTypeMap().put(key, colType);
			}
		}
		return results;
	}

	@Override
	protected List<TupleData> processInsert(TableDescriptor roleTableDesc,
			Map<String, Integer> headerMap, List<String> dataCols, DBMap targetDb) {
		if (roleTableDesc == null) {
			roleTableDesc = targetDb.getTableDescriptor("KRIM_ROLE_T");
		}
		// process krim_role_t
		List<TupleData> results = super.processInsert(roleTableDesc, headerMap, dataCols, targetDb);

		TableDescriptor tableDesc = targetDb.getTableDescriptor("KRIM_ROLE_MBR_T");
		ExcelMetadata excelMeta = getExcelMetadata(tableDesc);
		List<RoleMemberExcel> roleMembers = parseRoleMemberData(headerMap, dataCols, targetDb);
		if (roleMembers == null || roleMembers.isEmpty()) {
			return results;
		}
		// process krim_role_mbr_t
		Map<String, String> roleRefValueMap = getReferenceValueMap(tableDesc,
				headerMap, dataCols, targetDb);
		String roleId = roleRefValueMap.get("ROLE_ID");
		for (RoleMemberExcel roleMbr: roleMembers) {
			roleMbr.setRoleId(OracleSqlConstant.LEFT_PARENTHESE + roleId + OracleSqlConstant.RIGHT_PARENTHESE);
			roleMbr.setActiveFromDate(OracleSqlConstant.SYSDATE);
		}
		
		int colType = -1;
		String colValue = "";
		for (RoleMemberExcel roleMbr : roleMembers) {
			TupleData tuple = new TupleData(true, true, tableDesc.getTableName());
			results.add(tuple);
			List<ColumnDescriptor> columns = tableDesc.getColumns();
			for (ColumnDescriptor columnDescriptor : columns) {
				String colName = columnDescriptor.getName();
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
					
					if (OracleSqlConstant.ACTV_IND.equalsIgnoreCase(colName)) {
						colValue = OracleSqlConstant.ACTIVE_YES;
					}
					colValue = getValueFromExcel(RoleMemberExcel.class, roleMbr, excelMeta, colName);
					if (colValue != null) {
						tuple.getUpdatingColumns().add(colName);
						tuple.getUpdatingColValMap().put(colName, colValue);
						tuple.getUpdatingColTypeMap().put(colName, colType);
					}
				}
			}
		}
		return results;
	}
	
	
}
