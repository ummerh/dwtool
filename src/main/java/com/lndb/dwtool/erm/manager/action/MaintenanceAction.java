package com.lndb.dwtool.erm.manager.action;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.struts.ActionSupport;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.db.data.Record;
import com.lndb.dwtool.erm.db.data.TableRow;
import com.lndb.dwtool.erm.ddl.Dialect;
import com.lndb.dwtool.erm.manager.web.Session;
import com.lndb.dwtool.erm.util.ObjectValueUtils;

public class MaintenanceAction extends ActionSupport implements Cloneable {
    private String connectionName;
    private String tableName;
    private TableDescriptor tableDescriptor;
    private HashMap<String, Object> fieldMap = new HashMap<String, Object>();
    private List<Record> records = new ArrayList<Record>();
    private String recordId;
    private String message;
    private String columnName;
    private String fkName;
    private boolean fromLookup;
    private String method;
    private String formId;

    public String addRecord() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	TableRow row = new TableRow(tableDescriptor, fieldMap);
	if (!row.validateDataTypes()) {
	    setMessage("Cannot be saved");
	    return "addStep";
	}
	String sql = TableRow.prepareInsertStatement(row.getHeaders(), tableDescriptor);
	Connection con = null;
	PreparedStatement pstmt = null;

	try {
	    ConnectionDetail detail = ConnectionDetail.configure(getConnectionName());
	    con = DatabaseConnection.newConnection(detail);
	    pstmt = con.prepareStatement(sql);
	    row.insertStatementParams(pstmt);
	    if (pstmt.executeUpdate() > 0) {
		setMessage("Saved successfully.");
	    } else {
		setMessage("Save failed.");
	    }
	} catch (Exception e) {
	    setMessage("Save failed.");
	    e.printStackTrace();
	} finally {
	    if (pstmt != null) {
		pstmt.close();
	    }
	    DatabaseConnection.release(con);
	}
	setMethod("editStep1");
	return "editStep1";
    }

    public String addStep1() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	this.fieldMap.clear();
	setMethod("addStep");
	return "addStep";
    }

    public String deleteRecord() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	String[] keyValPairs = this.recordId.split(";");
	this.fieldMap.clear();
	for (String valpair : keyValPairs) {
	    String[] splits = valpair.split("~");
	    if (StringUtils.isNotBlank(splits[0]) && StringUtils.isNotBlank(splits[0])) {
		this.fieldMap.put(splits[0], splits[1]);
	    }
	}
	findRecords();
	if (!this.records.isEmpty()) {
	    Record record = this.records.get(0);
	    Object[] data = record.getData();
	    int pos = 0;
	    for (Object object : data) {
		if (data != null) {
		    this.fieldMap.put(tableDescriptor.getColumns().get(pos).getName(), object);
		}
		pos++;
	    }
	    TableRow row = new TableRow(tableDescriptor, fieldMap);
	    String sql = TableRow.prepareDeleteStatement(tableDescriptor);
	    Connection con = null;
	    PreparedStatement pstmt = null;

	    try {
		ConnectionDetail detail = ConnectionDetail.configure(getConnectionName());
		con = DatabaseConnection.newConnection(detail);
		pstmt = con.prepareStatement(sql);
		row.deleteStatementParams(pstmt);
		if (pstmt.executeUpdate() > 0) {
		    setMessage("Deleted successfully.");
		} else {
		    setMessage("Delete failed.");
		}
	    } catch (Exception e) {
		setMessage("Delete failed.");
		e.printStackTrace();
	    } finally {
		if (pstmt != null) {
		    pstmt.close();
		}
		DatabaseConnection.release(con);
	    }
	}
	this.fieldMap.clear();
	this.records.clear();
	setMethod("editStep1");
	return "editStep1";
    }

    public String editStep1() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	setMethod("editStep1");
	return "editStep1";
    }

    public String editStep2() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	this.fieldMap.clear();
	this.fieldMap.putAll(getRecordIdMap());
	findRecords();
	if (!this.records.isEmpty()) {
	    Record record = this.records.get(0);
	    Object[] data = record.getData();
	    int pos = 0;
	    for (Object object : data) {
		if (data != null) {
		    this.fieldMap.put(tableDescriptor.getColumns().get(pos).getName(), object);
		}
		pos++;
	    }
	}
	setMethod("editStep2");
	return "editStep2";
    }

    private int[] findKeyPos(List<ColumnDescriptor> columns, List<String> primaryKeys) {
	int[] keyPose = new int[primaryKeys.size()];
	int pos = 0;
	for (int i = 0; i < columns.size(); i++) {
	    if (tableDescriptor.isMemberOfPK(columns.get(i).getName())) {
		keyPose[pos] = i;
		pos++;
	    }
	}
	return keyPose;
    }

    public String findRecords() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	TableRow row = new TableRow(tableDescriptor, fieldMap);
	String sql = row.prepareSelectSql();
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	List<ColumnDescriptor> columns = tableDescriptor.getColumns();
	List<String> primaryKeys = tableDescriptor.getPrimaryKeys();
	int[] keyPose = findKeyPos(columns, primaryKeys);

	try {
	    ConnectionDetail detail = ConnectionDetail.configure(getConnectionName());
	    Dialect dialect = detail.getDialect();
	    con = DatabaseConnection.newConnection(detail);
	    pstmt = con.prepareStatement(sql);
	    row.updateSelectParms(pstmt);
	    rs = pstmt.executeQuery();
	    while (rs != null && rs.next()) {
		Object[] data = new Object[columns.size()];
		int pos = 0;
		for (ColumnDescriptor column : columns) {
		    data[pos++] = dialect.readData(column, rs);
		}
		Record record = new Record(getConnectionName(), tableDescriptor, data, keyPose);
		record.setFromLookup(isFromLookup());
		records.add(record);
	    }

	} catch (Exception e) {
	    setMessage("Search failed.");
	    e.printStackTrace();
	} finally {
	    if (rs != null) {
		rs.close();
	    }

	    if (pstmt != null) {
		pstmt.close();
	    }
	    DatabaseConnection.release(con);
	}

	setMethod("editStep1");
	return "editStep1";
    }

    public String getColumnName() {
	return columnName;
    }

    public String getConnectionName() {
	return connectionName;
    }

    public HashMap<String, Object> getFieldMap() {
	return fieldMap;
    }

    public String getFkName() {
	return fkName;
    }

    public String getFormId() {
	return formId;
    }

    public String getMessage() {
	return message;
    }

    public String getMethod() {
	return method;
    }

    public String getRecordId() {
	return recordId;
    }

    private HashMap<String, Object> getRecordIdMap() {
	HashMap<String, Object> idMap = new HashMap<String, Object>();
	String[] keyValPairs = this.recordId.split(";");
	for (String valpair : keyValPairs) {
	    String[] splits = valpair.split("~");
	    if (StringUtils.isNotBlank(splits[0]) && StringUtils.isNotBlank(splits[0])) {
		idMap.put(splits[0], splits[1]);
	    }
	}
	return idMap;
    }

    public List<Record> getRecords() {
	return records;
    }

    public TableDescriptor getTableDescriptor() {
	return tableDescriptor;
    }

    public String getTableName() {
	return tableName;
    }

    public Object getValue(String name) {
	return this.fieldMap.get(name);
    }

    public boolean isFromLookup() {
	return fromLookup;
    }

    @SuppressWarnings("unchecked")
    public String lookupReference() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	ForeignKey foreignKey = this.tableDescriptor.getForeignKey(getFkName());

	HashMap<String, Object> newFieldMap = new HashMap<String, Object>();
	String referToTable = foreignKey.getReferToTable();
	List<String> referToCols = foreignKey.getReferToCols();
	for (String string : referToCols) {
	    // find data from current map
	    newFieldMap.put(string, fieldMap.get(foreignKey.getReferByColumn(string)));

	}
	// put current form in session
	Stack<MaintenanceAction> stack = (Stack<MaintenanceAction>) Session.get(getFormId());
	if (stack == null) {
	    stack = new Stack<MaintenanceAction>();
	}
	stack.push((MaintenanceAction) this.clone());
	Session.set(getFormId(), stack);
	// set form with new values and call edit step1
	setFromLookup(true);
	setTableName(referToTable);
	setFieldMap(newFieldMap);
	return editStep1();
    }

    @SuppressWarnings("unchecked")
    public String refreshFromLookup() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	Stack<MaintenanceAction> stack = (Stack<MaintenanceAction>) Session.get(getFormId());
	MaintenanceAction prevForm = stack.pop();
	ObjectValueUtils.copySimpleProperties(prevForm, this);
	HashMap<String, Object> recordIdMap = getRecordIdMap();
	Set<String> referToCols = recordIdMap.keySet();
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	ForeignKey foreignKey = this.tableDescriptor.getForeignKey(getFkName());

	for (String referTo : referToCols) {
	    this.fieldMap.put(foreignKey.getReferByColumn(referTo), recordIdMap.get(referTo));
	}

	return getMethod();
    }

    public String saveRecord() throws Exception {
	if (getFormId() == null) {
	    setFormId(String.valueOf(System.currentTimeMillis()));
	}
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	this.tableDescriptor = dbMap.getTableDescriptor(getTableName());
	TableRow row = new TableRow(tableDescriptor, fieldMap);
	if (!row.validateDataTypes()) {
	    setMessage("Cannot be saved");
	    setMethod("editStep2");
	    return "editStep2";
	}
	String sql = TableRow.prepareUpdateStatement(row.getHeaders(), tableDescriptor);
	Connection con = null;
	PreparedStatement pstmt = null;

	try {
	    ConnectionDetail detail = ConnectionDetail.configure(getConnectionName());
	    con = DatabaseConnection.newConnection(detail);
	    pstmt = con.prepareStatement(sql);
	    row.updateStatementParams(pstmt);
	    if (pstmt.executeUpdate() > 0) {
		setMessage("Saved successfully.");
	    } else {
		setMessage("Save failed.");
	    }
	} catch (Exception e) {
	    setMessage("Save failed.");
	    e.printStackTrace();
	} finally {
	    if (pstmt != null) {
		pstmt.close();
	    }
	    DatabaseConnection.release(con);
	}

	setMethod("editStep2");
	return "editStep2";
    }

    public void setColumnName(String columnName) {
	this.columnName = columnName;
    }

    public void setConnectionName(String connectionName) {
	this.connectionName = connectionName;
    }

    public void setFieldMap(HashMap<String, Object> fieldMap) {
	this.fieldMap = fieldMap;
    }

    public void setFkName(String fkName) {
	this.fkName = fkName;
    }

    public void setFormId(String formId) {
	this.formId = formId;
    }

    public void setFromLookup(boolean fromLookup) {
	this.fromLookup = fromLookup;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public void setMethod(String method) {
	this.method = method;
    }

    public void setRecordId(String recordId) {
	this.recordId = recordId;
    }

    public void setRecords(List<Record> records) {
	this.records = records;
    }

    public void setTableDescriptor(TableDescriptor tableDescriptor) {
	this.tableDescriptor = tableDescriptor;
    }

    public void setTableName(String tableName) {
	this.tableName = tableName;
    }
}
