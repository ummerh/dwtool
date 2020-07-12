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
import java.util.List;

public abstract class AbstractReferenceDescriptor {

    private String autoDelete;
    private String autoUpdate;
    private String autoRetrieve;
    private String name;
    private String proxy;
    private List<String> fieldRefs = new ArrayList<String>();

    public void addReferenceField(String field) {
	fieldRefs.add(field);
    }

    public String getAutoDelete() {
	return autoDelete;
    }

    public void setAutoDelete(String autoDelete) {
	this.autoDelete = autoDelete;
    }

    public String getAutoUpdate() {
	return autoUpdate;
    }

    public void setAutoUpdate(String autoUpdate) {
	this.autoUpdate = autoUpdate;
    }

    public String getAutoRetrieve() {
	return autoRetrieve;
    }

    public void setAutoRetrieve(String autoRetrieve) {
	this.autoRetrieve = autoRetrieve;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getProxy() {
	return proxy;
    }

    public void setProxy(String proxy) {
	this.proxy = proxy;
    }

    public List<String> getFieldRefs() {
	return fieldRefs;
    }

    public void setFieldRefs(List<String> fieldRefs) {
	this.fieldRefs = fieldRefs;
    }

}
