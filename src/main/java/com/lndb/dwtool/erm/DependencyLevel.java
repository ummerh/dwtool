package com.lndb.dwtool.erm;

import java.util.List;

public class DependencyLevel {
    private Integer pos;
    private String dependencyByTable;
    private String dependencyToTable;
    private List<String> dependencyByCols;

    public DependencyLevel() {
    }

    public Integer getPos() {
	return pos;
    }

    public void setPos(Integer pos) {
	this.pos = pos;
    }

    public String getDependencyByTable() {
	return dependencyByTable;
    }

    public void setDependencyByTable(String dependencyByTable) {
	this.dependencyByTable = dependencyByTable;
    }

    public String getDependencyToTable() {
	return dependencyToTable;
    }

    public void setDependencyToTable(String dependencyToTable) {
	this.dependencyToTable = dependencyToTable;
    }

    public List<String> getDependencyByCols() {
	return dependencyByCols;
    }

    public String getDependencyColsList() {
	if (this.dependencyByCols == null) {
	    return "";
	}
	return dependencyByCols.toString();
    }

    public void setDependencyByCols(List<String> dependencyByCols) {
	this.dependencyByCols = dependencyByCols;
    }

}
