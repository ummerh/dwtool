package com.lndb.dwtool.erm.ddl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.SequenceInfo;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.util.StringUtil;

public class MySqlDialect implements Dialect {

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
		int commaCount = columns.size();
		for (ColumnDescriptor columnDescriptor : columns) {
			ddlBuf.append(columnDescriptor.getName() + " ");
			ddlBuf.append(getSqlDataType(columnDescriptor));
			ddlBuf.append(" ");
			if (!columnDescriptor.isNullable()) {
				ddlBuf.append("NOT NULL ");
			}
			commaCount--;
			if (commaCount > 0) {
				ddlBuf.append(", ");
				ddlBuf.append(StringUtil.LINE_BREAK);
			}
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
			if (columnDescriptor.getDecimalDigits() > 0) {
				sqlDataType = "DECIMAL" + "(" + columnDescriptor.getSize() + (columnDescriptor.getDecimalDigits() > 0 ? "," + columnDescriptor.getDecimalDigits() : "") + ")";
			} else {
				sqlDataType = "NUMERIC";
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
			sqlDataType = "TINYINT";
			break;
		case Types.VARBINARY:
			sqlDataType = "VARBINARY";
			break;
		case Types.VARCHAR:
			sqlDataType = "VARCHAR" + "(" + columnDescriptor.getSize() + ")";
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

	public String getTableCountSql(String schemaName) {
		return "select count(1) from information_schema.tables where table_schema='" + schemaName + "'";
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
			// do nothing
			break;
		}
		return data;
	}

	public String generateTableDiffDLL(TableDescriptor src, TableDescriptor tgt) {
		StringBuilder sb = new StringBuilder();
		List<ColumnDescriptor> toCols = tgt.getColumns();
		for (ColumnDescriptor tgtCol : toCols) {
			ColumnDescriptor srcCol = src.getColumn(tgtCol.getName());
			if (srcCol == null) {
				// add column
				sb.append("alter table " + src.getTableName() + " add (" + tgtCol.getName() + " " + getSqlDataType(tgtCol) + ");");
				sb.append(StringUtil.LINE_BREAK);
			} else {
				if (!tgtCol.isSame(srcCol)) {
					sb.append("alter table " + src.getTableName() + " modify (" + tgtCol.getName() + " " + getSqlDataType(tgtCol) + ");");
					sb.append(StringUtil.LINE_BREAK);
				}
			}
		}
		return sb.toString();
	}

	public String columnSpec(ColumnDescriptor columnDescriptor) {
		String colDefault = columnDescriptor.getColDefault();
		if (colDefault != null) {
			colDefault = colDefault.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "").trim();
		}
		return colDefault;
	}

	public String createSequence(SequenceInfo sequence) {
		// TODO Auto-generated method stub
		return "";
	}

	public String buildDropUserObjectsSql(Connection con) {
		// TODO Auto-generated method stub
		return null;
	}
}
