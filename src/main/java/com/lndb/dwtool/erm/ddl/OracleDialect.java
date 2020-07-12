package com.lndb.dwtool.erm.ddl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.IndexInfo;
import com.lndb.dwtool.erm.SequenceInfo;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.util.StringUtil;

public class OracleDialect implements Dialect {

	public String dropObjectDDL(String objectType, String objectName) {
		return "drop " + objectType + " " + objectName + ";";
	}

	public String simpleObjectsSql(String objectType) {
		return "select object_name from user_objects where object_type ='" + objectType + "'";
	}

	public String addConstraintDDL(String tblName, String fkName, ForeignKey foreignKey) {
		StringBuilder ddlBuf = new StringBuilder("");
		ddlBuf.append("alter table " + tblName);
		ddlBuf.append(" add constraint " + fkName + " foreign key (");
		List<String> referByCols = foreignKey.getReferByCols();
		for (int i = 0; i < referByCols.size(); i++) {
			ddlBuf.append(referByCols.get(i));
			if (i < referByCols.size() - 1) {
				ddlBuf.append(", ");
			}
		}
		ddlBuf.append(") ");
		ddlBuf.append(StringUtil.LINE_BREAK);
		ddlBuf.append("references " + foreignKey.getReferToTable() + "(");
		List<String> referToCols = foreignKey.getReferToCols();
		for (int i = 0; i < referToCols.size(); i++) {
			ddlBuf.append(referToCols.get(i));
			if (i < referToCols.size() - 1) {
				ddlBuf.append(", ");
			}
		}
		ddlBuf.append(");");

		return ddlBuf.toString();
	}

	public String inlinePKConstraint(String constraintName, List<String> primaryKeys) {
		StringBuilder ddlBuf = new StringBuilder();
		ddlBuf.append("constraint " + constraintName + " primary key (");
		for (int i = 0; i < primaryKeys.size(); i++) {
			ddlBuf.append(primaryKeys.get(i));
			if (i < primaryKeys.size() - 1) {
				ddlBuf.append(", ");
			}
		}
		ddlBuf.append(") ");
		return ddlBuf.toString();
	}

	public String columnSpec(TableDescriptor tableDescriptor) {
		StringBuilder ddlBuf = new StringBuilder();
		List<ColumnDescriptor> columns = tableDescriptor.getColumns();
		Collections.sort(columns, new Comparator<ColumnDescriptor>() {
			public int compare(ColumnDescriptor o1, ColumnDescriptor o2) {
				return o1.getOrdinalPosition().compareTo(o2.getOrdinalPosition());
			}
		});
		int commaCount = columns.size();
		for (ColumnDescriptor columnDescriptor : columns) {
			ddlBuf.append(columnDescriptor.getName() + " ");
			ddlBuf.append(getSqlDataType(columnDescriptor));
			ddlBuf.append(" ");
			String dflt = columnDescriptor.getColDefault();

			if (StringUtils.isNotBlank(dflt)) {
				ddlBuf.append(" default " + dflt + " ");
			}

			if (!columnDescriptor.isNullable()) {
				ddlBuf.append(" not null ");
			}

			commaCount--;
			if (commaCount > 0) {
				ddlBuf.append(", ");
				ddlBuf.append(StringUtil.LINE_BREAK);
			}
		}
		return ddlBuf.toString();
	}

	public String columnSpec(ColumnDescriptor columnDescriptor) {
		StringBuilder ddlBuf = new StringBuilder();
		ddlBuf.append(columnDescriptor.getName() + " ");
		ddlBuf.append(getSqlDataType(columnDescriptor));
		ddlBuf.append(" ");
		if (!columnDescriptor.isNullable()) {
			ddlBuf.append(" not null ");
		}

		return ddlBuf.toString();

	}

