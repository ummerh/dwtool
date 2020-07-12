package com.lndb.dwtool.erm.manager.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.interceptor.validation.SkipValidation;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.util.ConnectionRepository;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class that supports Connection Create/Edit/Update
 * 
 * @author harsha07
 * 
 */
public class SaveConnection extends ActionSupport {
    private String url;
    private String user;
    private String password;
    private String driverClass;
    private String connectionName;
    private String schema;
    private boolean edit;
    private List<ConnectionDetail> connections = new ArrayList<ConnectionDetail>();

    /**
     * Serial id
     */
    private static final long serialVersionUID = -1462910413250248660L;

    /**
     * Action method displays all connections after validate
     * 
     * @return
     * @throws Exception
     */
    public String displayConnections() throws Exception {
	this.connections = ConnectionRepository.getAllConnections();
	ConnectionRepository.validateConnections(this.connections);
	return SUCCESS;
    }

    /**
     * Displays new connection input screen
     * 
     * @return
     * @throws Exception
     */
    public String newConnectionInput() throws Exception {
	return "input";
    }

    /**
     * Action method that displays existing connection for edit
     * 
     * @return
     * @throws Exception
     */
    @SkipValidation
    public String editConnectionInput() throws Exception {
	ConnectionDetail conDetail = ConnectionDetail.configure(this.connectionName);
	setDriverClass(conDetail.getDriver());
	setUrl(conDetail.getUrl());
	setUser(conDetail.getUserId());
	setSchema(conDetail.getSchema());
	setPassword(conDetail.getPassword());
	setEdit(true);
	return "input";
    }

    /**
     * Action method that saves new connection definition
     * 
     * @return
     * @throws Exception
     */
    public String configureConnection() throws Exception {
	if (!getFieldErrors().isEmpty()) {
	    return "input";
	}
	String[] entries = new String[5];
	entries[0] = driverClass;
	entries[1] = url;
	entries[2] = schema;
	entries[3] = user;
	entries[4] = password;
	ConnectionRepository.saveConnection(getConnectionName(), entries);
	return displayConnections();
    }

    public List<ConnectionDetail> getConnections() {
	return connections;
    }

    public void setConnections(List<ConnectionDetail> connections) {
	this.connections = connections;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getUser() {
	return user;
    }

    public void setUser(String user) {
	this.user = user;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getDriverClass() {
	return driverClass;
    }

    public void setDriverClass(String driverClass) {
	this.driverClass = driverClass;
    }

    public String getConnectionName() {
	return connectionName;
    }

    public void setConnectionName(String connectionName) {
	this.connectionName = connectionName;
    }

    public boolean isEdit() {
	return edit;
    }

    public void setEdit(boolean edit) {
	this.edit = edit;
    }

    public String getSchema() {
	return schema;
    }

    public void setSchema(String schema) {
	this.schema = schema;
    }
}
