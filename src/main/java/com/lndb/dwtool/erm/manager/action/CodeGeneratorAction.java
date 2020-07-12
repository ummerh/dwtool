/**
 * 
 */
package com.lndb.dwtool.erm.manager.action;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.lndb.dwtool.erm.code.generator.PojoGenerator;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.ojb.ClassDescriptor;
import com.lndb.dwtool.erm.ojb.FieldDescriptor;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.util.JavaSourceUtil;
import com.opensymphony.xwork2.ActionSupport;

/**
 * 
 */
public class CodeGeneratorAction extends ActionSupport {
    private static final long serialVersionUID = -2164504733277732432L;
    private String connectionName;
    private String tableName;
    private ClassDescriptor classDescriptor;
    private TableDescriptor tableDescriptor;
    private HashMap<String, FieldDescriptor> fieldMap = new HashMap<String, FieldDescriptor>();

    /**
     * @return the connectionName
     */
    public String getConnectionName() {
	return connectionName;
    }

    /**
     * @param connectionName
     *            the connectionName to set
     */
    public void setConnectionName(String connectionName) {
	this.connectionName = connectionName;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
	return tableName;
    }

    /**
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(String tableName) {
	this.tableName = tableName;
    }

    /**
     * @return the classDescriptor
     */
    public ClassDescriptor getClassDescriptor() {
	return classDescriptor;
    }

    /**
     * @param classDescriptor
     *            the classDescriptor to set
     */
    public void setClassDescriptor(ClassDescriptor classDescriptor) {
	this.classDescriptor = classDescriptor;
    }

    public String displayInput() throws Exception {
	this.tableDescriptor = DBMapCache.getDBMap(connectionName).getTableDescriptor(tableName);
	List<ColumnDescriptor> columns = this.tableDescriptor.getColumns();
	for (ColumnDescriptor columnDescriptor : columns) {
	    FieldDescriptor fieldDescriptor = new FieldDescriptor();
	    fieldDescriptor.setColumn(columnDescriptor.getName());
	    fieldDescriptor.setName(columnDescriptor.getPojoFieldName());
	    this.fieldMap.put(columnDescriptor.getName(), fieldDescriptor);
	}
	OJBMap ojbMap = OJBMapCache.getOJBMap();
	ClassDescriptor classDescriptorVal = ojbMap.getClassDescriptor(getTableName());
	if (classDescriptorVal != null) {
	    this.classDescriptor = classDescriptorVal;
	} else {
	    this.classDescriptor = new ClassDescriptor();
	    this.classDescriptor.setClassName(JavaSourceUtil.buildJavaClassName(tableDescriptor.getTableName(), "_"));
	}
	return "input";
    }

    /**
     * @return the tableDescriptor
     */
    public TableDescriptor getTableDescriptor() {
	return tableDescriptor;
    }

    /**
     * @param tableDescriptor
     *            the tableDescriptor to set
     */
    public void setTableDescriptor(TableDescriptor tableDescriptor) {
	this.tableDescriptor = tableDescriptor;
    }

    /**
     * @return the fieldMap
     */
    public HashMap<String, FieldDescriptor> getFieldMap() {
	return fieldMap;
    }

    /**
     * @param fieldMap
     *            the fieldMap to set
     */
    public void setFieldMap(HashMap<String, FieldDescriptor> fieldMap) {
	this.fieldMap = fieldMap;
    }

    public String generatePojoBo() throws Exception {
	Collection<FieldDescriptor> values = fieldMap.values();
	for (FieldDescriptor fieldDescriptor : values) {
	    System.out.println(fieldDescriptor.getName());
	}
	HttpServletResponse response = ServletActionContext.getResponse();
	response.setContentType("text/java");
	response.setHeader("Content-disposition", "attachment; filename=\"" + JavaSourceUtil.buildJavaClassName(getTableName(), "_") + ".java" + "\"");
	PrintWriter out = response.getWriter();
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	TableDescriptor tableDescriptor = dbMap.getTableDescriptor(getTableName());
	PojoGenerator.toPojo(tableDescriptor, out);
	return "input";
    }

    public FieldDescriptor getFieldDescriptor(String column) {
	return this.fieldMap.get(column);
    }
}
