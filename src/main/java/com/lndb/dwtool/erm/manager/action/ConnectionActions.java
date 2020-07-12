package com.lndb.dwtool.erm.manager.action;

import java.util.ArrayList;
import java.util.List;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.util.ConnectionRepository;
import com.lndb.dwtool.erm.util.StatusMonitor;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class supports view for existing and new connections
 * 
 * @author harsha07
 * 
 */
public class ConnectionActions extends ActionSupport {
    private String connectionName;
    private List<ConnectionDetail> connections = new ArrayList<ConnectionDetail>();

    /**
     * Serial Id
     */
    private static final long serialVersionUID = -1462910413250248660L;

    /**
     * Retrieves all the connection settings and validates the connection before
     * displaying them
     * 
     * @return dispatch
     * @throws Exception
     */
    public String displayConnections() throws Exception {
	this.connections = ConnectionRepository.getAllConnections();
	ConnectionRepository.validateConnections(this.connections);
	return SUCCESS;
    }

    /**
     * Displays new connection configuration input screen
     * 
     * @return
     * @throws Exception
     */
    public String newConnectionInput() throws Exception {
	return "input";
    }

    public List<ConnectionDetail> getConnections() {
	return connections;
    }

    public void setConnections(List<ConnectionDetail> connections) {
	this.connections = connections;
    }

    /**
     * Deletes an existing connection from the repository file
     * 
     * @return
     * @throws Exception
     */
    public String deleteConnection() throws Exception {
	ConnectionRepository.deleteConnection(getConnectionName());
	return displayConnections();
    }

    public String getConnectionName() {
	return connectionName;
    }

    public void setConnectionName(String connectionName) {
	this.connectionName = connectionName;
    }

    public String refreshConnection() throws Exception {
	DBMapCache.removeDBMap(getConnectionName());
	StatusMonitor.removeStatus(getConnectionName());
	StatusMonitor.removeProgressInd(getConnectionName());
	return displayConnections();
    }
}
