package com.lndb.dwtool.erm.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.lndb.dwtool.erm.util.Configuration;

/**
 *  This class contains differences between
 *  data in some columns in the source table
 *  and in the target table, which share the
 *  same primary key values (represented by the
 *  primaryKeyString).
 */
public class TableRowObjectDiff {
	private TableRowObject sourceTableRowObject;
	private TableRowObject targetTableRowObject;
	private Map<String, List<Object>> diffAttributes;
	private String tableName;
	private String primaryKeyString;
	protected static final Pattern DATA_COMPARE_EXCLUDE_PATTERN = Pattern.compile(Configuration.getProperty("report.technical.columns.exclude.pattern"));
	
	public TableRowObjectDiff(TableRowObject sourceTableRowObject, TableRowObject targetTableRowObject, String primaryKeyString) {
		this.sourceTableRowObject = sourceTableRowObject;
		this.targetTableRowObject = targetTableRowObject;	
		this.setTableName(sourceTableRowObject.getTableName());
		populateDiffAttributes();
		this.primaryKeyString = primaryKeyString;
	}
	
	/**
	 *  Find out the exact differences between a row of data
	 *  in the source database and a row of data in the target database
	 *  where both source and target has the same values for the primary key
	 *  but the values of the other columns of those rows may be different,
	 *  then store the information in the diffAttributes Map.
	 */
	private void populateDiffAttributes() {
		diffAttributes = new HashMap<String, List<Object>>();
		for (String keySource : sourceTableRowObject.getAttributes().keySet()) {
			if (!DATA_COMPARE_EXCLUDE_PATTERN.matcher(keySource).matches()) {
				Object valueSource = sourceTableRowObject.getAttributes().get(keySource);
				Object valueTarget = targetTableRowObject.getAttributes().get(keySource);

				if (valueSource != null && valueTarget != null && !valueSource.equals(valueTarget)) {
					List values = new ArrayList();
					values.add(valueSource);
					values.add(valueTarget);
					diffAttributes.put(keySource, values);
				}
				else if ((valueSource == null && valueTarget != null) || (valueTarget == null && valueSource != null)) {
					List values = new ArrayList();
					values.add(valueSource);
					values.add(valueTarget);
					diffAttributes.put(keySource, values);
				}
			}
		}
	}
	
	public Map<String, List<Object>> getDiffAttributes() {
		return this.diffAttributes;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPrimaryKeyString() {
		return primaryKeyString;
	}

	public void setPrimaryKeyString(String primaryKeyString) {
		this.primaryKeyString = primaryKeyString;
	}

	public TableRowObject getSourceTableRowObject() {
		return sourceTableRowObject;
	}

	public void setSourceTableRowObject(TableRowObject sourceTableRowObject) {
		this.sourceTableRowObject = sourceTableRowObject;
	}

	public TableRowObject getTargetTableRowObject() {
		return targetTableRowObject;
	}

	public void setTargetTableRowObject(TableRowObject targetTableRowObject) {
		this.targetTableRowObject = targetTableRowObject;
	}
	
}
