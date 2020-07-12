package com.lndb.dwtool.erm.dml.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TupleData {
	private String tableName;
	private Map<String, String> updatingColValMap;
	private Map<String, Integer> updatingColTypeMap;
	private List<String> updatingColumns;
	private Map<String, String> matchingColValMap;
	private Map<String, Integer> matchingColTypeMap;
	private List<String> matchingColumns;
	private String sqlString;
	private String errorMessage;
	private boolean useDefaultVersionNbr;
	private boolean useDefaultObjId;
	
	public TupleData() {
		updatingColValMap = new HashMap<String, String>();
		updatingColTypeMap = new HashMap<String, Integer>();
		updatingColumns = new ArrayList<String>();
		matchingColValMap = new HashMap<String, String>();
		matchingColTypeMap = new HashMap<String, Integer>();
		matchingColumns = new ArrayList<String>();
	}
	
	public TupleData(boolean useDefaultVersionNbr, boolean useDefaultObjId, String tableName) {
		this();
		this.useDefaultObjId = useDefaultObjId;
		this.useDefaultVersionNbr = useDefaultVersionNbr;
		this.tableName = tableName;
	}
	

	/**
	 * @return the useDefaultVersionNbr
	 */
	public boolean isUseDefaultVersionNbr() {
		return useDefaultVersionNbr;
	}

	/**
	 * @param useDefaultVersionNbr the useDefaultVersionNbr to set
	 */
	public void setUseDefaultVersionNbr(boolean useDefaultVersionNbr) {
		this.useDefaultVersionNbr = useDefaultVersionNbr;
	}


	/**
	 * @return the useDefaultObjId
	 */
	public boolean isUseDefaultObjId() {
		return useDefaultObjId;
	}

	/**
	 * @param useDefaultObjId the useDefaultObjId to set
	 */
	public void setUseDefaultObjId(boolean useDefaultObjId) {
		this.useDefaultObjId = useDefaultObjId;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
	
	/**
	 * @return the updatingColValMap
	 */
	public Map<String, String> getUpdatingColValMap() {
		return updatingColValMap;
	}

	/**
	 * @param updatingColValMap the updatingColValMap to set
	 */
	public void setUpdatingColValMap(Map<String, String> updatingColValMap) {
		this.updatingColValMap = updatingColValMap;
	}

	/**
	 * @return the updatingColTypeMap
	 */
	public Map<String, Integer> getUpdatingColTypeMap() {
		return updatingColTypeMap;
	}

	/**
	 * @param updatingColTypeMap the updatingColTypeMap to set
	 */
	public void setUpdatingColTypeMap(Map<String, Integer> updatingColTypeMap) {
		this.updatingColTypeMap = updatingColTypeMap;
	}

	/**
	 * @return the matchingColValMap
	 */
	public Map<String, String> getMatchingColValMap() {
		return matchingColValMap;
	}

	/**
	 * @param matchingColValMap the matchingColValMap to set
	 */
	public void setMatchingColValMap(Map<String, String> matchingColValMap) {
		this.matchingColValMap = matchingColValMap;
	}

	/**
	 * @return the matchingColTypeMap
	 */
	public Map<String, Integer> getMatchingColTypeMap() {
		return matchingColTypeMap;
	}

	/**
	 * @param matchingColTypeMap the matchingColTypeMap to set
	 */
	public void setMatchingColTypeMap(Map<String, Integer> matchingColTypeMap) {
		this.matchingColTypeMap = matchingColTypeMap;
	}

	/**
	 * @return the sqlString
	 */
	public String getSqlString() {
		return sqlString;
	}
	/**
	 * @param sqlString the sqlString to set
	 */
	public void setSqlString(String sqlString) {
		this.sqlString = sqlString;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the updatingColumns
	 */
	public List<String> getUpdatingColumns() {
		return updatingColumns;
	}

	/**
	 * @param updatingColumns the updatingColumns to set
	 */
	public void setUpdatingColumns(List<String> updatingColumns) {
		this.updatingColumns = updatingColumns;
	}

	/**
	 * @return the matchingColumns
	 */
	public List<String> getMatchingColumns() {
		return matchingColumns;
	}

	/**
	 * @param matchingColumns the matchingColumns to set
	 */
	public void setMatchingColumns(List<String> matchingColumns) {
		this.matchingColumns = matchingColumns;
	}
	
}
