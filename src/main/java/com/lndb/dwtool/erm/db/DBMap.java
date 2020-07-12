package com.lndb.dwtool.erm.db;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.IndexInfo;
import com.lndb.dwtool.erm.RelationalMap;
import com.lndb.dwtool.erm.SequenceInfo;
import com.lndb.dwtool.erm.ddl.OracleDialect;
import com.lndb.dwtool.erm.util.StatusMonitor;
import com.lndb.dwtool.erm.util.StopWatch;

public class DBMap extends RelationalMap {
	private ConnectionDetail connectionDetail;
	private Connection con;
	private Map<String, TableDescriptor> tableMap = new HashMap<String, TableDescriptor>();
	private Map<String, SequenceInfo> sequenceInfoMap = new HashMap<String, SequenceInfo>();
	private HashSet<String> contraintNames = new HashSet<String>();
	private List<IndexInfo> indexes = new ArrayList<IndexInfo>();

	// This will be set true if found very slow
	private boolean ignoreExportKeys;

	public DBMap() {
		super();
	}

	public DBMap(boolean loaded) {
		this();
		setLoaded(loaded);
	}

	public void add(TableDescriptor tableDescriptor) {
		tableMap.put(tableDescriptor.getTableName().toUpperCase(), tableDescriptor);
	}

	public void remove(String tableName) {
		tableMap.remove(tableName.toUpperCase());
	}

	public TableDescriptor getTableDescriptor(String tableName) {
		return this.tableMap.get(tableName.toUpperCase());
	}

	public void loadMap(ConnectionDetail connectionDetail) {
		StopWatch.start();
		String currTableName = null;
		try {
			StatusMonitor.removeStatus(connectionDetail.getName());
			StatusMonitor.removeProgressInd(connectionDetail.getName());
			this.connectionDetail = connectionDetail;
			con = DatabaseConnection.newConnection(connectionDetail);
			Statement stmt = con.createStatement();
			int pctUnit = 1;
			ResultSet rs = stmt.executeQuery(connectionDetail.getDialect().getTableCountSql(connectionDetail.getSchema()));
			if (rs != null && rs.next()) {
				pctUnit = rs.getInt(1) / 100;
			}
			if (pctUnit == 0) {
				pctUnit = 1;
			}
			rs.close();
			stmt.close();
			DatabaseMetaData dbMetaData = con.getMetaData();
			String schema = connectionDetail.getSchema().toUpperCase();
			String catalog = connectionDetail.getCatalog();
			ResultSet tables = dbMetaData.getTables(catalog, schema, null, new String[] { "TABLE" });
			TableDescriptor tableDescriptor = null;
			int progress = 0;
			while (tables.next()) {
				progress++;
				currTableName = tables.getString("TABLE_NAME");
				StatusMonitor.putStatus(connectionDetail.getName(), currTableName);
				if (!Pattern.matches(".*[~!@#$%^&*()-].*", currTableName)) {
					tableDescriptor = new TableDescriptor();
					loadPKCols(dbMetaData, schema, catalog, currTableName, tableDescriptor);
					tableDescriptor.setTableName(currTableName);
					loadColumnSpec(dbMetaData, catalog, schema, currTableName, tableDescriptor);
					loadImportKeys(dbMetaData, schema, catalog, currTableName, tableDescriptor);
					if (!OracleDialect.class.isAssignableFrom(this.connectionDetail.getDialect().getClass())) {
						//loadIndexInfoGeneric(dbMetaData, schema, catalog, currTableName, tableDescriptor);
					}
					// when loading is very time consuming, exported key loading
					// is ignored
					if (!ignoreExportKeys) {
						loadExportKeys(dbMetaData, schema, catalog, currTableName, tableDescriptor);
					}
					add(tableDescriptor);

				}
				if (progress % pctUnit == 0 && (progress / pctUnit) < 99) {
					StatusMonitor.putProgressInd(connectionDetail.getName(), (progress / pctUnit));
				}
			}
			StatusMonitor.putStatus(connectionDetail.getName(), "DONE");
			StatusMonitor.putProgressInd(connectionDetail.getName(), 100);
			tables.close();

			if (OracleDialect.class.isAssignableFrom(this.connectionDetail.getDialect().getClass())) {
				loadOracleIndexInfo();
				loadOracleSequenceInfo();
			}

			DatabaseConnection.release(con);
			this.loaded = true;
		} catch (Exception e) {
			System.out.println("FAILED AT " + currTableName);
			e.printStackTrace();
			StatusMonitor.putStatus(connectionDetail.getName(), "ERROR");
			throw new RuntimeException(e);
		}
		StopWatch.stop();
	}

