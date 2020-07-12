package com.lndb.dwtool.erm.ddl;

import static com.lndb.dwtool.erm.ddl.Constraint.buildConstraintName;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.IndexInfo;
import com.lndb.dwtool.erm.RelationalMap;
import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.SequenceInfo;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.ojb.ClassDescriptor;
import com.lndb.dwtool.erm.ojb.OJBMap;

public class DDLGenerator {
	private OJBMap ojbMap;

	private DBMap dbMap;

	private static final String LINE_BREAK = System.getProperty("line.separator");

	private static final OracleDialect oracleDialect = new OracleDialect();

	private List<Constraint> constraints = new ArrayList<Constraint>();

	private List<String> constraintDDLs = new ArrayList<String>();

	private List<String> dbConstraintDDLs = new ArrayList<String>();

	private List<String> ojbContraintsDDLs = new ArrayList<String>();

	private List<IKeyFilter> filters = new ArrayList<IKeyFilter>();

	private SchemaJoinMetaData schemaMetaData;

	private int tableCount;

	private int dbRefCount;

	private int ojbRefCount;

	public DDLGenerator(OJBMap ojbMap, DBMap dbMap) {
		super();
		this.ojbMap = ojbMap;
		this.dbMap = dbMap;
		this.schemaMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
	}

	public OJBMap getOjbMap() {
		return ojbMap;
	}

	public void setOjbMap(OJBMap ojbMap) {
		this.ojbMap = ojbMap;
	}

	public RelationalMap getDbMap() {
		return dbMap;
	}

	public void setDbMap(DBMap dbMap) {
		this.dbMap = dbMap;
	}

	public String prepareDDL(String tableName) {
		String tblName = tableName.toUpperCase();
		ClassDescriptor classDescriptor = ojbMap.getClassDescriptor(tblName);
		TableDescriptor tableDescriptor = dbMap.getTableDescriptor(tblName);
		if (tableDescriptor == null) {
			return "";
		}
		if (classDescriptor == null) {
			System.out.println("INFO! OJB Mapping not found for " + tblName);
		}

		StringBuilder ddlBuf = new StringBuilder();
		ddlBuf.append("create table " + tableName);
		ddlBuf.append(LINE_BREAK);
		ddlBuf.append("(");
		ddlBuf.append(DDL.columnSpec(tableDescriptor));
		generatePKRelationshipsInline(tblName, ddlBuf);
		generateFKRelationshipsExternally(tblName);
		if (ddlBuf.charAt(ddlBuf.length() - 1) == ',') {
			ddlBuf.replace(ddlBuf.length() - 1, ddlBuf.length(), "");
		}
		ddlBuf.append(LINE_BREAK);
		ddlBuf.append(");");
		ddlBuf.append(LINE_BREAK);
		return ddlBuf.toString();
	}

	private void generatePKRelationshipsInline(String tblName, StringBuilder ddlBuf) {
		List<String> primaryKeys = dbMap.getPrimaryKeys(tblName);
		String prefix = null;
		String constraintName = "";
		if (primaryKeys == null || primaryKeys.isEmpty()) {
			primaryKeys = ojbMap.getPrimaryKeys(tblName);
			prefix = "O";
			constraintName = buildConstraintName(prefix, tblName, "PK", false);
		} else {
			constraintName = dbMap.getTableDescriptor(tblName).getPrimaryKeyName();
		}
		if (primaryKeys != null && !primaryKeys.isEmpty()) {
			this.constraints.add(new Constraint(constraintName, tblName));
			ddlBuf.append(",");
			ddlBuf.append(LINE_BREAK);
			ddlBuf.append(DDL.inlinePKConstraint(constraintName, primaryKeys));
		}
	}

	private void generateFKRelationshipsExternally(String tblName) {
		Integer counter = new Integer(0);
		List<ForeignKey> foreignKeys = dbMap.getForeignKeys(tblName);
		String prefix = null;
		counter = generateReferenceSQLExternally(tblName, foreignKeys, prefix, counter, false);
		foreignKeys = ojbMap.getForeignKeys(tblName);
		prefix = "O";
		counter = generateReferenceSQLExternally(tblName, foreignKeys, prefix, counter, true);
	}

