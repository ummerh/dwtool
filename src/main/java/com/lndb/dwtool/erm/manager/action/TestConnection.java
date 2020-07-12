package com.lndb.dwtool.erm.manager.action;

import java.sql.Connection;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.util.ConnectionRepository;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class that supports testing a database connection definition
 * 
 * @author harsha07
 * 
 */
public class TestConnection extends ActionSupport {
    /**
     * 
     */
    private static final long serialVersionUID = 1487870499670285013L;
    private String connectionName;
    private String statusMessage;
    private boolean status;

    /**
     * Action method tests a connection and displays the message
     */
    @Override
    public String execute() throws Exception {

	ConnectionDetail conDetail = ConnectionDetail.configure(connectionName);
	try {
	    Connection newConnection = ConnectionRepository.validateConnection(conDetail);
	    if (newConnection == null) {
		this.statusMessage = "Connection could not be obtained";
	    } else {
		this.statusMessage = "Connected successfully";
		this.status = true;
	    }
	} catch (Exception e) {
	    this.statusMessage = "Error occured while attempting connection " + e.toString();
	}
	return SUCCESS;
    }

    public String getConnectionName() {
	return connectionName;
    }

    public void setConnectionName(String connectionName) {
	this.connectionName = connectionName;
    }

    public String getStatusMessage() {
	return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
	this.statusMessage = statusMessage;
    }

    public boolean isStatus() {
	return status;
    }

    public void setStatus(boolean status) {
	this.status = status;
    }

}
