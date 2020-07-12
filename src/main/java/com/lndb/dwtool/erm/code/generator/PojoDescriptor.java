/**
 * 
 */
package com.lndb.dwtool.erm.code.generator;

import java.util.List;

import com.lndb.dwtool.erm.ojb.FieldDescriptor;

/**
 * 
 */
public class PojoDescriptor {
    private String packageName;
    private String className;
    private String superClassName;
    private List<FieldDescriptor> fieldDescriptors;

    public String getPackageName() {
	return packageName;
    }

    public void setPackageName(String packageName) {
	this.packageName = packageName;
    }

    public String getClassName() {
	return className;
    }

    public void setClassName(String className) {
	this.className = className;
    }

    public String getSuperClassName() {
	return superClassName;
    }

    public void setSuperClassName(String superClassName) {
	this.superClassName = superClassName;
    }

    public List<FieldDescriptor> getFieldDescriptors() {
	return fieldDescriptors;
    }

    public void setFieldDescriptors(List<FieldDescriptor> fieldDescriptors) {
	this.fieldDescriptors = fieldDescriptors;
    }

}
