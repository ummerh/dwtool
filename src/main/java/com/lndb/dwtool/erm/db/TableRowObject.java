package com.lndb.dwtool.erm.db;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.ddl.DataDiffGenerator;

/**
 *  This class represents a row of data  
 *  that are completely missing from either the source
 *  database or from the target database table
 *
 */
public class TableRowObject {
    private String tableName;
    private Map<String, Object> attributes;
	private PrimaryKey primaryKey;
	
    public TableRowObject(String tableName) {
    	this.tableName = tableName;
    	this.attributes = new HashMap<String, Object>();
    	
    }
    
    public void addAttribute(String columnName, Object object) {
    	this.getAttributes().put(columnName, object);
    }

    public boolean equals(TableRowObject anObject) {
    	//First we check that the tableName of both are equal
        if (!this.tableName.equals(anObject.tableName)) {
        	return false;
        }
        //Now we check whether the primaryKey's key-value pairs are equal
        else if (!primaryKeyFieldsEqual(anObject)) {
        	return false;        	
        }
        //Now we check whether the attributes, i.e. all of the columns of this table
        //and their content are equal, excluding the excludedColumnNames
        else if (!attributesContentsEqual(anObject)) {
        	return false;
        }
        return true;
    }
    
    public boolean primaryKeyFieldsEqual(TableRowObject anObject) {
    	if (this.getPrimaryKey().getKeyValues().size() != anObject.getPrimaryKey().getKeyValues().size()) {
    		return false;
    	}
    	else {
    		for (String key : this.primaryKey.getKeyValues().keySet() ) {
    			if (!anObject.getPrimaryKey().getKeyValues().containsKey(key)) {
    				return false;
    			}
    			else {
    				Object value = this.primaryKey.getKeyValues().get(key);
    				if (!value.equals(anObject.getPrimaryKey().getKeyValues().get(key))) {
    					return false;
    				}
    			}
    		}
    	    return true;
    	}
    }


    private boolean attributesContentsEqual(TableRowObject anObject) {
    	if (this.getAttributes().size() != anObject.getAttributes().size()) {
    		return false;
    	}
    	for (String key : this.getAttributes().keySet()) {
    		//We only need to check the content if this is not part of excludedColumnNames.
    		if (!DataDiffGenerator.DATA_COMPARE_EXCLUDE_PATTERN.matcher(key).matches()) {
				Object value = this.getAttributes().get(key);
				Object anotherObjectValue = anObject.getAttributes().get(key);
				if ((value == null && anotherObjectValue != null) ||
				    (value != null && anotherObjectValue == null)) {
					return false;
				}
				if (value == null && anotherObjectValue == null) {
					//both are null, so they are equal
					continue;
				}
				if (!StringUtils.equals(value.toString().trim(), anotherObjectValue.toString().trim())) {
					return false;
				}
			}
    	}
    	return true;
    }
    
    public String getTableName() {
		return tableName;
	}

    public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}
    
	public String toString() {
		StringBuffer result = new StringBuffer();
        for (String key : this.attributes.keySet()) {
        	result.append(key).append("=");
        	result.append(this.attributes.get(key)).append(", ");
        }
        return result.toString();
	}
    
}