	public String getSqlDataType(ColumnDescriptor columnDescriptor) {
		String sqlDataType = null;
		switch (columnDescriptor.getJdbcType()) {
		case Types.BIGINT:
			sqlDataType = "BIGINT";
			break;
		case Types.BINARY:
			sqlDataType = "BINARY";
			break;
		case Types.BIT:
			sqlDataType = "BIT";
			break;
		case Types.BLOB:
			sqlDataType = "BLOB";
			break;
		case Types.BOOLEAN:
			sqlDataType = "BOOLEAN";
			break;
		case Types.CHAR:
			sqlDataType = "CHAR" + "(" + columnDescriptor.getSize() + ")";
			break;
		case Types.CLOB:
			sqlDataType = "CLOB";
			break;
		case Types.DATE:
			sqlDataType = "DATE";
			break;
		case Types.DECIMAL:
			if (columnDescriptor.getSize() > 0 || columnDescriptor.getDecimalDigits() > 0) {
				sqlDataType = "NUMBER" + "(" + columnDescriptor.getSize() + (columnDescriptor.getDecimalDigits() > 0 ? "," + columnDescriptor.getDecimalDigits() : "") + ")";
			} else {
				sqlDataType = "INTEGER";
			}
			break;
		case Types.DOUBLE:
			sqlDataType = "DOUBLE" + "(" + columnDescriptor.getSize() + (columnDescriptor.getDecimalDigits() > 0 ? "," + columnDescriptor.getDecimalDigits() : "") + ")";
			break;
		case Types.FLOAT:
			sqlDataType = "FLOAT" + "(" + columnDescriptor.getSize() + (columnDescriptor.getDecimalDigits() > 0 ? "," + columnDescriptor.getDecimalDigits() : "") + ")";
			break;
		case Types.INTEGER:
			sqlDataType = "INTEGER";
			break;
		case Types.NUMERIC:
			sqlDataType = "NUMERIC" + "(" + columnDescriptor.getSize() + (columnDescriptor.getDecimalDigits() > 0 ? "," + columnDescriptor.getDecimalDigits() : "") + ")";
			break;

		case Types.SMALLINT:
			sqlDataType = "SMALLINT";
			break;
		case Types.TIME:
			sqlDataType = "TIME";
			break;
		case Types.TIMESTAMP:
			sqlDataType = "TIMESTAMP";
			break;

		case Types.TINYINT:
			sqlDataType = "SMALLINT";
			break;
		case Types.VARBINARY:
			sqlDataType = "VARBINARY";
			break;
		case Types.VARCHAR:
			sqlDataType = "VARCHAR2" + "(" + columnDescriptor.getSize() + ")";
			break;
		case Types.SQLXML:
			sqlDataType = "XMLTYPE";
			break;
		default:
			sqlDataType = "NULL";
			break;
		}
		return sqlDataType;
	}

	public String getJavaDataType(ColumnDescriptor columnDescriptor) {
		String sqlDataType = null;
		switch (columnDescriptor.getJdbcType()) {
		case Types.BIGINT:
			sqlDataType = "Integer";
			break;
		case Types.BINARY:
			sqlDataType = "byte";
			break;
		case Types.BIT:
			sqlDataType = "byte";
			break;
		case Types.BLOB:
			sqlDataType = "byte[]";
			break;
		case Types.BOOLEAN:
			sqlDataType = "Boolean";
			break;
		case Types.CHAR:
			sqlDataType = "String";
			break;
		case Types.CLOB:
			sqlDataType = "byte[]";
			break;
		case Types.DATE:
			sqlDataType = "java.sql.Date";
			break;
		case Types.DECIMAL:
			if (columnDescriptor.getDecimalDigits() > 0) {
				sqlDataType = "BigDecimal";
			} else {
				sqlDataType = "Integer";
			}
			break;
		case Types.DOUBLE:
			if (columnDescriptor.getDecimalDigits() > 0) {
				sqlDataType = "Double";
			} else {
				sqlDataType = "Integer";
			}
			break;
		case Types.FLOAT:
			if (columnDescriptor.getDecimalDigits() > 0) {
				sqlDataType = "BigDecimal";
			} else {
				sqlDataType = "Integer";
			}
			break;
		case Types.INTEGER:
			sqlDataType = "Integer";
			break;
		case Types.NUMERIC:
			if (columnDescriptor.getDecimalDigits() > 0) {
				sqlDataType = "BigDecimal";
			} else {
				sqlDataType = "Integer";
			}
			break;

		case Types.SMALLINT:
			sqlDataType = "Integer";
			break;
		case Types.TIME:
			sqlDataType = "java.sql.Timestamp";
			break;
		case Types.TIMESTAMP:
			sqlDataType = "java.sql.Timestamp";
			break;

		case Types.TINYINT:
			sqlDataType = "Integer";
			break;
		case Types.VARBINARY:
			sqlDataType = "byte";
			break;
		case Types.VARCHAR:
			sqlDataType = "String";
			break;
		default:
			sqlDataType = "";
			break;
		}
		return sqlDataType;
	}

