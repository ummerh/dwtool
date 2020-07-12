package com.lndb.dwtool.erm.manager.web.tag;

import java.io.IOException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;

public class DisplayDBFieldTag extends ErTagSupport {
	private static final long serialVersionUID = 288371892417543721L;
	private String columnName;
	private TableDescriptor tableDescriptor;
	private Object data;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public TableDescriptor getTableDescriptor() {
		return tableDescriptor;
	}

	public void setTableDescriptor(TableDescriptor tableDescriptor) {
		this.tableDescriptor = tableDescriptor;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public int doEndTag() throws JspException {
		ColumnDescriptor column = this.tableDescriptor.getColumn(getColumnName());
		int size = column.getSize() > 50 ? 50 : column.getSize();
		int maxlen = column.getSize();
		if (data != null && Date.class.isAssignableFrom(data.getClass())) {
			data = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(data);
		}

		String output = "";
		if (isString() || isDateTime()) {
			output = "<input id=\"" + getId() + "\" size=\"" + size + "\" maxlength=\"" + maxlen + "\" type=\"text\" name=\"" + getProperty() + "\" value=\""
					+ String.valueOf(data == null ? "" : data) + "\" " + (readOnly ? " readonly=\"readonly\" " : "") + " />";
		}
		if (isNumeric()) {
			output = "<input id=\"" + getId() + "\" size=\"" + size + "\" maxlength=\"" + maxlen + 1 + "\" type=\"text\" name=\"" + getProperty() + "\" value=\""
					+ String.valueOf(data == null ? "" : data) + "\" " + (readOnly ? " readonly=\"readonly\" " : "")
					+ " style=\"text-align:right;\" onkeypress=\"return allowNumbers(event, this.value, " + (column.getDecimalDigits() > 0 ? "true" : "false") + ")\" />";
		}
		if (isDateTime()) {
			output = output + "<script>$(function(){$('#" + getId() + "').datepicker();});</script>";
		}
		if (maxlen > 200 && !tableDescriptor.isMemberOfPK(columnName) && !tableDescriptor.isMemberOfFK(columnName) && isString()) {
			output = "<textarea style='height:3em;' name=\"" + getProperty() + "\"" + (readOnly ? " readonly=\"readonly\" " : "") + " >" + String.valueOf(data == null ? "" : data)
					+ "</textarea>";
		}
		ForeignKey matchingFK = tableDescriptor.matchingFK(this.columnName);
		if (matchingFK != null) {
			output = output + matchingFK.toHtmlLookupButton(columnName);
		}
		try {
			pageContext.getOut().print(output);
		} catch (IOException e) {
			throw new JspException("Could not paint the output", e);
		}
		return super.doEndTag();
	}

	public boolean isDateTime() {
		ColumnDescriptor column = this.tableDescriptor.getColumn(getColumnName());
		return Types.DATE == column.getJdbcType() || Types.TIME == column.getJdbcType() || Types.TIMESTAMP == column.getJdbcType();
	}

	public boolean isTime() {
		ColumnDescriptor column = this.tableDescriptor.getColumn(getColumnName());
		return Types.TIME == column.getJdbcType() || Types.TIMESTAMP == column.getJdbcType();
	}

	public boolean isNumeric() {
		ColumnDescriptor column = this.tableDescriptor.getColumn(getColumnName());
		return Types.TINYINT == column.getJdbcType() || Types.SMALLINT == column.getJdbcType() || Types.INTEGER == column.getJdbcType() || Types.BIGINT == column.getJdbcType()
				|| Types.FLOAT == column.getJdbcType() || Types.REAL == column.getJdbcType() || Types.DOUBLE == column.getJdbcType() || Types.NUMERIC == column.getJdbcType()
				|| Types.DECIMAL == column.getJdbcType();

	}

	public boolean isString() {
		ColumnDescriptor column = this.tableDescriptor.getColumn(getColumnName());
		return Types.CHAR == column.getJdbcType() || Types.VARCHAR == column.getJdbcType() || Types.LONGVARCHAR == column.getJdbcType();
	}
}
