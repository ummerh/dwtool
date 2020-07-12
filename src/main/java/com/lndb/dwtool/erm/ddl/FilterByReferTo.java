package com.lndb.dwtool.erm.ddl;

import com.lndb.dwtool.erm.ForeignKey;

public class FilterByReferTo implements IKeyFilter {

    private String referToTableName;

    public FilterByReferTo(String referToTableName) {
	super();
	this.referToTableName = referToTableName;
    }

    public boolean ignoreKey(ForeignKey foreignKey) {
	if (referToTableName.equalsIgnoreCase(foreignKey.getReferToTable())) {
	    return true;
	}
	return false;
    }

}
