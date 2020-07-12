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
public class LoadSharepointPreAERData {
    static Connection getDBConnection() {
	return Connections.REVMETADB.newConnection();
    }

    public static void main(String[] args) {
	run("/java/projects/kuali-erd-web/external/db/csv-files/v1.0/sharepoint-pre-AER.csv");
    }

    public static void run(String location) {
	try {
	    Connection con = getDBConnection();
	    Statement stmt = null;
	    ResultSet rs = null;
	    PreparedStatement pstmt = null;
	    try {
		String sql = "select count(1) from user_tables where table_name = 'SHAREPOINT_PRE_AER'";
		stmt = con.createStatement();
		rs = stmt.executeQuery(sql);
		if (rs != null && rs.next()) {
		    if (rs.getInt(1) == 0) {
			rs.close();
			stmt.execute("create table SHAREPOINT_PRE_AER(Id Integer,IssueID varchar2(100),Title varchar2(1000),Status varchar2(100),Team varchar2(100),Comments varchar2(1000),Assigned varchar2(100))");
		    }
		}
		// load data
		BufferedReader rdr = new BufferedReader(new FileReader(location));
		String line = null;
		pstmt = con.prepareStatement("insert into SHAREPOINT_PRE_AER(Id,IssueID,Title,Status,Team,Comments,Assigned) values(?,?,?,?,?,?,?)");
		int lineCnt = 0;
		while ((line = rdr.readLine()) != null) {
		    lineCnt++;
		    if (lineCnt == 1) {
			// ignore the first one
			continue;
		    }
		    String[] tkns = StringUtil.parseQuoted(',', line);
		    if (tkns.length == 6) {
			pstmt.setInt(1, lineCnt);
			pstmt.setString(2, tkns[0]);
			pstmt.setString(3, tkns[1]);
			pstmt.setString(4, tkns[2]);
			pstmt.setString(5, tkns[3]);
			pstmt.setString(6, tkns[4]);
			pstmt.setString(7, tkns[5]);
			pstmt.addBatch();
		    } else {
			System.out.println("ignored " + lineCnt + ": " + line);
		    }
		    if (lineCnt % 1000 == 0) {
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
