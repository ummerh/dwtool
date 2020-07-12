package com.lndb.dwtool.erm.db.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.StatementPool;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.util.ErrorTypes;
import com.lndb.dwtool.erm.util.StringUtil;

public class TableRow {
	private HashMap<String, Object> rowMap = new HashMap<String, Object>(1);
	private TableDescriptor descriptor;
	private String[] headers;
	private List<String> errorsFound;
	private String currCol;

	private static final String[] DATE_PATTERNS = new String[] { "MM/dd/yyyy", "MM-dd-yyyy", "yyyy/MM/dd", "yyyy-MM-dd", "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy HH:mm:ss", "MM-dd-yyyy hh:mm:ss a",
			"MM-dd-yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SS", "MM-dd-yyyy HH:mm:ss.SSS" };

	public TableRow(String[] headers, TableDescriptor descriptor, Object[] data) {
		this.headers = headers;
		for (int i = 0; i < headers.length; i++) {
			this.rowMap.put(headers[i].toUpperCase(), data[i]);
		}
		this.descriptor = descriptor;
		this.errorsFound = new ArrayList<String>(headers.length);
	}

	public TableRow(TableDescriptor descriptor, HashMap<String, Object> rowMap) {
		Set<String> keySet = rowMap.keySet();
		headers = new String[keySet.size()];
		int i = 0;
		for (String string : keySet) {
			headers[i] = string;
			i++;
		}
		this.rowMap = rowMap;
		this.descriptor = descriptor;
		this.errorsFound = new ArrayList<String>(headers.length);
	}

	public Object getData(String colName) {
		currCol = colName.toUpperCase();
		Object data = this.rowMap.get(colName.toUpperCase());
		if (data == null)
			return null;
		// add special rules for DATE, TIME and TIMESTAMP
		int jdbcType = descriptor.getColumn(colName.toUpperCase()).getJdbcType();
		Object time = null;
		switch (jdbcType) {
		case Types.DATE:
			if ((time = getTime(data)) != null && Long.class.isAssignableFrom(time.getClass())) {
				data = new Date((Long) time);
			}
			break;
		case Types.TIME:
			if ((time = getTime(data)) != null && Long.class.isAssignableFrom(time.getClass())) {
				data = new Timestamp((Long) time);
			}
			break;
		case Types.TIMESTAMP:
			if ((time = getTime(data)) != null && Long.class.isAssignableFrom(time.getClass())) {
				data = new Timestamp((Long) time);
			}
			break;
		default:
			data = this.rowMap.get(colName.toUpperCase());
			if (String.class.isAssignableFrom(data.getClass())) {
				data = StringUtil.replaceFiller(data.toString());
			}
			break;
		}

		return data;
	}

	private Object getTime(Object data) {
		if (data != null && java.util.Date.class.isAssignableFrom(data.getClass())) {
			return ((java.util.Date) data).getTime();
		}
		String str = data != null ? data.toString() : null;
		if (str == null || StringUtils.isBlank(str))
			return null;

		Long time = 0L;
		try {
			time = DateUtils.parseDate(str.length() > 21 ? str.substring(0, 21) : str, DATE_PATTERNS).getTime();
		} catch (ParseException e) {
			System.out.println("Failed parsing " + str);
			// this.errorsFound.add("Date parse error for data colum:"
			// + this.currCol + " data:" + data);
			// return ErrorTypes.PARSE_ERROR;
			time = new java.util.Date().getTime();
		}
		return time;
	}

	public void updateStatementParams(PreparedStatement pStatement) throws SQLException {
		List<String> primaryKeys = this.descriptor.getPrimaryKeys();
		int pos = 0;
		for (int i = 0; i < headers.length; i++) {
			if (!isObjIdOrVerNbr(headers[i].toUpperCase()) && !descriptor.getPrimaryKeys().contains(headers[i].toUpperCase())) {
				pos++;
				Object data = getData(headers[i].toUpperCase());
				if (data != null) {
					pStatement.setObject(pos, data);
				} else {
					pStatement.setNull(pos, this.descriptor.getColumn(headers[i].toUpperCase()).getJdbcType());
				}
			}
		}
		for (int i = 0; i < primaryKeys.size(); i++) {
			pos++;
			Object data = getData(primaryKeys.get(i));
			if (data == null) {
				pStatement.setNull(pos, descriptor.getColumn(primaryKeys.get(i)).getJdbcType());
			} else {
				pStatement.setObject(pos, data);
			}
		}
	}

