package com.lndb.dwtool.erm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseConnection {

    public static final Connection newConnection(ConnectionDetail connectionDetail) throws ClassNotFoundException, SQLException {
	Connection con = null;
	Class.forName(connectionDetail.getDriver());
	con = DriverManager.getConnection(connectionDetail.getUrl(), connectionDetail.getUserId(), connectionDetail.getPassword());
	return con;
    }

    public static final void release(Connection con) throws SQLException {
	StatementPool.release();
	if (con != null) {
	    con.close();
	}
    }

    public static final void release(Statement stmt, Connection con) throws SQLException {
	if (stmt != null) {
	    stmt.close();
	}
	if (con != null) {
	    con.close();
	}
    }

    public static final void release(ResultSet rs, Statement stmt, Connection con) throws SQLException {
	if (rs != null) {
	    rs.close();
	}
	if (stmt != null) {
	    stmt.close();
	}
	if (con != null) {
	    con.close();
	}
    }

}