	private Integer generateReferenceSQLExternally(String tblName, List<ForeignKey> foreignKeys, String prefix, Integer counter, boolean isOjbKey) {
		List<ForeignKey> generatedList = new ArrayList<ForeignKey>();
		Integer currCounter = counter;
		if (foreignKeys == null) {
			return currCounter;
		}
		String fkName = null;
		for (ForeignKey foreignKey : foreignKeys) {
			if (foreignKey.isValid() && !ignoreByFilter(foreignKey) && !schemaMetaData.isDuplicateFK(generatedList, foreignKey) && (!isOjbKey || !schemaMetaData.isDuplicateFK(foreignKey))) {
				if (isOjbKey) {
					this.ojbRefCount++;
					currCounter++;
					fkName = buildConstraintName(prefix, tblName, "FK", currCounter);
				} else {
					this.dbRefCount++;
					fkName = foreignKey.getName();
				}
				this.constraints.add(new Constraint(fkName, tblName));
				String alterSQL = DDL.addConstraintDDL(tblName, fkName, foreignKey);
				if (alterSQL.length() > 0) {
					generatedList.add(foreignKey);
					this.constraintDDLs.add(alterSQL.toString());
					if (isOjbKey) {
						this.ojbContraintsDDLs.add(alterSQL.toString());
					} else {
						this.dbConstraintDDLs.add(alterSQL.toString());
					}
				}
			}
		}
		return currCounter;
	}

	private boolean ignoreByFilter(ForeignKey foreignKey) {
		for (IKeyFilter filter : this.filters) {
			if (filter.ignoreKey(foreignKey)) {
				return true;
			}
		}
		return false;
	}

	public String getConstraintsDDL() {
		StringBuilder ddlBuf = new StringBuilder();
		for (String ddl : this.dbConstraintDDLs) {
			ddlBuf.append(LINE_BREAK);
			ddlBuf.append(ddl);
		}
		return ddlBuf.toString();
	}

	public void writeDropDbDDL(Writer result) throws Exception {
		BufferedWriter ddlFile = new BufferedWriter(result);
		try {
			Connection con = DatabaseConnection.newConnection(dbMap.getConnectionDetail());
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select table_name, constraint_name from user_constraints");
			while (rs != null && rs.next()) {
				String tblNm = rs.getString(1);
				if (tblNm.indexOf('$') == -1) {
					ddlFile.write("alter table " + tblNm + " disable constraint " + rs.getString(2) + " ;");
					ddlFile.newLine();
				}
			}
			rs.close();
			rs = stmt.executeQuery("select object_type, object_name from user_objects");
			while (rs != null && rs.next()) {
				String objNm = rs.getString(2);
				if (objNm.indexOf('$') == -1) {
					ddlFile.write(oracleDialect.dropObjectDDL(rs.getString(1), objNm));
					ddlFile.newLine();
				}
			}
		} finally {
			ddlFile.flush();
			ddlFile.close();
		}
	}

	public void writeSchemaDDL(Writer result) throws IOException {
		List<String> allTables = dbMap.getAllTables();
		BufferedWriter ddlFile = new BufferedWriter(result);
		try {
			for (String tableName : allTables) {
				this.tableCount++;
				ddlFile.write(this.prepareDDL(tableName));
				ddlFile.newLine();
			}
			ddlFile.write(getConstraintsDDL());
			ddlFile.newLine();

			ddlFile.write("--SEQUENCES");
			List<SequenceInfo> sequences = dbMap.getSequences();
			for (SequenceInfo sequenceInfo : sequences) {
				ddlFile.write(dbMap.getConnectionDetail().getDialect().createSequence(sequenceInfo));
				ddlFile.newLine();
			}

			ddlFile.write("--INDEXES");
			List<IndexInfo> indexes = dbMap.getAllIndexes();
			for (IndexInfo index : indexes) {
				if (index.isUnique()) {
					ddlFile.write(DDL.addUniqueDDL(index.getTableName(), index.getName(), index));
					ddlFile.newLine();
				} else {
					ddlFile.write(DDL.addIndexDDL(index.getTableName(), index.getName(), index));
					ddlFile.newLine();
				}
			}

			System.out.println("Total table count = " + this.tableCount);
			System.out.println("DB Reference count = " + this.dbRefCount);
			System.out.println("OJB Reference count = " + this.ojbRefCount);

		} finally {
			ddlFile.flush();
			ddlFile.close();
		}
	}

	public void writeOjbDDL(Writer result) throws IOException {
		List<String> allTables = dbMap.getAllTables();
		for (String tableName : allTables) {
			this.tableCount++;
			this.prepareDDL(tableName);
		}
		BufferedWriter writer = new BufferedWriter(result);
		for (String string : this.ojbContraintsDDLs) {
			writer.write(string);
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}

	public void writeDbConstraintsDDL(Writer result) throws IOException {
		List<String> allTables = dbMap.getAllTables();
		for (String tableName : allTables) {
			this.tableCount++;
			this.prepareDDL(tableName);
		}
		BufferedWriter writer = new BufferedWriter(result);
		for (String string : this.dbConstraintDDLs) {
			writer.write(string);
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}

	public void addFilter(IKeyFilter filter) {
		this.filters.add(filter);
	}

	public List<IKeyFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<IKeyFilter> filters) {
		this.filters = filters;
	}
}
