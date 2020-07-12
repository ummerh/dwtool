/**
 * 
 */
package com.lndb.dwtool.erm.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;

/**
 * 
 */
public enum Connections {
	REVMETADB {
		public Connection newConnection() {
			Connection con = null;
			try {
				con = DatabaseConnection.newConnection(ConnectionDetail.configure(Configuration.getProperty("revision.db")));
			} catch (Exception e) {
				throw new RuntimeException("DB connection error!", e);
			}
			return con;
		}
	},
	KMMDEMO {
		public Connection newConnection() {
			Connection con = null;
			try {
				con = DatabaseConnection.newConnection(ConnectionDetail.configure(Configuration.getProperty("kmmdemo.db")));
			} catch (Exception e) {
				throw new RuntimeException("DB connection error!", e);
			}
			return con;
		}
	},
	MEMORYDB {
		public Connection newConnection() {
			Connection con = null;
			try {
				String excelDir = Configuration.getProperty("excel.repository");
				if (!new File(excelDir).exists()) {
					new File(excelDir).mkdirs();
					new File(excelDir, "hsqldb").mkdirs();
				}
				System.setProperty("hsqldb.reconfig_logging", "false");
				Class.forName("org.hsqldb.jdbc.JDBCDriver");
				String url = "jdbc:hsqldb:file:" + new File(excelDir, "hsqldb").getPath() + File.separator + "memorydb";
				System.out.println("HSQLDB URL used is " + url);
				con = DriverManager.getConnection(url, "dwtool", "dwtool");
			} catch (Exception e) {
				throw new RuntimeException("DB connection error!", e);
			}
			return con;
		}
	};
	public abstract Connection newConnection();
}
