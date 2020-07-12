package com.lndb.dwtool.erm.db;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.ddl.DDL;
import com.lndb.dwtool.erm.util.JavaSourceUtil;

public class ColumnDescriptor {
    private String name;
    private String typeName;
    private int size;
    private String tableName;
    private int decimalDigits;
    private boolean nullable;
    private int jdbcType;
    private String sqlDataType;
    private String colDefault;
    private Integer ordinalPosition;

    public Integer getOrdinalPosition() {
	return ordinalPosition;
    }

    public void setOrdinalPosition(Integer ordinalPosition) {
	this.ordinalPosition = ordinalPosition;
    }

    public boolean isNullable() {
	return nullable;
    }

    public void setNullable(boolean nullable) {
	this.nullable = nullable;
    }

    public String getName() {
	if (name != null)
	    return name.toUpperCase();
	return name;
    }

    public int getDecimalDigits() {
	return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
	this.decimalDigits = decimalDigits;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getTypeName() {
	return typeName;
    }

    public void setTypeName(String jdbcType) {
	this.typeName = jdbcType;
    }

    public int getSize() {
	return size;
    }

    public void setSize(int size) {
	this.size = size;
    }

    public String getTableName() {
	return tableName;
    }

    public void setTableName(String tableName) {
	this.tableName = tableName;
    }

    @Override
    public String toString() {
	return ("" + name + ":" + jdbcType + ":" + size + ":" + ":" + decimalDigits + ":" + nullable).toUpperCase();
    }

    public int getJdbcType() {
	return jdbcType;
    }

    public void setJdbcType(int jdbcType) {
	this.jdbcType = jdbcType;
    }

    public String getSqlDataType() {
	if (this.sqlDataType == null) {
	    setSqlDataType(DDL.getSqlDataType(this));
	}
	return sqlDataType;
    }

    public void setSqlDataType(String sqlDataType) {
	this.sqlDataType = sqlDataType;
    }

    public String getPojoFieldName() {
	return JavaSourceUtil.buildJavaFieldName(getName(), "_");
    }

    public String getColDefault() {
	if (colDefault != null) {
	    this.colDefault = this.colDefault.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "").trim();
	}
	return colDefault;
    }

    public void setColDefault(String colDefault) {
	this.colDefault = colDefault;
    }

    public boolean isSame(ColumnDescriptor other) {
	if (other == null) {
	    return false;
	}

	if (tableName == null) {
	    if (other.tableName != null)
		return false;
	} else if (!tableName.equalsIgnoreCase(other.tableName))
	    return false;

	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equalsIgnoreCase(other.name)) {
	    return false;
	} else if ("OBJ_ID".equalsIgnoreCase(name) || "VER_NBR".equalsIgnoreCase(name)) {
	    // dont compare these columns
	    return true;
	}

	if (jdbcType != other.jdbcType)
	    return false;

	if (decimalDigits != other.decimalDigits)
	    return false;
	if (size != other.size)
	    return false;

	if (!isDefualtEqual(other)) {
	    return false;
	}

	if (!isNullableEqual(other)) {
	    return false;
	}
	return true;
    }

    public boolean isSizeIncreased(ColumnDescriptor other) {
	if (decimalDigits > other.decimalDigits)
	    return true;
	if (size > other.size)
	    return true;
	return false;
    }

    public boolean isDefaultAvailable(ColumnDescriptor other) {
	if (StringUtils.isNotBlank(getColDefault()) && StringUtils.isBlank(other.getColDefault())) {
	    return true;
	}
	return false;
    }

    public boolean isDefualtEqual(ColumnDescriptor other) {
	// because in some cases values are string "null"
	// instead of null
	String one = getColDefault() == null ? "null" : getColDefault();
	String two = other.getColDefault() == null ? "null" : other.getColDefault();
	return one.equals(two);
    }

    public boolean isSizeEqual(ColumnDescriptor other) {
	if (decimalDigits != other.decimalDigits)
	    return false;
	if (size != other.size)
	    return false;
	return true;
    }

    public boolean isJdbcTypeEqual(ColumnDescriptor other) {
	if (jdbcType != other.jdbcType)
	    return false;
	return true;
    }

    public boolean isNullableEqual(ColumnDescriptor other) {
	if (nullable != other.nullable && (StringUtils.isBlank(getColDefault()) && nullable))
	    return false;
	return true;
    }

    public boolean isSqlDataTypeEqual(ColumnDescriptor other) {
	if (sqlDataType == null) {
	    if (other.sqlDataType != null)
		return false;
	} else if (!sqlDataType.equals(other.sqlDataType))
	    return false;
	return true;
    }

    public String toSpecString() {
	return ("Type:" + getSqlDataType() + ", Null:" + isNullable() + ", Default:" + getColDefault());
    }
}
