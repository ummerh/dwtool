package com.lndb.dwtool.erm.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.IndexInfo;
import com.lndb.dwtool.erm.util.Configuration;

public class TableDescriptor {
	protected static Pattern EXCLUDE_FROM_DATA_COMPARE_PATTERN = null;
	protected static Pattern EXCLUDE_TBL_FROM_DATA_COMPARE_PATTERN = null;
	static {
		try {
			EXCLUDE_FROM_DATA_COMPARE_PATTERN = Pattern.compile(Configuration.getProperty("data.compare.exclude.pattern"));
			EXCLUDE_TBL_FROM_DATA_COMPARE_PATTERN = Pattern.compile(Configuration.getProperty("data.compare.exclude.table.pattern"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static Comparator<ColumnDescriptor> c = new Comparator<ColumnDescriptor>() {
		public int compare(ColumnDescriptor o1, ColumnDescriptor o2) {
			return o1.getOrdinalPosition().compareTo(o2.getOrdinalPosition());
		}
	};
	private String tableName;
	private String schema;
	private List<ColumnDescriptor> columns = new ArrayList<ColumnDescriptor>();
	private Map<String, ColumnDescriptor> columnMap = new HashMap<String, ColumnDescriptor>();
	private Map<String, ForeignKey> importKeyMap = new HashMap<String, ForeignKey>();
	private Map<String, ForeignKey> exportKeyMap = new HashMap<String, ForeignKey>();
	private List<String> primaryKeys = new ArrayList<String>();
	private String primaryKeyName;
	private boolean sorted;
	private Map<String, IndexInfo> indexInfoMap = new HashMap<String, IndexInfo>();
	private HashSet<String> contraintNames = new HashSet<String>();

	public String getPrimaryKeyName() {
		return primaryKeyName;
	}

	public void setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
	}

	public void addColumn(ColumnDescriptor column) {
		this.columns.add(column);
		columnMap.put(column.getName(), column);
	}

	public String getTableName() {
		if (tableName != null) {
			return tableName.toUpperCase();
		}
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public List<ColumnDescriptor> getColumns() {
		synchronized (columns) {
			if (!sorted) {
				Collections.sort(columns, c);
				sorted = true;
			}
		}
		return columns;
	}

	public void setColumns(List<ColumnDescriptor> columns) {
		this.columns = columns;
		if (columns != null) {
			for (ColumnDescriptor column : columns) {
				columnMap.put(column.getName(), column);
			}
		}
	}

	public void addImportKey(String fkName, String fkTable, String fkColumn, String pkTable, String pkColumn, Integer seq) {
		String fkUpperName = fkName.toUpperCase();
		this.contraintNames.add(fkUpperName);
		ForeignKey fKey = this.importKeyMap.get(fkUpperName);
		if (fKey == null) {
			fKey = new ForeignKey();
			fKey.setName(fkUpperName);
			fKey.setReferByTable(fkTable);
			fKey.setReferToTable(pkTable);
			this.importKeyMap.put(fkUpperName, fKey);
		}
		fKey.addReferMapping(fkColumn, pkColumn);
		fKey.addReferByCol(seq, fkColumn);
		fKey.addReferToCol(seq, pkColumn);
	}

	public void addImportKey(ForeignKey fKey) {
		this.contraintNames.add(fKey.getName().toUpperCase());
		this.importKeyMap.put(fKey.getName().toUpperCase(), fKey);
	}

	public void removeImportKey(ForeignKey fKey) {
		this.contraintNames.remove(fKey.getName().toUpperCase());
		this.importKeyMap.remove(fKey.getName().toUpperCase());
	}

	public void addExportKey(String fkName, String fkTable, String fkColumn, String pkTable, String pkColumn, Integer seq) {
		this.contraintNames.add(fkName.toUpperCase());
		ForeignKey fKey = this.exportKeyMap.get(fkName.toUpperCase());
		if (fKey == null) {
			fKey = new ForeignKey();
			fKey.setName(fkName);
			fKey.setReferByTable(fkTable);
			fKey.setReferToTable(pkTable);
			this.exportKeyMap.put(fkName.toUpperCase(), fKey);
		}
		fKey.addReferMapping(fkColumn, pkColumn);
		fKey.addReferByCol(seq, fkColumn);
		fKey.addReferToCol(seq, pkColumn);
	}

	public List<ForeignKey> getForeignKeys() {
		List<ForeignKey> keys = new ArrayList<ForeignKey>();
		keys.addAll(this.importKeyMap.values());
		return keys;
	}

	public ForeignKey getForeignKey(String fkName) {
		return this.importKeyMap.get(fkName.toUpperCase());
	}

	public void addPrimaryKey(String col) {
		if (!this.primaryKeys.contains(col)) {
			this.primaryKeys.add(col);
		}
	}

	public List<String> getPrimaryKeys() {
		return primaryKeys;
	}

	public void setPrimaryKeys(List<String> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	public List<ForeignKey> getExportedKeys() {
		return new ArrayList<ForeignKey>(this.exportKeyMap.values());
	}

	public boolean isColsMatch(TableDescriptor target) {
		List<ColumnDescriptor> tgtCols = target.getColumns();
		List<ColumnDescriptor> srcCols = this.getColumns();
		if (srcCols.size() != tgtCols.size()) {
			return false;
		}
		for (ColumnDescriptor tgtCol : tgtCols) {
			ColumnDescriptor srcCol = getColumn(tgtCol.getName());
			if (!tgtCol.isSame(srcCol)) {
				return false;
			}
		}
		// find if PK is same
		List<String> tgtKeys = target.getPrimaryKeys();
		List<String> srcKeys = getPrimaryKeys();
		if (tgtKeys.size() != srcKeys.size()) {
			return false;
		}
		for (String pk : tgtKeys) {
			if (!srcKeys.contains(pk)) {
				return false;
			}
		}
		return true;
	}

	public ColumnDescriptor getColumn(String name) {
		return this.columnMap.get(name.toUpperCase());
	}

	public boolean contains_OBJ_ID() {
		for (ColumnDescriptor ds : this.columns) {
			if ("OBJ_ID".equalsIgnoreCase(ds.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean contains_VER_NBR() {
		for (ColumnDescriptor ds : this.columns) {
			if ("VER_NBR".equalsIgnoreCase(ds.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean isMemberOfPK(String columnName) {
		if (this.primaryKeys == null || this.primaryKeys.isEmpty()) {
			return false;
		}
		for (String pk : this.primaryKeys) {
			if (pk.equalsIgnoreCase(columnName)) {
				return true;
			}
		}
		return false;
	}

	public boolean isMemberOfFK(String columnName) {
		List<ForeignKey> foreignKeys = getForeignKeys();
		for (ForeignKey foreignKey : foreignKeys) {
			if (foreignKey.getReferByCols().contains(columnName)) {
				return true;
			}
		}
		return false;
	}

	public ForeignKey getMemberFK(String columnName) {
		List<ForeignKey> foreignKeys = getForeignKeys();
		for (ForeignKey foreignKey : foreignKeys) {
			if (foreignKey.getReferByCols().contains(columnName)) {
				return foreignKey;
			}
		}
		return null;
	}

	public boolean displayReference(String fkName, String columnName) {
		boolean isMember = false;
		HashSet<String> curr = new HashSet<String>();
		ForeignKey foreignKey = this.importKeyMap.get(fkName);
		List<String> referByCols = foreignKey.getReferByCols();
		for (ColumnDescriptor col : this.columns) {
			String colNm = col.getName();
			if (referByCols.contains(colNm)) {
				curr.add(colNm);
				if (columnName.equalsIgnoreCase(colNm)) {
					isMember = true;
					break;
				}
			}
		}
		return referByCols.size() > 0 && referByCols.size() == curr.size() && isMember;
	}

	public boolean displayReference(String columnName) {
		Set<String> fks = this.importKeyMap.keySet();
		if (fks != null && !fks.isEmpty()) {
			for (String foreignKey : fks) {
				if (displayReference(foreignKey, columnName)) {
					return true;
				}
			}
		}
		return false;
	}

	public ForeignKey matchingFK(String columnName) {
		Set<String> fks = this.importKeyMap.keySet();
		if (fks != null && !fks.isEmpty()) {
			for (String foreignKey : fks) {
				if (displayReference(foreignKey, columnName)) {
					return this.importKeyMap.get(foreignKey);
				}
			}
		}
		return null;
	}

	public boolean excludeFromDataCompare() {
		if (EXCLUDE_TBL_FROM_DATA_COMPARE_PATTERN.matcher(getTableName()).matches()) {
			return true;
		}
		for (ColumnDescriptor cd : this.columns) {
			if (EXCLUDE_FROM_DATA_COMPARE_PATTERN.matcher(cd.getName()).matches()) {
				// ignore technical columns
				return true;
			}
		}
		return false;
	}

	public void addIndexInfo(String indexName, Integer ordinalPos, String column, boolean unique) {
		this.contraintNames.add(indexName);
		// ignore FKs and PK
		if (getForeignKey(indexName) != null || indexName.equalsIgnoreCase(getPrimaryKeyName())) {
			return;
		}
		IndexInfo indx = this.indexInfoMap.get(indexName);
		if (indx == null) {
			indx = new IndexInfo();
			indx.setTableName(getTableName());
			indx.setName(indexName);
			indx.setUnique(unique);
			this.indexInfoMap.put(indexName, indx);
		}
		indx.getColumnMap().put(ordinalPos, column);
	}

	public List<IndexInfo> getIndexes() {
		ArrayList<IndexInfo> indexes = new ArrayList<IndexInfo>();
		indexes.addAll(this.indexInfoMap.values());
		return indexes;
	}

	public boolean isContraintNameExists(String consName) {
		return this.contraintNames.contains(consName.toUpperCase());
	}

	public List<ForeignKey> getExportedKeys(String columnName) {
		ArrayList<ForeignKey> matches = new ArrayList<ForeignKey>();
		Collection<ForeignKey> values = this.exportKeyMap.values();
		for (ForeignKey foreignKey : values) {
			List<String> referToCols = foreignKey.getReferToCols();
			if (referToCols.contains(columnName)) {
				matches.add(foreignKey);
			}
		}
		return matches;
	}
}
