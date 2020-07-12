package com.lndb.dwtool.erm.dml;

public interface OracleSqlConstant {
	public static final String INSERT_STATEMENT = "INSERT INTO ";
	public static final String UPDATE_STATEMENT = "UPDATE ";
	public static final String DELETE_STATEMENT = "DELETE FROM ";
	public static final String SELECT_STATEMENT = "SELECT ";
	public static final String LEFT_PARENTHESE = " ( ";
	public static final String RIGHT_PARENTHESE = " ) ";
	public static final String SEPARATOR = " , ";
	public static final String VALUES_CLAUSE = " VALUES ";
	public static final String SET_CLAUSE = " SET ";
	public static final String WHERE_CLAUSE = " WHERE ";
	public static final String EQUAL_SIGN = " = ";
	public static final String END_OF_SQL_LINE = " ;";
	public static final String SINGLE_QUOTE = "'";
	public static final String AND_CLAUSE = " AND ";
	public static final String FROM_CLAUSE = " FROM ";
	
	public static class ErrorMessage {
		public static final String NO_TABLE_NAME = "Table name is missing.";
		public static final String NO_COLUMN_NAME = "Column name is missing.";
		public static final String NO_DATA_VALUE = "Data value is missing.";
		public static final String MISMATCH_COLUMN_AND_VALUE = "Number of columns does not match values.";
		public static final String NO_MATCHING_COLUMN = "Matching column name is missing. Change will apply to whole table!";
		public static final String NO_MATCHING_VALUE = "Matching value is missing.";
		public static final String MISMATCH_MATCHING_COLUMN_AND_VALUE = "Number of matching columns does not match values.";
	}
	
	public static class UserDecision {
		public static final String INSERT = "INSERT";
		public static final String DELETE = "DELETE";
		public static final String UPDATE = "UPDATE";
		public static final String NO_CHANGE = "NO CHANGE";
	}
	
	public static final String OBJ_ID = "OBJ_ID";
	public static final String VER_NBR = "VER_NBR";
	public static final String LAST_UPDATE_PATTERN = ".*LAST_UPDT_DT$|.*LSTUPDT_DT$";
	public static final String ACTV_IND = "ACTV_IND";
	
	public static final String SYSDATE = "SYSDATE";
	public static final String SYS_GUID = "SYS_GUID()";
	public static final String DEFAULT_VERSION_NBR = "1";
	public static final String ACTIVE_YES = "Y";
}
