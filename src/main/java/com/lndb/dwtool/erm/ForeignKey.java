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
package com.lndb.dwtool.erm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ForeignKey {
    private String name;
    private String referToTable;
    private String referByTable;
    private TreeMap<Integer, String> referByCols = new TreeMap<Integer, String>();
    private TreeMap<Integer, String> referToCols = new TreeMap<Integer, String>();
    private Map<String, String> referMap = new HashMap<String, String>();
    private Map<String, String> inverseReferMap = new HashMap<String, String>();
    private boolean cascadeUpdate;

    public String getName() {
	if (name != null)
	    return name.toUpperCase();
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getReferToTable() {
	return referToTable;
    }

    public void setReferToTable(String referToTable) {
	this.referToTable = referToTable;
    }

    public List<String> getReferToCols() {
	return new ArrayList<String>(referToCols.values());
    }

    public List<String> getReferByCols() {
	return new ArrayList<String>(referByCols.values());
    }

    public String getReferByTable() {
	return referByTable;
    }

    public void setReferByTable(String referByTable) {
	this.referByTable = referByTable;
    }

    public boolean isValid() {
	return (referByCols != null && !referByCols.isEmpty() && referToCols != null && !referToCols.isEmpty() && (referByCols.size() == referToCols.size()) && (!Preferences.isIgnoreDBViews() || !isReferToView()));
    }

    public void addReferMapping(String referBy, String referTo) {
	this.referMap.put(referBy, referTo);
	this.inverseReferMap.put(referTo, referBy);
    }

    public void addReferByCol(Integer seq, String referByCol) {
	this.referByCols.put(seq, referByCol);

    }

    public void addReferToCol(Integer seq, String referToCol) {
	this.referToCols.put(seq, referToCol);
    }

    public boolean isSameDefinition(ForeignKey other) {
	if (other == null) {
	    return false;
	}
	if (referByTable == null) {
	    if (other.referByTable != null)
		return false;
	} else if (!referByTable.equalsIgnoreCase(other.referByTable))
	    return false;

	if (referToTable == null) {
	    if (other.referToTable != null)
		return false;
	} else if (!referToTable.equalsIgnoreCase(other.referToTable))
	    return false;

	if (referByCols == null) {
	    if (other.referByCols != null)
		return false;
	} else {
	    Set<Integer> keys = referByCols.keySet();
	    for (Integer key : keys) {
		String one = referByCols.get(key);
		String two = other.referByCols.get(key);
		if (!one.equalsIgnoreCase(two)) {
		    return false;
		}
	    }
	}
	if (referToCols == null) {
	    if (other.referToCols != null)
		return false;
	} else {
	    Set<Integer> keys = referToCols.keySet();
	    for (Integer key : keys) {
		String one = referToCols.get(key);
		String two = other.referToCols.get(key);
		if (!one.equalsIgnoreCase(two)) {
		    return false;
		}
	    }
	}

	return true;
    }

    public boolean isReferToView() {
	return (this.referToTable != null && this.referToTable.endsWith("_V"));
    }

    @Override
    public String toString() {
	return "Table " + getReferByTable() + " refers " + getReferToTable() + "" + getReferByCols() + "";
    }

    /**
     * @return the cascadeUpdate
     */
    public boolean isCascadeUpdate() {
	return cascadeUpdate;
    }

    /**
     * @param cascadeUpdate
     *            the cascadeUpdate to set
     */
    public void setCascadeUpdate(boolean cascadeUpdate) {
	this.cascadeUpdate = cascadeUpdate;
    }

    public String toHtmlLookupButton(String columnName) {
	return "<img name='action:maintenanceAction!lookupReference' onclick=\"return lookup('" + getName() + "','" + columnName
		+ "');\" border='0' width='20' height='20' src='./images/search.jpg' />";
    }

    public String getReferByColumn(String referTo) {
	return this.inverseReferMap.get(referTo);
    }

    public String getReferToColumn(String referBy) {
	return this.referMap.get(referBy);
    }
}
