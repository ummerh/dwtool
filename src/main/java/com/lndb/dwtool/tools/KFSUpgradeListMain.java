package com.lndb.dwtool.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Connections;
import com.lndb.dwtool.erm.xldb.ExcelWorkbookDB;

public class KFSUpgradeListMain {
    public static void main(String[] args) {
	try {
	    ExcelWorkbookDB db = new ExcelWorkbookDB();
	    db.load("/java/projects/kuali-erd-web/external/sharepoint/kfs-upgrade.xls");

	    Connection memcon = Connections.MEMORYDB.newConnection();
	    Statement memstmt = memcon.createStatement();
	    Connection revdb = Connections.REVMETADB.newConnection();
	    Statement revstmt = revdb.createStatement();
	    ResultSet revrs = null;
	    Statement revUpdtstmt = revdb.createStatement();

	    ResultSet memrs = memstmt.executeQuery("select b, ae from kfs_upgrade_s1 where ae <> ''");

	    while (memrs.next()) {
		String aerId = memrs.getString("b");
		revrs = revstmt.executeQuery("select count(1) from sharepoint_aer where itemnumber= '" + aerId + "'");
		if (revrs.next() && revrs.getInt(1) == 0) {
		    System.err.println("update sharepoint_dtt set cntrb='" + memrs.getString("ae") + "' where itemid='" + aerId + "';");
		} else {
		    // revUpdtstmt.executeUpdate("update sharepoint_aer set cntrb='"
		    // + memrs.getString("ae") + "' where itemnumber= '" + aerId
		    // + "'");
		}
	    }

	    DatabaseConnection.release(memrs, memstmt, memcon);
	    revUpdtstmt.close();
	    revdb.commit();
	    DatabaseConnection.release(revrs, revstmt, revdb);

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);

    }
}
