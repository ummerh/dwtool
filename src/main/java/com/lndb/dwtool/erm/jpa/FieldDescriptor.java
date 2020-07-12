package com.lndb.dwtool.erm.jpa;

import javax.persistence.Column;

public class FieldDescriptor {
    private String column;
    private boolean unique;
    private boolean nullable;
    private boolean insertable;
    private boolean updatable;
    private String columnDefinition;
    private String table;
    private int length;
    private int precision;
    private int scale;
    private String field;

    public FieldDescriptor() {
    }

    public FieldDescriptor(Column column) {
	this.column = column.name();
	this.unique = column.unique();
	this.nullable = column.nullable();
	this.insertable = column.insertable();
	this.updatable = column.updatable();
	this.columnDefinition = column.columnDefinition();
	this.table = column.table();
	this.length = column.length();
	this.precision = column.precision();
	this.scale = column.scale();
    }

    /**
     * @return the column
     */
    public String getColumn() {
	return column;
    }

    /**
     * @param column
     *            the column to set
     */
    public void setColumn(String column) {
	this.column = column;
    }

    /**
     * @return the unique
     */
    public boolean isUnique() {
	return unique;
    }

    /**
     * @param unique
     *            the unique to set
     */
    public void setUnique(boolean unique) {
	this.unique = unique;
    }

    /**
     * @return the nullable
     */
    public boolean isNullable() {
	return nullable;
    }

    /**
     * @param nullable
     *            the nullable to set
     */
    public void setNullable(boolean nullable) {
	this.nullable = nullable;
    }

    /**
     * @return the insertable
     */
    public boolean isInsertable() {
	return insertable;
    }

    /**
     * @param insertable
     *            the insertable to set
     */
    public void setInsertable(boolean insertable) {
	this.insertable = insertable;
    }

    /**
     * @return the updatable
     */
    public boolean isUpdatable() {
	return updatable;
    }

    /**
     * @param updatable
     *            the updatable to set
     */
    public void setUpdatable(boolean updatable) {
	this.updatable = updatable;
    }

    /**
     * @return the columnDefinition
     */
    public String getColumnDefinition() {
	return columnDefinition;
    }

    /**
     * @param columnDefinition
     *            the columnDefinition to set
     */
    public void setColumnDefinition(String columnDefinition) {
	this.columnDefinition = columnDefinition;
    }

    /**
     * @return the table
     */
    public String getTable() {
	return table;
    }

    /**
     * @param table
     *            the table to set
     */
    public void setTable(String table) {
	this.table = table;
    }

    /**
     * @return the length
     */
    public int getLength() {
	return length;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(int length) {
	this.length = length;
    }

    /**
     * @return the precision
     */
    public int getPrecision() {
	return precision;
    }

    /**
     * @param precision
     *            the precision to set
     */
    public void setPrecision(int precision) {
	this.precision = precision;
    }

    /**
     * @return the scale
     */
    public int getScale() {
	return scale;
    }

    /**
     * @param scale
     *            the scale to set
     */
    public void setScale(int scale) {
	this.scale = scale;
    }

    /**
     * @return the field
     */
    public String getField() {
	return field;
    }

    /**
     * @param field
     *            the field to set
     */
    public void setField(String field) {
	this.field = field;
    }

}
