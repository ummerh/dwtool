package com.lndb.dwtool.erm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DifferenceMap {
    private Set<String> tables = new HashSet<String>();
    private Map<String, List<ForeignKey>> missingFromSecond = new HashMap<String, List<ForeignKey>>();
    private Map<String, List<ForeignKey>> missingFromFirst = new HashMap<String, List<ForeignKey>>();

    public void compare(RelationalMap first, RelationalMap second) {
	List<String> allTables = first.getAllTables();
	for (String tblName : allTables) {
	    findMissingFromFirst(first, second, tblName);
	    findMissingFromSecond(first, second, tblName);
	}
    }

    private void findMissingFromFirst(RelationalMap first, RelationalMap second, String tblName) {
	List<ForeignKey> foreignKeys = second.getForeignKeys(tblName);
	if (foreignKeys != null) {
	    for (ForeignKey foreignKey : foreignKeys) {
		if (foreignKey.isValid() && !foreignKey.isReferToView() && !isDefined(first, foreignKey)) {
		    this.tables.add(tblName);
		    List<ForeignKey> secondList = this.missingFromFirst.get(tblName);
		    if (secondList == null) {
			secondList = new ArrayList<ForeignKey>();
			this.missingFromFirst.put(tblName, secondList);
		    }
		    secondList.add(foreignKey);
		}
	    }
	}
    }

    private void findMissingFromSecond(RelationalMap first, RelationalMap second, String tblName) {
	List<ForeignKey> foreignKeys = first.getForeignKeys(tblName);
	if (foreignKeys != null) {
	    for (ForeignKey foreignKey : foreignKeys) {
		if (foreignKey.isValid() && !foreignKey.isReferToView() && !isDefined(second, foreignKey)) {
		    this.tables.add(tblName);
		    List<ForeignKey> firstList = this.missingFromSecond.get(tblName);
		    if (firstList == null) {
			firstList = new ArrayList<ForeignKey>();
			this.missingFromSecond.put(tblName, firstList);
		    }
		    firstList.add(foreignKey);
		}
	    }
	}
    }

    public boolean isDefined(RelationalMap baseMap, ForeignKey targetKey) {
	List<ForeignKey> baseForeignKeys = baseMap.getForeignKeys(targetKey.getReferByTable());
	if (baseForeignKeys != null) {
	    for (ForeignKey baseKey : baseForeignKeys) {
		if (baseKey.isSameDefinition(targetKey)) {
		    return true;
		}
	    }
	}
	return false;
    }

    public Set<String> getAllTables() {
	return this.tables;
    }

    public Map<String, List<ForeignKey>> getMissingFromFirst() {
	return this.missingFromFirst;
    }

    public Map<String, List<ForeignKey>> getMissingFromSecond() {
	return this.missingFromSecond;
    }
}
