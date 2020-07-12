package com.lndb.dwtool.erm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DependencyLevelMap {
    private HashSet<String> includedList = new HashSet<String>();
    private String tableName;
    private String referByTableName;
    private List<DependencyLevel> currentDependencyList;
    int level = 0;
    private Map<Integer, List<DependencyLevel>> dependencyMap = new TreeMap<Integer, List<DependencyLevel>>();

    public String getTableName() {
	return tableName;
    }

    public void setTableName(String tableName) {
	this.tableName = tableName;
    }

    public DependencyLevelMap() {
    }

    public void load(SchemaJoinMetaData schemaJoinMap, String tableName) {
	this.includedList.clear();
	this.dependencyMap.clear();
	List<ForeignKey> referenceToTables = schemaJoinMap.getImportedKeys(tableName);
	referByTableName = tableName;
	findLevel(schemaJoinMap, referenceToTables);
    }

    private void findLevel(SchemaJoinMetaData schemaJoinMap, List<ForeignKey> referencedToTables) {
	for (int j = 0; j < referencedToTables.size(); j++) {
	    if (j == 0) {
		++level;
		List<DependencyLevel> dependencyLsit = dependencyMap.get(this.level);
		if (dependencyLsit == null) {
		    dependencyLsit = new ArrayList<DependencyLevel>();
		    dependencyMap.put(level, dependencyLsit);
		    currentDependencyList = dependencyLsit;

		}
	    }
	    ForeignKey currentKey = referencedToTables.get(j);
	    String referToTable = currentKey.getReferToTable();

	    if (!referByTableName.equals(currentKey.getReferByTable())) {
		referByTableName = currentKey.getReferByTable();
	    }

	    DependencyLevel dependencyLevel = new DependencyLevel();
	    dependencyLevel.setPos(level);
	    dependencyLevel.setDependencyByTable(referByTableName);
	    dependencyLevel.setDependencyToTable(referToTable);
	    dependencyLevel.setDependencyByCols(currentKey.getReferByCols());

	    this.currentDependencyList.add(dependencyLevel);
	    iterateLevels(schemaJoinMap, referencedToTables, j);
	}
    }

    private void iterateLevels(SchemaJoinMetaData schemaJoinMap, List<ForeignKey> referencedToTables, int j) {
	if (j == referencedToTables.size() - 1) {
	    List<ForeignKey> newReferenceToTables = new ArrayList<ForeignKey>();
	    for (ForeignKey foreignKey : referencedToTables) {
		String curReferToTable = foreignKey.getReferToTable();
		if (this.includedList.add(curReferToTable)) {
		    newReferenceToTables.addAll(schemaJoinMap.getImportedKeys(curReferToTable));
		}
	    }
	    findLevel(schemaJoinMap, newReferenceToTables);
	}
    }

    public int totalLevels() {
	return this.dependencyMap.size();
    }

    public List<DependencyLevel> getLevelDetails(int pos) {
	return this.dependencyMap.get(pos);
    }

    public Collection<List<DependencyLevel>> getAllLevelDetails() {
	return this.dependencyMap.values();
    }

    public String getReferByTableName() {
	return referByTableName;
    }

    public void setReferByTableName(String referByTableName) {
	this.referByTableName = referByTableName;
    }

    public Map<Integer, List<DependencyLevel>> getDependencyMap() {
	return dependencyMap;
    }

    public void setDependencyMap(Map<Integer, List<DependencyLevel>> dependencyMap) {
	this.dependencyMap = dependencyMap;
    }

}
