package com.lndb.dwtool.erm.manager.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.validation.SkipValidation;

import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.util.ConnectionRepository;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class supports view for existing and new connections
 * 
 * @author harsha07
 * 
 */
public class DataValidatorAction extends ActionSupport {
    private String targetDb;
    private Map<String, String> dataStatus;
    private String connectionName;
    /**
     * 
     */
    private static final long serialVersionUID = -4025402883486041044L;
    private List<ConnectionDetail> connections = new ArrayList<ConnectionDetail>();

    @SkipValidation
    public String displayInput() throws Exception {
	return INPUT;
    }

    private void prepareConnectionList() {
	this.connections.clear();
	List<ConnectionDetail> allConnections = ConnectionRepository.getAllConnections();
	ConnectionRepository.validateConnections(allConnections);
	for (ConnectionDetail connectionDetail : allConnections) {
	    if (connectionDetail.isValid()) {
		this.connections.add(connectionDetail);
	    }
	}
    }

    public String validateDb() throws Exception {
	OJBMap ojbMap = OJBMapCache.getOJBMap();
	DBMap dbMap = DBMapCache.getDBMap(getTargetDb());
	SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
	Connection newConnection = DatabaseConnection.newConnection(ConnectionDetail.configure(getConnectionName()));
	setDataStatus(joinMetaData.checkDataIntegrity(newConnection));
	DatabaseConnection.release(newConnection);
	return "dataStatus";
    }

    public List<ConnectionDetail> getConnections() {
	prepareConnectionList();
	return connections;
    }

    public void setConnections(List<ConnectionDetail> connections) {
	this.connections = connections;
    }

    public String getTargetDb() {
	return targetDb;
    }

    public void setTargetDb(String stagingDb) {
	this.targetDb = stagingDb;
    }

    public String getConnectionName() {
	return connectionName;
    }

    public void setConnectionName(String connectionName) {
	this.connectionName = connectionName;
    }

    public Map<String, String> getDataStatus() {
	return dataStatus;
    }

    public void setDataStatus(Map<String, String> dataStatus) {
	this.dataStatus = dataStatus;
    }

}