	public void deleteStatementParams(PreparedStatement pStatement) throws SQLException {
		List<String> primaryKeys = this.descriptor.getPrimaryKeys();
		int pos = 0;
		for (int i = 0; i < primaryKeys.size(); i++) {
			pos++;
			Object data = getData(primaryKeys.get(i));
			if (data == null) {
				pStatement.setNull(pos, descriptor.getColumn(primaryKeys.get(i)).getJdbcType());
			} else {
				pStatement.setObject(pos, data);
			}
		}
	}

	public void insertStatementParams(PreparedStatement pStatement) throws SQLException {
		int pos = 0;
		for (int i = 0; i < headers.length; i++) {
			pos++;
			String col = headers[i].toUpperCase();
			Object data = getData(col);
			if (isObjIdOrVerNbr(col)) {
				if (data == null || StringUtils.isBlank(data.toString())) {
					if ("OBJ_ID".equalsIgnoreCase(col)) {
						// select * from dual
						Connection con = pStatement.getConnection();
						Statement stmt = con.createStatement();
						ResultSet rs = stmt.executeQuery("select SYS_GUID() from dual");
						if (rs != null && rs.next()) {
							pStatement.setString(pos, rs.getString(1));
						}
						rs.close();
						stmt.close();
					} else if ("VER_NBR".equalsIgnoreCase(col)) {
						pStatement.setInt(pos, 0);
					}
				} else {
					pStatement.setObject(pos, data);
				}

			} else {
				if (data != null) {
					pStatement.setObject(pos, data);
				} else {
					pStatement.setNull(pos, this.descriptor.getColumn(headers[i].toUpperCase()).getJdbcType());
				}
			}
		}
	}

	public String checkDataIntegrity(Connection con, SchemaJoinMetaData metaData) {

		try {
			List<ForeignKey> allKeys = metaData.getImportedKeys(this.descriptor.getTableName());
			if (allKeys == null) {
				return "PASS";
			}
			prepareSelectStatements(con, allKeys);
			for (ForeignKey fkKey : allKeys) {
				List<String> referToCols = fkKey.getReferToCols();
				List<String> referByCols = fkKey.getReferByCols();
				if (shouldCheckReference(fkKey, referToCols, referByCols)) {
					PreparedStatement pstmt = StatementPool.get(fkKey.getName());
					updateSelectParameters(referToCols, referByCols, pstmt);
					ResultSet result = pstmt.executeQuery();
					if (result != null && result.next()) {
						if (result.getInt(1) == 0) {
							result.close();
							pstmt.clearParameters();
							this.errorsFound.add("FK from " + fkKey.getReferByTable() + ":" + referByCols + " to " + fkKey.getReferToTable() + " failed");
							return "FAIL";
						}
					}
					result.close();
					pstmt.clearParameters();
				}
			}
		} catch (Exception e) {
			this.errorsFound.add("Exception in data integrity check for table " + this.descriptor.getTableName());
			return "FAIL";
		}
		return "PASS";
	}

	private void prepareSelectStatements(Connection con, List<ForeignKey> allKeys) throws SQLException {
		for (ForeignKey fkKey : allKeys) {
			if (StatementPool.get(fkKey.getName()) == null) {
				List<String> referToCols = fkKey.getReferToCols();
				List<String> referByCols = fkKey.getReferByCols();
				String existenceCheckSql = generateExistenceCountSql(fkKey, referToCols, referByCols);
				PreparedStatement pstmt = con.prepareStatement(existenceCheckSql);
				StatementPool.add(fkKey.getName(), pstmt);
			}

		}
	}

	private void updateSelectParameters(List<String> referToCols, List<String> referByCols, PreparedStatement pstmt) throws SQLException {
		int pos = 0;
		for (int i = 0; i < referToCols.size() && i < referByCols.size(); i++) {
			pos++;
			pstmt.setObject(pos, getData(referByCols.get(i)));
		}
	}

	private String generateExistenceCountSql(ForeignKey ojbKey, List<String> referToCols, List<String> referByCols) {
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("select count(1) from ");
		sqlBuf.append(ojbKey.getReferToTable());
		sqlBuf.append(" A where ");
		for (int i = 0; i < referToCols.size() && i < referByCols.size(); i++) {
			sqlBuf.append("A.");
			sqlBuf.append(referToCols.get(i));
			sqlBuf.append(" = ? ");
			if (i < referToCols.size() - 1) {
				sqlBuf.append(" and ");
			}
		}
		sqlBuf.append("");
		return sqlBuf.toString();
	}

