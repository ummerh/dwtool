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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.FromXmlRuleSet;
import org.xml.sax.SAXException;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.RelationalMap;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.util.FileFinder;
import com.lndb.dwtool.erm.util.FileInputStreamWrapper;

public class OJBMap extends RelationalMap {
	private FromXmlRuleSet OJB_XML_RULE_SET = new FromXmlRuleSet(Thread.currentThread().getContextClassLoader().getResource("ojb-parser-rules.xml"));

	public static class OjbXmlFilter implements FileFilter {
		public static final String OJB_FILE_NAME_PATTERN = "[\\s\\S]*ojb[\\s\\S]*.xml";

		public boolean accept(File pathname) {
			String pathIgnoreCase = pathname.getName().toLowerCase();
			return pathIgnoreCase.matches(OJB_FILE_NAME_PATTERN);
		}
	}

	private Map<String, ClassDescriptor> classOjbMap = new HashMap<String, ClassDescriptor>();
	private Map<String, ClassDescriptor> tableOjbMap = new HashMap<String, ClassDescriptor>();
	private Map<String, String> classToTableMap = new HashMap<String, String>();
	private Map<String, String> tableToClassMap = new HashMap<String, String>();
	private HashSet<String> loadedFiles = new HashSet<String>();
	private HashMap<String, List<ReferenceDescriptor>> inverseReferences = new HashMap<String, List<ReferenceDescriptor>>();
	private boolean loaded;

	protected OJBMap() {
		super();
	}

