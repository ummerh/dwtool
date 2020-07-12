package com.lndb.dwtool.erm.db;

import java.util.HashMap;
import java.util.Map;

public class PrimaryKey {

	private String tableName;
	private Map<String, Object> keyValues;
	
	public PrimaryKey(String tableName) {
		this.tableName = tableName;
		this.keyValues = new HashMap<String, Object>();
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public Map<String, Object> getKeyValues() {
		return keyValues;
	}

	public void setKeyValues(Map<String, Object> keyValues) {
		this.keyValues = keyValues;
	}

	public void addPrimaryKeyValues(String key, Object value) {
		this.keyValues.put(key, value);
	}
	
	public boolean equals(PrimaryKey anotherPrimaryKey) {
		if (! this.getTableName().equals(anotherPrimaryKey.getTableName())) {
			return false;
		}
		if (this.keyValues.size() != anotherPrimaryKey.getKeyValues().size()) {
			return false;
		}
		for (String key : this.keyValues.keySet()) {
			if (! anotherPrimaryKey.getKeyValues().containsKey(key)) {
				return false;
			}
			else {
				Object value = this.keyValues.get(key);
				if (!value.equals(anotherPrimaryKey.getKeyValues().get(key))) {
					return false;
				}
			}
		}
		return true;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
        for (String key : this.keyValues.keySet()) {
        	result.append(key).append("=");
        	result.append(this.keyValues.get(key)).append(", ");
        }
        int size = result.length();
        return result.substring(0,size - 2);
	}
}