	private boolean shouldCheckReference(ForeignKey fkKey, List<String> referToCols, List<String> referByCols) {
		boolean valid = true;
		valid &= !fkKey.isCascadeUpdate();
		valid &= !fkKey.isReferToView();
		valid &= referToCols != null;
		valid &= !referToCols.isEmpty();
		valid &= referByCols != null;
		valid &= !referByCols.isEmpty();
		valid &= referByCols.size() == referToCols.size();
		if (valid) {
			for (String col : referByCols) {
				Object data = this.getData(col);
				if (data == null || (String.class.isAssignableFrom(data.getClass()) && ((String) data).trim().length() == 0)) {
					valid = false;
					break;
				}
			}
		}
		return valid;
	}

	public static String prepareUpdateStatement(String[] headers, TableDescriptor descriptor) {
		if (descriptor == null || descriptor.getPrimaryKeys().isEmpty()) {
			return null;
		}
		List<String> primaryKeys = descriptor.getPrimaryKeys();
		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append(descriptor.getTableName());
		sql.append(" set ");
		// set columns
		for (int i = 0; i < headers.length; i++) {
			if (!isObjIdOrVerNbr(headers[i].toUpperCase()) && !descriptor.getPrimaryKeys().contains(headers[i].toUpperCase())) {
				sql.append(headers[i].toUpperCase());
				sql.append("= ? ");
				if (i < headers.length - 1) {
					sql.append(", ");
				}
			}
		}
		sql.append(" where ");
		for (int i = 0; i < primaryKeys.size(); i++) {
			sql.append(primaryKeys.get(i));
			sql.append("= ? ");
			if (i < primaryKeys.size() - 1) {
				sql.append(" and ");
			}
		}
		return sql.toString();
	}

	public static String prepareDeleteStatement(TableDescriptor descriptor) {
		if (descriptor == null || descriptor.getPrimaryKeys().isEmpty()) {
			return null;
		}
		List<String> primaryKeys = descriptor.getPrimaryKeys();
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ");
		sql.append(descriptor.getTableName());
		sql.append(" where ");
		for (int i = 0; i < primaryKeys.size(); i++) {
			sql.append(primaryKeys.get(i));
			sql.append("= ? ");
			if (i < primaryKeys.size() - 1) {
				sql.append(" and ");
			}
		}
		return sql.toString();
	}

