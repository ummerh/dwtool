package com.lndb.dwtool.erm.db.data;

import org.apache.struts2.ServletActionContext;

import com.lndb.dwtool.erm.db.TableDescriptor;

public class Record {
    private Object[] data;
    private String connectionName;
    private TableDescriptor tableDescriptor;
    private int[] keyPos;
    private boolean fromLookup;

    public Record(String connectionName, TableDescriptor tableDescriptor, Object[] data, int[] keyPos) {
	this.data = data;
	this.connectionName = connectionName;
	this.tableDescriptor = tableDescriptor;
	this.keyPos = keyPos;

    }

    public String getConnectionName() {
	return connectionName;
    }

    public Object[] getData() {
	return data;
    }

    public int[] getKeyPos() {
	return keyPos;
    }

    public TableDescriptor getTableDescriptor() {
	return tableDescriptor;
    }

    public void setConnectionName(String connectionName) {
	this.connectionName = connectionName;
    }

    public void setData(Object[] data) {
	this.data = data;
    }

    public void setKeyPos(int[] keyPos) {
	this.keyPos = keyPos;
    }

    public void setTableDescriptor(TableDescriptor tableDescriptor) {
	this.tableDescriptor = tableDescriptor;
    }

    public String toHtmlRow(String formId, String fkName, String columnName) {
	String recordId = "";
	for (int pos : keyPos) {
	    recordId = recordId + this.tableDescriptor.getColumns().get(pos).getName() + "~" + data[pos] + ";";
	}
	String html = "<tr><td>";
	if (isFromLookup()) {
	    String returnLink = "<a href='" + ServletActionContext.getRequest().getContextPath() + "/maintenanceAction!refreshFromLookup.action?connectionName=" + connectionName + "&tableName="
		    + tableDescriptor.getTableName() + "&recordId=" + recordId + "&fkName=" + fkName + "&formId=" + formId + "&columnName=" + columnName + "'>" + "Return</a>";
	    html = html + returnLink + "</td>";

	} else {
	    String editLink = "<a href='" + ServletActionContext.getRequest().getContextPath() + "/maintenanceAction!editStep2.action?connectionName=" + connectionName + "&tableName="
		    + tableDescriptor.getTableName() + "&formId=" + formId + "&recordId=" + recordId + "'>" + "Edit</a>";

	    String deleteLink = "<a href='" + ServletActionContext.getRequest().getContextPath() + "/maintenanceAction!deleteRecord.action?connectionName=" + connectionName + "&tableName="
		    + tableDescriptor.getTableName() + "&formId=" + formId + "&recordId=" + recordId + "' onclick=\"return confirmDelete('Please click OK to delete this record.')\" >" + "Delete</a>";
	    html = html + editLink + "&nbsp;" + deleteLink + "</td>";
	}
	for (Object dataE : data) {
	    html = html + "<td>" + String.valueOf(dataE) + "</td>";
	}
	html = html + "</tr>";
	return html;
    }

    public boolean isFromLookup() {
	return fromLookup;
    }

    public void setFromLookup(boolean fromLookup) {
	this.fromLookup = fromLookup;
    }

}