	private void loadPKCols(DatabaseMetaData dbMetaData, String schema, String catalog, String currTableName, TableDescriptor tableDescriptor) throws SQLException {
		TreeMap<Integer, String> pkeys = new TreeMap<Integer, String>();
		String pkName = "";
		ResultSet pkRs = dbMetaData.getPrimaryKeys(catalog, schema, currTableName);
		while (pkRs.next()) {
			pkeys.put(pkRs.getInt("KEY_SEQ"), pkRs.getString("COLUMN_NAME").toUpperCase());
			pkName = pkRs.getString("PK_NAME").toUpperCase();
		}
		pkRs.close();
		tableDescriptor.setPrimaryKeyName(pkName);
		Set<Integer> keySet = pkeys.keySet();
		for (Integer key : keySet) {
			tableDescriptor.addPrimaryKey(pkeys.get(key));
		}
		addConstraintName(pkName);
	}

	private void loadIndexInfoGeneric(DatabaseMetaData dbMetaData, String schema, String catalog, String currTableName, TableDescriptor tableDescriptor) throws SQLException {
		ResultSet idxRs = dbMetaData.getIndexInfo(catalog, schema, currTableName, true, false);
		while (idxRs.next()) {
			String name = idxRs.getString("INDEX_NAME");
			Integer pos = idxRs.getInt("ORDINAL_POSITION");
			String col = idxRs.getString("COLUMN_NAME");
			if (name != null && col != null) {
				tableDescriptor.addIndexInfo(name.toUpperCase(), pos, col.toUpperCase(), (idxRs.getInt("NON_UNIQUE") == 0 ? true : false));
			}
			addConstraintName(name);
		}
		idxRs.close();
	}

	private void loadOracleIndexInfo() throws SQLException {
		Statement stmt = con.createStatement();
		ResultSet idxRs = stmt.executeQuery(" select a.index_name, a.table_name, a.uniqueness, b.column_name, b.column_position "
				+ "from user_indexes a, user_ind_columns b where a.index_name = b.index_name and a.table_name = b.table_name order by a.index_name, b.column_position");
		while (idxRs != null && idxRs.next()) {
			String name = idxRs.getString("index_name");
			Integer pos = idxRs.getInt("column_position");
			String col = idxRs.getString("column_name");
			if (name != null && col != null) {
				TableDescriptor tableDescriptor = getTableDescriptor(idxRs.getString("table_name"));
				if (tableDescriptor != null) {
					tableDescriptor.addIndexInfo(name.toUpperCase(), pos, col.toUpperCase(), (idxRs.getString("uniqueness").equals("UNIQUE") ? true : false));
				}
				addConstraintName(name);
			}
		}
		idxRs.close();
		stmt.close();
	}

	private void loadOracleSequenceInfo() throws SQLException {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select sequence_name,min_value,max_value,increment_by, cycle_flag, order_flag, cache_size, last_number from user_sequences");
		while (rs != null && rs.next()) {
			addSequenceInfo(rs.getString("sequence_name").toUpperCase(), rs.getDouble("min_value"), rs.getDouble("max_value"), rs.getDouble("increment_by"), rs.getString("cycle_flag"),
					rs.getString("order_flag"), rs.getInt("cache_size"), rs.getDouble("last_number"));
		}
		rs.close();
		stmt.close();
	}