	public static String prepareInsertStatement(String[] headers, TableDescriptor descriptor) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		sql.append(descriptor.getTableName());
		sql.append(" (");
		boolean shouldGenerate_OBJ_ID = shouldGenerate_OBJ_ID(headers, descriptor);
		boolean shouldGenerate_VER_NBR = shouldGenerate_VER_NBR(headers, descriptor);
		if (shouldGenerate_OBJ_ID) {
			sql.append("OBJ_ID, ");
		}
		if (shouldGenerate_VER_NBR) {
			sql.append("VER_NBR, ");
		}
		// set columns
		for (int i = 0; i < headers.length; i++) {
			sql.append(headers[i].toUpperCase());
			if (i < headers.length - 1) {
				sql.append(", ");
			}
		}
		sql.append(") values (");
		if (shouldGenerate_OBJ_ID) {
			sql.append("SYS_GUID(), ");
		}
		if (shouldGenerate_VER_NBR) {
			sql.append("1, ");
		}
		for (int i = 0; i < headers.length; i++) {
			sql.append(" ? ");
			if (i < headers.length - 1) {
				sql.append(", ");
			}
		}
		sql.append(")");
		return sql.toString();
	}

	public static boolean shouldGenerate_OBJ_ID(String[] headers, TableDescriptor descriptor) {
		boolean objIdExists = false;
		for (int i = 0; i < headers.length; i++) {
			if ("OBJ_ID".equalsIgnoreCase(headers[i].toUpperCase())) {
				objIdExists = true;
				break;
			}
		}
		if (!objIdExists && descriptor.contains_OBJ_ID()) {
			return true;
		}
		return false;
	}

	public static boolean shouldGenerate_VER_NBR(String[] headers, TableDescriptor descriptor) {
		boolean verNumExists = false;
		for (int i = 0; i < headers.length; i++) {
			if ("VER_NBR".equalsIgnoreCase(headers[i].toUpperCase())) {
				verNumExists = true;
				break;
			}
		}
		if (!verNumExists && descriptor.contains_VER_NBR()) {
			return true;
		}
		return false;
	}

	/**
	 * @return the headers
	 */
	public String[] getHeaders() {
		return headers;
	}

	/**
	 * @param headers
	 *            the headers to set
	 */
	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

	public boolean validateDataTypes() {
		String[] headers = getHeaders();
		boolean valid = true;
		for (String hdr : headers) {
			ColumnDescriptor column = descriptor.getColumn(hdr);
			int jdbcType = column.getJdbcType();
			Object data = getData(hdr);
			if (data == null) {
				if (isNotNullable(hdr, column)) {
					valid &= false;
					this.errorsFound.add("Null not allowed for col:" + this.currCol);
				}
				continue;
			}
			if (ErrorTypes.class.isAssignableFrom(data.getClass())) {
				continue;
			}
			String strData = null;
			if (String.class.isAssignableFrom(data.getClass())) {
				strData = data.toString();
				if (StringUtils.isBlank(strData)) {
					if (isNotNullable(hdr, column)) {
						valid &= false;
						this.errorsFound.add("Null not allowed for col:" + this.currCol);
					}
					continue;
				}
			}
			switch (jdbcType) {
			case Types.BIGINT:
				valid &= validateInt(strData);
				break;
			case Types.BOOLEAN:
				valid &= validateBoolean(strData);
				break;
			case Types.DECIMAL:
				valid &= validateDouble(strData);
				break;
			case Types.DOUBLE:
				valid &= validateDouble(strData);
				break;
			case Types.FLOAT:
				valid &= validateFloat(strData);
				break;
			case Types.INTEGER:
				valid &= validateInt(strData);
				break;
			case Types.NUMERIC:
				valid &= validateInt(strData);
				break;

			case Types.SMALLINT:
				valid &= validateInt(strData);
				break;

			case Types.TINYINT:
				valid &= validateInt(strData);
				break;
			default:
				if (strData != null && strData.length() > (column.getDecimalDigits() > 0 ? column.getSize() + 1 + column.getDecimalDigits() : column.getSize())) {
					this.errorsFound.add("Size exceeds col:" + this.currCol + " data:" + strData);
					valid &= false;
				}
				valid &= true;
				break;
			}
		}
		return valid;
	}

	private boolean isNotNullable(String hdr, ColumnDescriptor column) {
		return !column.isNullable() && !"OBJ_ID".equalsIgnoreCase(hdr) && !"VER_NBR".equalsIgnoreCase(hdr);
	}

	private static boolean isObjIdOrVerNbr(String colName) {
		return "OBJ_ID".equalsIgnoreCase(colName) || "VER_NBR".equalsIgnoreCase(colName);
	}

	private boolean validateFloat(String strData) {
		try {
			if (strData != null) {
				Float.parseFloat(strData);
			}
		} catch (Exception e) {
			this.errorsFound.add("Float parse error for col:" + this.currCol + " data:" + strData);
			return false;
		}
		return true;
	}

	private boolean validateDouble(String strData) {
		try {
			if (strData != null) {
				Double.parseDouble(strData);
			}
		} catch (Exception e) {
			this.errorsFound.add("Double parse error for col:" + this.currCol + " data:" + strData);
			return false;
		}
		return true;
	}

	private boolean validateBoolean(String strData) {
		String dataStr = strData;
		try {
			if (dataStr != null) {
				dataStr = dataStr.equals("Y") ? "true" : (dataStr.equals("N") ? "false" : dataStr);
				Boolean.parseBoolean(strData);
			}
		} catch (Exception e) {
			this.errorsFound.add("Boolean parse error for col:" + this.currCol + " data:" + strData);
			return false;
		}
		return true;
	}

	private boolean validateInt(String strData) {
		try {
			if (strData != null) {
				Integer.parseInt(strData);
			}
		} catch (Exception e) {
			this.errorsFound.add("Integer parse error for col:" + this.currCol + " data:" + strData);
			return false;
		}
		return true;
	}

	/**
	 * @return the errorsFound
	 */
	public List<String> getErrorsFound() {
		return errorsFound;
	}

	/**
	 * @param errorsFound
	 *            the errorsFound to set
	 */
	public void setErrorsFound(List<String> errorsFound) {
		this.errorsFound = errorsFound;
	}

	public String prepareSelectSql() {
		Set<String> keys = rowMap.keySet();
		String sql = "select * from " + this.descriptor.getTableName();
		int match = 0;
		for (String key : keys) {
			Object val = rowMap.get(key);
			if (val != null && val.toString().trim().length() > 0) {
				match++;
				if (match == 1) {

					sql = sql + " where ";
				}
				sql = sql + " " + key + " = ? and";
			}
		}
		if (match > 0) {
			sql = sql.substring(0, sql.lastIndexOf("and"));
		}
		return sql;
	}

	public void updateSelectParms(PreparedStatement pstmt) throws SQLException {

		Set<String> keys = rowMap.keySet();
		int match = 0;
		for (String key : keys) {
			Object val = rowMap.get(key);
			if (val != null && val.toString().trim().length() > 0) {
				match++;
				pstmt.setObject(match, getData(key));
			}
		}
	}
}
