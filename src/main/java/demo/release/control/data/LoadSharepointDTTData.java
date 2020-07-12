/**
 * 
 */
package demo.release.control.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Connections;
import com.lndb.dwtool.erm.util.StringUtil;

/**
 * 
 */
public class LoadSharepointDTTData {
    static Connection getDBConnection() {
	return Connections.REVMETADB.newConnection();
    }

    public static void main(String[] args) {
	run("/java/projects/kuali-erd-web/external/db/csv-files/v2.1/sharepoint-DTT.csv");
    }

    public static void run(String location) {
	try {
	    Connection con = getDBConnection();
	    Statement stmt = null;
	    ResultSet rs = null;
	    PreparedStatement pstmt = null;
	    try {
		// create table SHAREPOINT_DTT
		String sql = "select count(1) from user_tables where table_name = 'SHAREPOINT_DTT'";
		stmt = con.createStatement();
		rs = stmt.executeQuery(sql);
		if (rs != null && rs.next()) {
		    if (rs.getInt(1) == 0) {
			rs.close();
			stmt.execute("create table SHAREPOINT_DTT(Id integer, ItemID varchar2(100),DefectName varchar2(1000),Status varchar2(100),AERNumber varchar2(100),Team varchar2(100),Reporter varchar2(100),Assignment varchar2(1000))");
		    }
		}
		// get latest id from SHAREPOINT_DTT
		BufferedReader rdr = new BufferedReader(new FileReader(location));
		String line = null;
		pstmt = con.prepareStatement("insert into SHAREPOINT_DTT(Id, ItemID,DefectName,Status,AERNumber,Team,Reporter,Assignment) values(?,?,?,?,?,?,?,?)");
		int id = 0;
		sql = "select max(id) from SHAREPOINT_DTT";
		stmt = con.createStatement();
		rs = stmt.executeQuery(sql);
		if (rs != null && rs.next()) {
		    id = rs.getInt(1);
		}

		int count = 0;
		while ((line = rdr.readLine()) != null) {
		    id++;
		    count++;
		    if (count == 1) {
			// ignore the header
			continue;
		    }
		    String[] tkns = StringUtil.parseQuoted(',', line);
		    if (tkns.length == 7) {
			pstmt.setInt(1, id);
			pstmt.setString(2, tkns[0]);
			pstmt.setString(3, tkns[1]);
			pstmt.setString(4, tkns[2]);
			pstmt.setString(5, tkns[3]);
			pstmt.setString(6, tkns[4]);
			pstmt.setString(7, tkns[5]);
			pstmt.setString(8, tkns[6]);
			pstmt.addBatch();
		    } else {
			System.out.println("ignored " + count + ": " + line);
		    }
		    if (count % 1000 == 0) {
			pstmt.executeBatch();
			pstmt.clearBatch();
		    }
		}

		pstmt.executeBatch();
		pstmt.clearBatch();
		pstmt.close();

	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		DatabaseConnection.release(rs, stmt, con);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
