package com.lndb.dwtool.erm.hive;

import java.sql.Types;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.util.StringUtil;

public class HiveDDLGenerator {
	public static String hiveETLTableDDL(TableDescriptor td) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table if not exists " + td.getTableName());
		sb.append("(");
		sb.append(StringUtil.LINE_BREAK);
		int colCnt = td.getColumns().size();
		for (ColumnDescriptor colDesc : td.getColumns()) {
			sb.append(getColDesc(colDesc));
			if (colCnt-- > 1) {
				sb.append(", ");
				sb.append(StringUtil.LINE_BREAK);
			}
		}
		sb.append(StringUtil.LINE_BREAK);
		sb.append(") PARTITIONED BY (dw_run_date DATE);");
		return sb.toString();
	}

	private static String getColDesc(ColumnDescriptor columnDescriptor) {
		String colDesc = "";
		switch (columnDescriptor.getJdbcType()) {
		case Types.BIGINT:
			colDesc = "BIGINT";
			break;
		case Types.BINARY:
			colDesc = "BINARY";
			break;
		case Types.BIT:
			colDesc = "BIT";
			break;
		case Types.BLOB:
			colDesc = "BINARY	";
			break;
		case Types.BOOLEAN:
			colDesc = "BOOLEAN";
			break;
		case Types.CHAR:
			// colDesc = "CHAR" + "(" + columnDescriptor.getSize() + ")";
			colDesc = "STRING";
			break;
		case Types.CLOB:
			colDesc = "BINARY";
			break;
		case Types.DATE:
			colDesc = "DATE";
			break;
		case Types.DECIMAL:
			if (columnDescriptor.getDecimalDigits() > 0) {
				colDesc = "DECIMAL" + "(" + columnDescriptor.getSize() + (columnDescriptor.getDecimalDigits() > 0 ? "," + columnDescriptor.getDecimalDigits() : "") + ")";
			} else {
				colDesc = "INT";
			}
			break;
		case Types.DOUBLE:
			colDesc = "DOUBLE";
			break;
		case Types.FLOAT:
			colDesc = "FLOAT";
			break;
		case Types.INTEGER:
			colDesc = "INT";
			break;
		case Types.NUMERIC:
			if (columnDescriptor.getDecimalDigits() > 0) {
				colDesc = "DECIMAL" + "(" + columnDescriptor.getSize() + (columnDescriptor.getDecimalDigits() > 0 ? "," + columnDescriptor.getDecimalDigits() : "") + ")";
			} else {
				colDesc = "INT";
			}
			break;

		case Types.SMALLINT:
			colDesc = "SMALLINT";
			break;
		case Types.TIME:
			colDesc = "TIMESTAMP";
			break;
		case Types.TIMESTAMP:
			colDesc = "TIMESTAMP";
			break;

		case Types.TINYINT:
			colDesc = "SMALLINT";
			break;
		case Types.VARBINARY:
			colDesc = "BINARY";
			break;
		case Types.VARCHAR:
			// colDesc = "VARCHAR2" + "(" + columnDescriptor.getSize() + ")";
			colDesc = "STRING";
			break;
		case Types.SQLXML:
			colDesc = "STRING";
			break;
		default:
			colDesc = "STRING";
			break;
		}
		return columnDescriptor.getName() + " " + colDesc;
	}
}
