package com.lndb.dwtool.erm.jpa;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;

public class ReferenceDescriptor {
    private String name;
    private String[] referencedColumnNames;
    private boolean unique;
    private boolean nullable;
    private boolean insertable;
    private boolean updatable;
    private String columnDefinition;
    private String table;

    public ReferenceDescriptor() {

    }

    public ReferenceDescriptor(JoinColumn join) {
	this.referencedColumnNames = new String[1];
	this.referencedColumnNames[0] = join.referencedColumnName();
	this.name = join.name();
	this.unique = join.unique();
	this.nullable = join.nullable();
	this.insertable = join.insertable();
	this.updatable = join.updatable();
	this.columnDefinition = join.columnDefinition();
	this.table = join.table();
    }

    public ReferenceDescriptor(JoinColumns joinColumns) {
	JoinColumn[] joins = joinColumns.value();
	if (joins != null) {
	    this.referencedColumnNames = new String[joins.length];
	    for (int i = 0; i < joins.length; i++) {
		this.referencedColumnNames[i] = joins[i].referencedColumnName();
		this.name = joins[i].name();
		this.unique = joins[i].unique();
		this.nullable = joins[i].nullable();
		this.insertable = joins[i].insertable();
		this.updatable = joins[i].updatable();
		this.columnDefinition = joins[i].columnDefinition();
		this.table = joins[i].table();
	    }
	}
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the referencedColumnNames
     */
    public String[] getReferencedColumnNames() {
	return referencedColumnNames;
    }

    /**
     * @param referencedColumnNames
     *            the referencedColumnNames to set
     */
    public void setReferencedColumnNames(String[] referencedColumnNames) {
	this.referencedColumnNames = referencedColumnNames;
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

}
