/*
 * Copyright 2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lndb.dwtool.erm.ojb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDescriptor {
	private String className;
	private String tableName;
	private String packageName;
	private String superClassName;
	private List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
	private Map<String, String> fieldMap = null;
	private List<ReferenceDescriptor> referenceDescriptors = new ArrayList<ReferenceDescriptor>();
	private List<CollectionDescriptor> collectionDescriptors = new ArrayList<CollectionDescriptor>();
	private List<String> columnNames;
	private List<String> pkColumns;
	private String ojbFileName;
	private List<String> duplicateOjbFileNames = new ArrayList<String>();
	private List<ClassDescriptor> duplicates = new ArrayList<ClassDescriptor>(0);

	public void add(FieldDescriptor fieldDescriptor) {
		this.fieldDescriptors.add(fieldDescriptor);
	}

	public List<String> getColumnNames() {
		if (columnNames == null) {
			populateColumnNames();
		}
		return columnNames;
	}

	private void populateColumnNames() {
		columnNames = new ArrayList<String>();
		for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
			String column = fieldDescriptor.getColumn();
			if (column != null) {
				columnNames.add(column);
			}
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void add(ReferenceDescriptor referenceDescriptor) {
		this.referenceDescriptors.add(referenceDescriptor);
	}

	public void add(CollectionDescriptor collectionDescriptor) {
		this.collectionDescriptors.add(collectionDescriptor);
	}

	public List<FieldDescriptor> getFieldDescriptors() {
		return fieldDescriptors;
	}

	public void setFieldDescriptors(List<FieldDescriptor> fieldDescriptors) {
		this.fieldDescriptors = fieldDescriptors;
	}

	public List<ReferenceDescriptor> getReferenceDescriptors() {
		return referenceDescriptors;
	}

	public void setReferenceDescriptors(List<ReferenceDescriptor> referenceDescriptors) {
		this.referenceDescriptors = referenceDescriptors;
	}

	public List<CollectionDescriptor> getCollectionDescriptors() {
		return collectionDescriptors;
	}

	public void setCollectionDescriptors(List<CollectionDescriptor> collectionDescriptors) {
		this.collectionDescriptors = collectionDescriptors;
	}

	public String getColumnName(String name) {
		if (this.fieldMap == null) {
			populateFieldMap();
		}
		return this.fieldMap.get(name);
	}

	private void populateFieldMap() {
		this.fieldMap = new HashMap<String, String>();
		for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
			this.fieldMap.put(fieldDescriptor.getName(), fieldDescriptor.getColumn());
		}
	}

	public List<String> getPrimaryKeyColumns() {
		if (pkColumns == null) {
			populatePrimaryKeyColumns();
		}
		return pkColumns;
	}

	private void populatePrimaryKeyColumns() {
		pkColumns = new ArrayList<String>();
		for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
			if (fieldDescriptor.isPrimaryKey()) {
				pkColumns.add(fieldDescriptor.getColumn());
			}
		}
	}

	public List<String> getForeignKeyColumns(ReferenceDescriptor referenceDescriptor) {
		List<String> parentCols = new ArrayList<String>();
		List<String> refFields = referenceDescriptor.getFieldRefs();
		for (String refFieldName : refFields) {
			parentCols.add(getColumnName(refFieldName));
		}
		return parentCols;
	}

	/**
	 * @return the superClassName
	 */
	public String getSuperClassName() {
		return superClassName;
	}

	/**
	 * @param superClassName
	 *            the superClassName to set
	 */
	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getOjbFileName() {
		return ojbFileName;
	}

	public void setOjbFileName(String ojbFileName) {
		this.ojbFileName = ojbFileName;
	}

	public List<String> getDuplicateOjbFileNames() {
		return duplicateOjbFileNames;
	}

	public void setDuplicateOjbFileNames(List<String> duplicateOjbFileNames) {
		this.duplicateOjbFileNames = duplicateOjbFileNames;
	}

	public List<ClassDescriptor> getDuplicates() {
		return duplicates;
	}

	public void setDuplicates(List<ClassDescriptor> duplicates) {
		this.duplicates = duplicates;
	}

	public void addDuplicate(ClassDescriptor clsDescriptor) {
		this.duplicates.add(clsDescriptor);
	}

	public boolean updatesChildren() {

		List<ReferenceDescriptor> references = getReferenceDescriptors();

		for (ReferenceDescriptor referenceDescriptor : references) {
			if ("true".equals(referenceDescriptor.getAutoUpdate()) || "object".equals(referenceDescriptor.getAutoUpdate())) {
				return true;
			}
		}
		List<CollectionDescriptor> invReferences = getCollectionDescriptors();
		if (invReferences != null) {
			for (CollectionDescriptor collectionDescriptor : invReferences) {
				if ("true".equals(collectionDescriptor.getAutoUpdate()) || "object".equals(collectionDescriptor.getAutoUpdate())) {
					return true;
				}
			}
		}

		return false;

	}
}