	private void loadImportKeys(DatabaseMetaData dbMetaData, String schema, String catalog, String currTableName, TableDescriptor tableDescriptor) throws SQLException {
		ResultSet refRs = dbMetaData.getImportedKeys(catalog, schema, currTableName);
		while (refRs.next()) {
			String name = refRs.getString("FK_NAME").toUpperCase();
			tableDescriptor.addImportKey(name, refRs.getString("FKTABLE_NAME").toUpperCase(), refRs.getString("FKCOLUMN_NAME").toUpperCase(), refRs.getString("PKTABLE_NAME").toUpperCase(), refRs
					.getString("PKCOLUMN_NAME").toUpperCase(), refRs.getInt("KEY_SEQ"));
			addConstraintName(name);
		}
		refRs.close();
	}

	private void loadExportKeys(DatabaseMetaData dbMetaData, String schema, String catalog, String currTableName, TableDescriptor tableDescriptor) throws SQLException {
		long start = System.currentTimeMillis();
		ResultSet refRs = dbMetaData.getExportedKeys(catalog, schema, currTableName);
		while (refRs.next()) {
			String name = refRs.getString("FK_NAME").toUpperCase();
			tableDescriptor.addExportKey(name, refRs.getString("FKTABLE_NAME").toUpperCase(), refRs.getString("FKCOLUMN_NAME").toUpperCase(), refRs.getString("PKTABLE_NAME").toUpperCase(), refRs
					.getString("PKCOLUMN_NAME").toUpperCase(), refRs.getInt("KEY_SEQ"));
			addConstraintName(name);
		}
		refRs.close();
		long end = System.currentTimeMillis();
		if ((end - start) > 50) {
			// avoid performance hit, ignore exported key list
			this.ignoreExportKeys = true;
		}
	}

	private void loadColumnSpec(DatabaseMetaData dbMetaData, String catalog, String schema, String currTableName, TableDescriptor tableDescriptor) throws SQLException {
		ColumnDescriptor colDescriptor;
		ResultSet columns = dbMetaData.getColumns(catalog, schema, currTableName, null);
		while (columns.next()) {
			colDescriptor = new ColumnDescriptor();
			colDescriptor.setName(columns.getString("COLUMN_NAME"));
			colDescriptor.setTypeName(columns.getString("TYPE_NAME"));
			colDescriptor.setSize(columns.getInt("COLUMN_SIZE"));
			colDescriptor.setDecimalDigits(columns.getInt("DECIMAL_DIGITS"));
			colDescriptor.setNullable(columns.getBoolean("NULLABLE"));
			colDescriptor.setJdbcType(columns.getInt("DATA_TYPE"));
			colDescriptor.setColDefault(columns.getString("COLUMN_DEF"));
			colDescriptor.setOrdinalPosition(columns.getInt("ORDINAL_POSITION"));
			tableDescriptor.addColumn(colDescriptor);
		}
		columns.close();
	}

	public List<String> getAllTables() {
		ArrayList<String> list = new ArrayList<String>(tableMap.keySet());
		Collections.sort(list);
		return list;
	}

	public List<TableDescriptor> getAllTableDescriptors() {
		List<TableDescriptor> all = new ArrayList<TableDescriptor>();
		all.addAll(tableMap.values());
		Collections.sort(all, new Comparator<TableDescriptor>() {
			public int compare(TableDescriptor o1, TableDescriptor o2) {
				return o1.getTableName().compareTo(o2.getTableName());
			}

		});
		return all;
	}

	public List<ForeignKey> getForeignKeys(String tblName) {
		TableDescriptor descriptor = this.tableMap.get(tblName.toUpperCase());
		if (descriptor == null) {
			return null;
		}
		return descriptor.getForeignKeys();
	}

