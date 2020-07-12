package com.lndb.dwtool.erm.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

public class EntityDescriptor {
    private String tableName;
    private String className;
    private List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
    private Map<String, String> fieldMap = null;
    private List<ReferenceDescriptor> referenceDescriptors = new ArrayList<ReferenceDescriptor>();
    private List<String> columnNames;
    private List<String> pkColumns;
    private List<CollectionDescriptor> collectionDescriptors = new ArrayList<CollectionDescriptor>();

    public EntityDescriptor() {
    }

    public EntityDescriptor(String className, Table table) {
	this.className = className;
	this.tableName = table.name();
    }

    public void add(FieldDescriptor fieldDescriptor) {
	this.fieldDescriptors.add(fieldDescriptor);
    }

    public String getTableName() {
	return tableName;
    }

    public void setTableName(String name) {
	this.tableName = name;
    }

    public String getClassName() {
	return className;
    }

    public void setClassName(String className) {
	this.className = className;
    }

    public List<FieldDescriptor> getFieldDescriptors() {
	return fieldDescriptors;
    }

    public void setFieldDescriptors(List<FieldDescriptor> fieldDescriptors) {
	this.fieldDescriptors = fieldDescriptors;
    }

    public Map<String, String> getFieldMap() {
	return fieldMap;
    }

    public void setFieldMap(Map<String, String> fieldMap) {
	this.fieldMap = fieldMap;
    }

    public List<ReferenceDescriptor> getReferenceDescriptors() {
	return referenceDescriptors;
    }

    public void setReferenceDescriptors(List<ReferenceDescriptor> referenceDescriptors) {
	this.referenceDescriptors = referenceDescriptors;
    }

    public List<String> getColumnNames() {
	return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
	this.columnNames = columnNames;
    }

    public List<String> getPkColumns() {
	return pkColumns;
    }

    public void setPkColumns(List<String> pkColumns) {
	this.pkColumns = pkColumns;
    }

    /**
     * @return the collectionDescriptors
     */
    public List<CollectionDescriptor> getCollectionDescriptors() {
	return collectionDescriptors;
    }

    /**
     * @param collectionDescriptors
     *            the collectionDescriptors to set
     */
    public void setCollectionDescriptors(List<CollectionDescriptor> collectionDescriptors) {
	this.collectionDescriptors = collectionDescriptors;
    }

}