	public Object readData(ColumnDescriptor columnDescriptor, ResultSet rs) throws SQLException {
		Object data = null;
		switch (columnDescriptor.getJdbcType()) {
		case Types.BIGINT:
			data = rs.getInt(columnDescriptor.getName());
			break;
		case Types.BINARY:
			// do nothing
			break;
		case Types.BIT:
			// do nothing
			break;
		case Types.BLOB:
			// do nothing
			break;
		case Types.BOOLEAN:
			data = rs.getBoolean(columnDescriptor.getName());
			break;
		case Types.CHAR:
			data = rs.getString(columnDescriptor.getName());
			break;
		case Types.CLOB:
			// do nothing
			break;
		case Types.DATE:
			data = rs.getDate(columnDescriptor.getName());
			break;
		case Types.DECIMAL:
			if (columnDescriptor.getDecimalDigits() > 0) {
				data = rs.getDouble(columnDescriptor.getName());
			} else {
				data = rs.getInt(columnDescriptor.getName());
			}
			break;
		case Types.DOUBLE:
			if (columnDescriptor.getDecimalDigits() > 0) {
				data = rs.getDouble(columnDescriptor.getName());
			} else {
				data = rs.getInt(columnDescriptor.getName());
			}
			break;
		case Types.FLOAT:
			if (columnDescriptor.getDecimalDigits() > 0) {
				data = rs.getFloat(columnDescriptor.getName());
			} else {
				data = rs.getInt(columnDescriptor.getName());
			}
			break;
		case Types.INTEGER:
			data = rs.getInt(columnDescriptor.getName());
			break;
		case Types.NUMERIC:
			if (columnDescriptor.getDecimalDigits() > 0) {
				data = rs.getDouble(columnDescriptor.getName());
			} else {
				data = rs.getInt(columnDescriptor.getName());
			}
			break;

		case Types.SMALLINT:
			data = rs.getInt(columnDescriptor.getName());
			break;
		case Types.TIME:
			data = rs.getTime(columnDescriptor.getName());
			break;
		case Types.TIMESTAMP:
			data = rs.getTimestamp(columnDescriptor.getName());
			break;

		case Types.TINYINT:
			data = rs.getInt(columnDescriptor.getName());
			break;
		case Types.VARBINARY:
			// do nothing
			break;
		case Types.VARCHAR:
			data = rs.getString(columnDescriptor.getName());
			break;
		default:
			data = rs.getObject(columnDescriptor.getName());
			break;
		}
		return data;
	}

	public String getTableCountSql(String schemaName) {
		return "select count(1) from user_tables";
	}