	public void writeMap(Writer out) throws IOException {
		if (!loaded) {
			throw new RuntimeException("DB Map is not loaded, invoke loadMap() method before attempting to print");
		}
		BufferedWriter writer = new BufferedWriter(out);
		try {
			TreeSet<String> tables = new TreeSet<String>(this.getAllTables());
			TableDescriptor tableDescriptor = null;
			for (String tableName : tables) {
				tableDescriptor = this.getTableDescriptor(tableName);
				writer.write("TABLE: " + tableName);
				writer.newLine();
				writer.write("Keys: " + this.getPrimaryKeys(tableName));
				writer.newLine();
				writer.newLine();
				List<ColumnDescriptor> columns = tableDescriptor.getColumns();
				for (ColumnDescriptor columnDescriptor : columns) {
					writer.write(columnDescriptor.getName() + " " + columnDescriptor.getTypeName() + "(" + columnDescriptor.getSize() + ")");
					writer.newLine();
				}
				writer.write("*************************");
				writer.newLine();
				writer.newLine();
				printFKRelations(writer, tableName);
				writer.write("*************************");
				writer.newLine();
				writer.newLine();
				printSelfReferences(writer, tableName);
				writer.write("*************************");
				writer.newLine();
				writer.newLine();
				printInverseReferences(writer, tableName);
				writer.write("*************************");
				writer.newLine();
				writer.newLine();
			}
			writer.write("end");
			writer.newLine();
		} finally {
			writer.flush();
			writer.close();
		}
	}

	public List<String> getPrimaryKeys(String tblName) {
		TableDescriptor tableDescriptor = this.tableMap.get(tblName.toUpperCase());
		if (tableDescriptor == null) {
			return null;
		}
		return tableDescriptor.getPrimaryKeys();
	}

	public List<ForeignKey> getExportedKeys(String tableName) {
		TableDescriptor tableDescriptor = this.tableMap.get(tableName.toUpperCase());
		if (tableDescriptor == null) {
			return null;
		}
		return tableDescriptor.getExportedKeys();
	}

	public ConnectionDetail getConnectionDetail() {
		return connectionDetail;
	}

	public void setConnectionDetail(ConnectionDetail connectionDetail) {
		this.connectionDetail = connectionDetail;
	}

	public void addSequenceInfo(String sequenceName, double minValue, double maxValue, double incrementBy, String cycleFlag, String orderFlag, int cacheSize, double lastNumber) {
		SequenceInfo info = new SequenceInfo();
		info.setSequenceName(sequenceName);
		info.setMinValue(minValue);
		info.setMaxValue(maxValue);
		info.setIncrementBy(incrementBy);
		info.setCycleFlag(cycleFlag);
		info.setOrderFlag(orderFlag);
		info.setCacheSize(cacheSize);
		info.setLastNumber(lastNumber);
		sequenceInfoMap.put(sequenceName, info);
	}

	public List<SequenceInfo> getSequences() {
		ArrayList<SequenceInfo> sequences = new ArrayList<SequenceInfo>();
		sequences.addAll(this.sequenceInfoMap.values());
		return sequences;
	}

	public SequenceInfo getSequenceInfo(String seqName) {
		return this.sequenceInfoMap.get(seqName);
	}

	public boolean isContraintNameExists(String consName) {
		return this.contraintNames.contains(consName.toUpperCase());
	}

	public void addConstraintName(String name) {
		this.contraintNames.add(name);
	}

	public List<IndexInfo> getAllIndexes() {
		if (!this.indexes.isEmpty()) {
			return this.indexes;
		}
		TableDescriptor baseTbl;
		for (String baseTblName : getAllTables()) {
			baseTbl = getTableDescriptor(baseTblName);
			if (baseTbl != null) {
				this.indexes.addAll(baseTbl.getIndexes());
			}
		}
		return this.indexes;
	}
}
