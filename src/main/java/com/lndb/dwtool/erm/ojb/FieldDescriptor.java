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

public class FieldDescriptor {
    private String name;
    private String column;
    private String jdbcType;
    private String conversion;
    private boolean primaryKey;
    private boolean index;
    private boolean locking;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getColumn() {
	return column;
    }

    public void setColumn(String column) {
	this.column = column;
    }

    public String getJdbcType() {
	return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
	this.jdbcType = jdbcType;
    }

    public boolean isPrimaryKey() {
	return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
	this.primaryKey = primaryKey;
    }

    public boolean isIndex() {
	return index;
    }

    public void setIndex(boolean index) {
	this.index = index;
    }

    public String getConversion() {
	return conversion;
    }

    public void setConversion(String conversion) {
	this.conversion = conversion;
    }

    public boolean isLocking() {
	return locking;
    }

    public void setLocking(boolean locking) {
	this.locking = locking;
    }
}
