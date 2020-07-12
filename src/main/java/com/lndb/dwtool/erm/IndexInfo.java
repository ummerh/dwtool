package com.lndb.dwtool.erm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class IndexInfo {
	private String tableName;
	private String name;
	private Map<Integer, String> columnMap = new TreeMap<Integer, String>();
	private boolean unique;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Integer, String> getColumnMap() {
		return columnMap;
	}

	public void setColumnMap(Map<Integer, String> columns) {
		this.columnMap = columns;
	}

	public boolean isSame(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexInfo other = (IndexInfo) obj;
		if (unique != other.unique)
			return false;
		if (columnMap == null) {
			if (other.columnMap != null)
				return false;
		} else if (other.columnMap != null) {
			Set<Integer> keys = this.columnMap.keySet();
			for (Integer integer : keys) {
				if (!this.columnMap.get(integer).equalsIgnoreCase(other.columnMap.get(integer))) {
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

	public List<String> getColumns() {
		ArrayList<String> cols = new ArrayList<String>();
		cols.addAll(this.columnMap.values());
		return cols;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

}