	public String generateTableDiffDLL(TableDescriptor src, TableDescriptor tgt) {
		if (src == null || tgt == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		// sb.append("-- TABLE " + src.getTableName());
		// sb.append(StringUtil.LINE_BREAK);
		List<ColumnDescriptor> toCols = tgt.getColumns();
		boolean dataTypeChanged = false;
		for (ColumnDescriptor tgtCol : toCols) {
			String colDefault = tgtCol.getColDefault();
			ColumnDescriptor srcCol = src.getColumn(tgtCol.getName());
			if (srcCol == null) {
				// add column
				sb.append("-- add column " + tgtCol.getName());
				sb.append(StringUtil.LINE_BREAK);
				sb.append("alter table " + src.getTableName() + " add (" + tgtCol.getName() + " " + getSqlDataType(tgtCol) + (StringUtils.isNotBlank(colDefault) ? " default " + colDefault : "")
						+ (tgtCol.isNullable() ? "" : " not null ") + ");");
				sb.append(StringUtil.LINE_BREAK);
			} else {
				if (!tgtCol.isSame(srcCol)) {
					if (!tgtCol.isJdbcTypeEqual(srcCol)) {
						dataTypeChanged = true;
					} else {
						if (tgtCol.isSizeIncreased(srcCol) || tgtCol.isDefaultAvailable(srcCol)) {
							// column name is same but definition is mismatching
							sb.append("-- modify existing column " + tgtCol.getName());
							sb.append(StringUtil.LINE_BREAK);
							sb.append("alter table " + src.getTableName() + " modify (" + tgtCol.getName() + " " + getSqlDataType(tgtCol)
									+ (StringUtils.isNotBlank(colDefault) ? " default " + colDefault : "") + ");");
							sb.append(StringUtil.LINE_BREAK);
						}
						if (!tgtCol.isNullableEqual(srcCol) && tgtCol.isNullable()) {
							sb.append("alter table " + src.getTableName() + " modify " + srcCol.getName() + " null;");
							sb.append(StringUtil.LINE_BREAK);
						}
					}
				}
			}
		}
		// find coumns to be dropped
		List<ColumnDescriptor> srcCols = src.getColumns();
		for (ColumnDescriptor col : srcCols) {
			if (tgt.getColumn(col.getName()) == null) {
				sb.append("-- drop column??  " + col.getName());
				sb.append(StringUtil.LINE_BREAK);
				if (!col.isNullable()) {
					sb.append("--alter table " + src.getTableName() + " drop column " + col.getName() + ";");
					sb.append(StringUtil.LINE_BREAK);
					sb.append("alter table " + src.getTableName() + " modify " + col.getName() + " null;");
					sb.append(StringUtil.LINE_BREAK);

				} else {
					sb.append("--alter table " + src.getTableName() + " drop column " + col.getName() + ";");
					sb.append(StringUtil.LINE_BREAK);
				}
			}
		}
		if (dataTypeChanged) {
			reconfigureColumnDataTypes(src, sb, toCols);

		}
		// find if PK is same
		List<String> tgtKeys = tgt.getPrimaryKeys();
		List<String> srcKeys = src.getPrimaryKeys();
		boolean pkMatched = true;
		if (!tgtKeys.isEmpty() && tgtKeys.size() != srcKeys.size()) {
			sb.append("-- PK mismatched in column count ");
			sb.append(StringUtil.LINE_BREAK);
			pkMatched = false;

		} else {
			for (String pk : tgtKeys) {
				if (!srcKeys.contains(pk)) {
					sb.append("-- PK mismatched in column " + pk);
					sb.append(StringUtil.LINE_BREAK);
					pkMatched = false;
					break;
				}
			}
		}
		String pkName = tgt.getPrimaryKeyName();
		if (!pkMatched && StringUtils.isNotBlank(pkName)) {
			// drop current one add create the new one
			sb.append("-- Drop PK ");
			sb.append(StringUtil.LINE_BREAK);
			String srcKey = src.getPrimaryKeyName();
			if (StringUtils.isNotBlank(srcKey)) {
				sb.append("alter table " + tgt.getTableName() + " drop constraint " + srcKey + " cascade;");
				sb.append(StringUtil.LINE_BREAK);
			}
			if (src.isContraintNameExists(pkName)) {
				pkName = pkName + "A";
			}
			sb.append("-- Add PK ");
			sb.append(StringUtil.LINE_BREAK);
			sb.append("alter table " + tgt.getTableName() + " add " + inlinePKConstraint(pkName, tgt.getPrimaryKeys()) + ";");
			sb.append(StringUtil.LINE_BREAK);
		}
		return sb.toString();
	}

	private void reconfigureColumnDataTypes(TableDescriptor src, StringBuilder sb, List<ColumnDescriptor> toCols) {
		sb.append("--create a new table _BK with all records");
		sb.append(StringUtil.LINE_BREAK);
		sb.append("create table " + src.getTableName() + "_BK as select * from " + src.getTableName() + ";");
		sb.append(StringUtil.LINE_BREAK);
		sb.append("--truncate table ");
		sb.append(StringUtil.LINE_BREAK);
		sb.append("truncate table " + src.getTableName() + ";");
		sb.append(StringUtil.LINE_BREAK);

		// drop and create referring constraints
		for (ColumnDescriptor tgtCol : toCols) {
			ColumnDescriptor srcCol = src.getColumn(tgtCol.getName());
			if (srcCol == null) {
				continue;
			}
			if (!tgtCol.isJdbcTypeEqual(srcCol)) {
				List<ForeignKey> exportedKeys = src.getExportedKeys(srcCol.getName());
				for (ForeignKey foreignKey : exportedKeys) {
					sb.append("alter table " + src.getTableName() + "drop constraint " + foreignKey.getName() + ";");
					sb.append(StringUtil.LINE_BREAK);
				}
			}
		}

		// apply the new configuration
		for (ColumnDescriptor tgtCol : toCols) {
			ColumnDescriptor srcCol = src.getColumn(tgtCol.getName());
			if (srcCol == null) {
				continue;
			}
			String colDefault = tgtCol.getColDefault();
			if (!tgtCol.isJdbcTypeEqual(srcCol)) {
				sb.append("--begin modify column type for " + srcCol.getName());
				sb.append(StringUtil.LINE_BREAK);
				// modify the table data type
				sb.append("alter table " + src.getTableName() + " modify (" + tgtCol.getName() + " " + getSqlDataType(tgtCol)
						+ ((StringUtils.isNotBlank(colDefault) ? (" default " + colDefault) : (!tgtCol.isNullableEqual(srcCol) ? " null " : ""))) + ");");
				sb.append(StringUtil.LINE_BREAK);
			}
		}
		for (ColumnDescriptor tgtCol : toCols) {
			ColumnDescriptor srcCol = src.getColumn(tgtCol.getName());
			if (srcCol == null) {
				continue;
			}
			if (!tgtCol.isJdbcTypeEqual(srcCol)) {
				List<ForeignKey> exportedKeys = src.getExportedKeys(srcCol.getName());
				for (ForeignKey foreignKey : exportedKeys) {
					sb.append(DDL.addConstraintDDL(foreignKey.getReferByTable(), foreignKey.getName(), foreignKey));
					sb.append(StringUtil.LINE_BREAK);
				}
			}
		}

		sb.append("--copy the data back");
		sb.append(StringUtil.LINE_BREAK);
		sb.append("insert into " + src.getTableName() + " select * from " + src.getTableName() + "_BK;");
		sb.append(StringUtil.LINE_BREAK);
		sb.append("commit;");
		sb.append(StringUtil.LINE_BREAK);

		// drop the _BK table
		sb.append("--drop _BK table");
		sb.append(StringUtil.LINE_BREAK);
		sb.append("drop table " + src.getTableName() + "_BK cascade constraints;");
		sb.append(StringUtil.LINE_BREAK);
	}

	public String addIndexDDL(String tblName, String indexName, IndexInfo index) {
		StringBuilder ddlBuf = new StringBuilder("");
		ddlBuf.append("create index " + indexName + " on ");
		ddlBuf.append(tblName);
		ddlBuf.append(" (");

		List<String> cols = index.getColumns();
		for (int i = 0; i < cols.size(); i++) {
			ddlBuf.append(cols.get(i));
			if (i < cols.size() - 1) {
				ddlBuf.append(", ");
			}
		}
		ddlBuf.append(");");
		return ddlBuf.toString();
	}

	public String addUniqueDDL(String tblName, String indexName, IndexInfo index) {
		StringBuilder ddlBuf = new StringBuilder("");
		ddlBuf.append("alter table ");
		ddlBuf.append(tblName);
		ddlBuf.append(" add constraint " + indexName + " unique ");
		ddlBuf.append("(");

		List<String> cols = index.getColumns();
		for (int i = 0; i < cols.size(); i++) {
			ddlBuf.append(cols.get(i));
			if (i < cols.size() - 1) {
				ddlBuf.append(", ");
			}
		}
		ddlBuf.append(") using index;");
		return ddlBuf.toString();
	}

	public String dropConstraintDDL(String tblName, String name) {
		return "alter table " + tblName + " drop constraint " + name + " cascade;";
	}

	public String createSequence(SequenceInfo sequence) {
		StringBuilder builder = new StringBuilder();
		builder.append("create sequence " + sequence.getSequenceName() + " minvalue " + (int) sequence.getMinValue());
		if (sequence.getMaxValue() > 0) {
			builder.append(" maxvalue " + (int) sequence.getMaxValue());
		}
		if (sequence.getLastNumber() > 0) {
			builder.append(" start with  " + sequence.getLastNumber());
		}
		if (sequence.getIncrementBy() > 0) {
			builder.append(" increment by  " + sequence.getIncrementBy());
		}
		if (sequence.getCacheSize() == 0) {
			builder.append(" nocache");
		} else {
			builder.append(" cache " + sequence.getCacheSize());
		}
		if ("Y".equals(sequence.getOrderFlag())) {
			builder.append(" order");
		}
		if ("Y".equals(sequence.getCycleFlag())) {
			builder.append(" cycle");
		}
		builder.append(";");

		return builder.toString();

	}

	public String buildDropUserObjectsSql(Connection con) {
		StringBuilder sb = new StringBuilder();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("select 'drop ' || object_type || ' ' || object_name || (decode(object_type, 'TABLE', ' cascade constraints', '')) || ';' from user_objects where object_type not in ('INDEX', 'LOB') order by object_type, object_name");
			while (rs != null && rs.next()) {
				sb.append(rs.getString(1));
				sb.append(StringUtil.LINE_BREAK);
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