	public void loadFromClasspath() {
		try {
			List<FileInputStreamWrapper> iss = FileFinder.getInstance().findFromSystemClasspath(new OjbXmlFilter());
			loadMap(iss);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void loadFromWebClasspath() {
		try {
			List<FileInputStreamWrapper> iss = FileFinder.getInstance().findFromWebClasspath(new OjbXmlFilter(), "ojb/xml/");
			loadMap(iss);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void loadFromRepositoryDir() {
		try {
			List<FileInputStreamWrapper> iss = FileFinder.getInstance().findFromOjbRepositoryDir(new OjbXmlFilter());
			loadMap(iss);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void loadFromDirectory(File sourceDir) {
		try {
			List<FileInputStreamWrapper> iss = FileFinder.getInstance().findFromDir(sourceDir, new OjbXmlFilter());
			loadMap(iss);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void loadFromFile(File ojbFile) {
		try {
			List<FileInputStreamWrapper> iss = new ArrayList<FileInputStreamWrapper>();
			iss.add(new FileInputStreamWrapper(new FileInputStream(ojbFile), ojbFile, ojbFile.getName()));
			loadMap(iss);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void loadMap(List<FileInputStreamWrapper> iss) throws IOException, SAXException {
		Digester digester = new Digester();
		digester.addRuleSet(OJB_XML_RULE_SET);
		for (FileInputStreamWrapper is : iss) {
			String fileName = is.getFileName();
			if (!this.loadedFiles.contains(fileName)) {
				ArrayList<ClassDescriptor> classDescriptors = (ArrayList<ClassDescriptor>) digester.parse(is.getInputStream());
				if (classDescriptors == null)
					continue;
				for (ClassDescriptor classDescriptor : classDescriptors) {
					addClassDescriptor(classDescriptor, fileName);
				}
				this.loadedFiles.add(fileName);

			} else {
				System.out.println("Duplicate OJB file name:" + fileName + " is ignored.");

			}
		}
		this.loaded = true;
	}

	public void addClassDescriptor(ClassDescriptor classDescriptor, String fileName) {
		classDescriptor.setOjbFileName(fileName);
		// always consider if class is coming from LNDB defined file
		if (fileName.toLowerCase().contains("lndb") || !this.classOjbMap.containsKey(classDescriptor.getClassName())) {
			this.classOjbMap.put(classDescriptor.getClassName(), classDescriptor);
			if (classDescriptor.getTableName() != null) {
				String tblUp = classDescriptor.getTableName().toUpperCase();
				if (this.tableOjbMap.get(tblUp) == null) {
					this.tableOjbMap.put(tblUp, classDescriptor);
					this.tableToClassMap.put(tblUp, classDescriptor.getClassName());
				} else {
					this.tableOjbMap.get(tblUp).addDuplicate(classDescriptor);
				}
				this.classToTableMap.put(classDescriptor.getClassName(), tblUp);
			} else {
				System.out.println("WARNING! Table mapping not found " + classDescriptor.getClassName());
			}

			// do inverse reference
			collectInverseReferences(classDescriptor);
		} else {
			ClassDescriptor exists = this.classOjbMap.get(classDescriptor.getClassName());
			exists.getDuplicateOjbFileNames().add(fileName);
		}
	}

	private void collectInverseReferences(ClassDescriptor classDescriptor) {
		List<CollectionDescriptor> collectionDescriptors = classDescriptor.getCollectionDescriptors();
		for (CollectionDescriptor collectionDescriptor : collectionDescriptors) {
			// create a inverse descriptor
			String referByClass = collectionDescriptor.getElementClassReference();
			List<String> referByFields = collectionDescriptor.getFieldRefs();
			ReferenceDescriptor invRef = new ReferenceDescriptor();
			invRef.setName(collectionDescriptor.getName() + "-ref");
			invRef.setAutoDelete(collectionDescriptor.getAutoDelete());
			invRef.setAutoUpdate(collectionDescriptor.getAutoUpdate());
			invRef.setAutoRetrieve(collectionDescriptor.getAutoRetrieve());
			invRef.setProxy(collectionDescriptor.getProxy());
			invRef.setClassReference(classDescriptor.getClassName());
			invRef.setFieldRefs(referByFields);

			if (this.inverseReferences.get(referByClass) == null) {
				this.inverseReferences.put(referByClass, new ArrayList<ReferenceDescriptor>());
			}
			this.inverseReferences.get(referByClass).add(invRef);
		}
	}

	public List<String> getColumns(String tableName) {
		ClassDescriptor classDescriptor = tableOjbMap.get(tableName);
		if (classDescriptor == null) {
			return null;
		}
		return classDescriptor.getColumnNames();
	}

	public String getClassNameForTable(String tableName) {
		return this.tableToClassMap.get(tableName.toUpperCase());
	}

	public String getTableNameForClass(String className) {
		return this.classToTableMap.get(className);
	}

	public List<String> getAllTables() {
		ArrayList<String> list = new ArrayList<String>(tableToClassMap.keySet());
		Collections.sort(list);
		return list;

	}

	public Set<String> getAllClasses() {
		return this.classToTableMap.keySet();
	}

	public List<ForeignKey> getForeignKeys(String tableName) {
		if (tableName == null) {
			return null;
		}
		List<ForeignKey> fkInfo = new ArrayList<ForeignKey>();
		ClassDescriptor classDescriptor = this.tableOjbMap.get(tableName.toUpperCase());
		if (classDescriptor == null) {
			return null;
		}
		List<ReferenceDescriptor> references = classDescriptor.getReferenceDescriptors();
		String childClassName = null;
		ClassDescriptor childClassDescriptor = null;
		ForeignKey fk = null;
		for (ReferenceDescriptor referenceDescriptor : references) {
			childClassName = referenceDescriptor.getClassReference();
			childClassDescriptor = this.classOjbMap.get(childClassName);
			fk = createFKDefinition(tableName, classDescriptor, childClassName, childClassDescriptor, referenceDescriptor);
			if (fk.isCascadeUpdate() && !checkInverseExists(tableName, childClassDescriptor)) {
				fkInfo.add(fk);
			} else if (!fk.isCascadeUpdate()) {
				fkInfo.add(fk);
			}
		}
		List<ReferenceDescriptor> invReferences = this.inverseReferences.get(classDescriptor.getClassName());
		if (invReferences != null) {
			for (ReferenceDescriptor referenceDescriptor : invReferences) {
				childClassName = referenceDescriptor.getClassReference();
				childClassDescriptor = this.classOjbMap.get(childClassName);
				fk = createFKDefinition(tableName, classDescriptor, childClassName, childClassDescriptor, referenceDescriptor);
				if (fk.isCascadeUpdate()) {
					fkInfo.add(fk);
				}
			}
		}
		return fkInfo;
	}

	private boolean checkInverseExists(String tableName, ClassDescriptor childClassDescriptor) {
		if (childClassDescriptor == null) {
			return false;
		}
		List<ReferenceDescriptor> referenceDescriptors = childClassDescriptor.getReferenceDescriptors();
		boolean inverseExists = false;
		for (ReferenceDescriptor referenceDescriptor : referenceDescriptors) {
			if (referenceDescriptor.getClassReference().equalsIgnoreCase(this.tableToClassMap.get(tableName))) {
				inverseExists = true;
				break;
			}
		}
		return inverseExists;
	}

	private ForeignKey createFKDefinition(String tableName, ClassDescriptor classDescriptor, String childClassName, ClassDescriptor childClassDescriptor, ReferenceDescriptor referenceDescriptor) {
		ForeignKey fk;
		fk = new ForeignKey();
		fk.setReferByTable(tableName);
		fk.setName(referenceDescriptor.getName());
		if (childClassDescriptor != null) {
			fk.setReferToTable(getTableNameForClass(childClassName));
		}
		if ("true".equals(referenceDescriptor.getAutoUpdate()) || "object".equals(referenceDescriptor.getAutoUpdate())) {
			fk.setCascadeUpdate(true);
		}
		return fk;
	}

	public List<ForeignKey> getExportedKeys(String tableName) {
		Collection<ClassDescriptor> classDescriptors = this.classOjbMap.values();
		List<ForeignKey> refByList = new ArrayList<ForeignKey>();
		for (ClassDescriptor classDescriptor : classDescriptors) {
			String childTable = classDescriptor.getTableName();
			if (childTable != null && !tableName.equalsIgnoreCase(childTable)) {
				List<ForeignKey> references = getForeignKeys(childTable);
				for (ForeignKey foreignKey : references) {
					if (tableName.equalsIgnoreCase(foreignKey.getReferToTable())) {
						refByList.add(foreignKey);
					}
				}
			}
		}
		return refByList;
	}

	public List<String> getPrimaryKeys(String table) {
		ClassDescriptor classDescriptor = this.tableOjbMap.get(table);
		if (classDescriptor == null) {
			return null;
		}
		return classDescriptor.getPrimaryKeyColumns();
	}

	public ClassDescriptor getClassDescriptor(String tableName) {
		return this.tableOjbMap.get(tableName.toUpperCase());
	}

	public void writeMap(Writer out) throws IOException {
		if (!loaded) {
			throw new RuntimeException("OJB Mapping is not loaded");
		}
		BufferedWriter writer = new BufferedWriter(out);
		try {
			SortedSet<String> sortedList = new TreeSet<String>(this.getAllTables());
			for (String table : sortedList) {
				writer.write("TABLE: " + table);
				writer.newLine();
				writer.write("BO: " + this.getClassNameForTable(table));
				writer.newLine();
				writer.write("Keys: " + this.getPrimaryKeys(table));
				writer.newLine();
				writer.write("*************************");
				writer.newLine();
				writer.newLine();
				printFKRelations(writer, table);
				writer.write("*************************");
				writer.newLine();
				printSelfReferences(writer, table);
				writer.write("*************************");
				writer.newLine();
				writer.newLine();
				printInverseReferences(writer, table);
				writer.write("*************************");
				writer.newLine();
				writer.newLine();
			}
			writer.write("end");
			writer.newLine();
		} finally {
			writer.flush();
			writer.close();
		}
	}

	public Set<String> getAutoUpdateableReferences(String tableName) {
		if (tableName == null) {
			return null;
		}
		Set<String> updateableReferences = new HashSet<String>();
		identifyUpdateableReferences(tableName, updateableReferences);
		return updateableReferences;
	}

	private void identifyUpdateableReferences(String tableName, Set<String> updateableReferences) {
		if (tableName == null) {
			return;
		}
		ClassDescriptor classDescriptor = this.tableOjbMap.get(tableName.toUpperCase());
		if (classDescriptor == null) {
			return;
		}
		List<ClassDescriptor> duplicates = classDescriptor.getDuplicates();
		int dupCount = duplicates.size();
		int i = 0;
		while (!duplicates.isEmpty() && !classDescriptor.updatesChildren() && i < dupCount) {
			classDescriptor = duplicates.get(i);
			i++;
		}
		List<ReferenceDescriptor> references = classDescriptor.getReferenceDescriptors();
		String tableNameForClass = null;
		for (ReferenceDescriptor referenceDescriptor : references) {
			if ("true".equals(referenceDescriptor.getAutoUpdate()) || "object".equals(referenceDescriptor.getAutoUpdate())) {
				tableNameForClass = getTableNameForClass(referenceDescriptor.getClassReference());
				if (updateableReferences.add(tableNameForClass)) {
					identifyUpdateableReferences(tableNameForClass, updateableReferences);
				}
			}
		}
		List<CollectionDescriptor> invReferences = classDescriptor.getCollectionDescriptors();
		if (invReferences != null) {
			for (CollectionDescriptor collectionDescriptor : invReferences) {
				if ("true".equals(collectionDescriptor.getAutoUpdate()) || "object".equals(collectionDescriptor.getAutoUpdate())) {
					tableNameForClass = getTableNameForClass(collectionDescriptor.getElementClassReference());
					if (updateableReferences.add(tableNameForClass)) {
						identifyUpdateableReferences(tableNameForClass, updateableReferences);
					}
				}
			}
		}
	}

	public List<String> validateAgainstDBSchema(DBMap dbMap, String packagePrefix) {
		List<String> errors = new ArrayList<String>();
		for (String classNm : classOjbMap.keySet()) {
			if (classNm.endsWith("View") || (packagePrefix != null && !classNm.startsWith(packagePrefix))) {
				continue;
			}
			ClassDescriptor classDescriptor = classOjbMap.get(classNm);
			if (classDescriptor == null) {
				errors.add("," + classNm + ", , Class descriptor is null");
				continue;
			}
			if (classDescriptor.getTableName() == null) {
				errors.add(classDescriptor.getOjbFileName() + "," + classNm + ", , Table name is null");
				continue;
			}
			if (!classDescriptor.getDuplicateOjbFileNames().isEmpty()) {
				errors.add(classDescriptor.getOjbFileName() + "," + classNm + "," + classDescriptor.getTableName() + ",  Duplicate(s): " + classDescriptor.getDuplicateOjbFileNames());
			}
			String tblNm = classDescriptor.getTableName().toUpperCase();
			TableDescriptor tableDescriptor = dbMap.getTableDescriptor(tblNm);
			if (tableDescriptor == null) {
				errors.add(classDescriptor.getOjbFileName() + "," + classNm + "," + classDescriptor.getTableName() + ", Table not found in DB");
				continue;
			}
			HashSet<String> cols = new HashSet<String>();
			for (FieldDescriptor field : classDescriptor.getFieldDescriptors()) {
				String col = field.getColumn().toUpperCase();
				cols.add(col);
				ColumnDescriptor column = tableDescriptor.getColumn(col);
				if (column == null) {
					errors.add(classDescriptor.getOjbFileName() + "," + classNm + "," + classDescriptor.getTableName() + ", Column not matched in table: " + field.getName());
				}
			}
			List<ColumnDescriptor> columns = tableDescriptor.getColumns();
			for (ColumnDescriptor columnDescriptor : columns) {
				if (!cols.contains(columnDescriptor.getName())) {
					errors.add(classDescriptor.getOjbFileName() + "," + classNm + "," + classDescriptor.getTableName() + ", Column not mapped in OJB: " + columnDescriptor.getName() + " [nullable: "
							+ columnDescriptor.isNullable() + "] ");

				}
			}
		}
		List<String> allTables = dbMap.getAllTables();
		for (String tblNm : allTables) {
			if (tableOjbMap.get(tblNm) == null) {
				errors.add(",," + tblNm + " ,No OJB mapping");
			}
		}
		return errors;
	}

	public void printUpdateableReferences(String tableName, Set<String> updateableReferences, StringBuilder sb) {
		if (tableName == null) {
			return;
		}
		ClassDescriptor classDescriptor = this.tableOjbMap.get(tableName.toUpperCase());
		if (classDescriptor == null) {
			return;
		}

		List<ClassDescriptor> duplicates = classDescriptor.getDuplicates();
		int dupCount = duplicates.size();
		int i = 0;
		while (!duplicates.isEmpty() && !classDescriptor.updatesChildren() && i < dupCount) {
			classDescriptor = duplicates.get(i);
			i++;
		}
		List<ReferenceDescriptor> references = classDescriptor.getReferenceDescriptors();
		List<CollectionDescriptor> invReferences = classDescriptor.getCollectionDescriptors();
		String tableNameForClass = null;
		for (ReferenceDescriptor referenceDescriptor : references) {
			if ("true".equals(referenceDescriptor.getAutoUpdate()) || "object".equals(referenceDescriptor.getAutoUpdate())) {
				tableNameForClass = getTableNameForClass(referenceDescriptor.getClassReference());
				if (updateableReferences.add(tableNameForClass)) {
					sb.append("{" + tableName + "> ref >" + tableNameForClass + "} ");
					printUpdateableReferences(tableNameForClass, updateableReferences, sb);
				}
			}
		}
		if (invReferences != null) {
			for (CollectionDescriptor collectionDescriptor : invReferences) {
				if ("true".equals(collectionDescriptor.getAutoUpdate()) || "object".equals(collectionDescriptor.getAutoUpdate())) {
					tableNameForClass = getTableNameForClass(collectionDescriptor.getElementClassReference());
					if (updateableReferences.add(tableNameForClass)) {
						sb.append("{" + tableName + "> col >" + tableNameForClass + "} ");
						printUpdateableReferences(tableNameForClass, updateableReferences, sb);
					}
				}
			}
		}
	}
}
