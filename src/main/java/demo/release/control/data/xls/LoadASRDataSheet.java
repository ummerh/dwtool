/**
 * 
 */
package demo.release.control.data.xls;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Connections;

/**
 * 
 */
public class LoadASRDataSheet extends DataSheet {
    static Connection getDBConnection() {
	return Connections.REVMETADB.newConnection();
    }

    public static void main(String[] args) {
	run("/java/git-repos/util/java/kuali-erd-web/external/sharepoint/asr.xls");
    }

    public static void run(String location) {
	try {
	    Connection con = getDBConnection();
	    Statement stmt = null;
	    ResultSet rs = null;
	    PreparedStatement pstmt = null;
	    try {
		String sql = "select count(1) from user_tables where table_name = 'SHAREPOINT_ASR'";
		stmt = con.createStatement();
		rs = stmt.executeQuery(sql);
		if (rs != null && rs.next()) {
		    if (rs.getInt(1) == 0) {
			rs.close();
			stmt.execute("create table SHAREPOINT_ASR(Id integer, ItemID varchar2(100),ItemNumber varchar2(1000),Title varchar2(1000),GroupNm varchar2(100),asrType varchar2(100),Product varchar2(100), Assigned varchar2(100))");
		    }
		}
		pstmt = con.prepareStatement("insert into SHAREPOINT_ASR(Id,ItemID,Product,Title,asrType,Assigned,ItemNumber) values(?,?,?,?,?,?,?)");

		// get id
		int id = 0;
		sql = "select max(id) from SHAREPOINT_ASR";
		stmt = con.createStatement();
		rs = stmt.executeQuery(sql);
		if (rs != null && rs.next()) {
		    id = rs.getInt(1);
		}

		// load data
		FileInputStream dataInputStream = new FileInputStream(location);
		Workbook wb = new HSSFWorkbook(new POIFSFileSystem(dataInputStream));
		Iterator<Row> rows = wb.getSheetAt(0).iterator();
		Row headerRow = (rows != null && rows.hasNext()) ? rows.next() : null;
		Map<String, Integer> headerMap = readHeaderMap(headerRow);
		int count = 0;
		while (rows.hasNext()) {
		    List<String> dataCols = readDataCells(rows.next());
		    String asrId = readColValue("Issue ID", headerMap, dataCols);
		    sql = "select count(1) from SHAREPOINT_ASR where ItemID = '" + asrId + "'";
		    rs.close();
		    stmt.close();
		    stmt = con.createStatement();
		    rs = stmt.executeQuery(sql);
		    if (rs != null && rs.next()) {
			if (rs.getInt(1) > 0) {
			    // ignore if already loaded
			    continue;
			}
		    }
		    count++;
		    id++;
		    pstmt.setInt(1, id);
		    pstmt.setString(2, readColValue("Issue ID", headerMap, dataCols));
		    pstmt.setString(3, readColValue("Product", headerMap, dataCols));
		    pstmt.setString(4, readColValue("Title", headerMap, dataCols));
		    pstmt.setString(5, readColValue("ASR Type", headerMap, dataCols));
		    pstmt.setString(6, readColValue("Assigned To", headerMap, dataCols));
		    pstmt.setString(7, readColValue("Item Number", headerMap, dataCols));
		    pstmt.addBatch();
		    if (count % 1000 == 0) {
			pstmt.executeBatch();
			pstmt.clearBatch();
		    }
		}

		pstmt.executeBatch();
		pstmt.clearBatch();
		pstmt.close();
		System.out.println("Loaded record count - " + count);

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
