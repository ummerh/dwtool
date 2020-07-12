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
public class LoadAERDataSheet extends DataSheet {
	static Connection getDBConnection() {
		return Connections.REVMETADB.newConnection();
	}

	public static void main(String[] args) {
		run("/java/git-repos/util/java/kuali-erd-web/external/sharepoint/aer.xls");
	}

	public static void run(String location) {
		try {
			Connection con = getDBConnection();
			Statement stmt = null;
			ResultSet rs = null;
			PreparedStatement pstmt = null;
			try {
				String sql = "select count(1) from user_tables where table_name = 'SHAREPOINT_AER'";
				stmt = con.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs != null && rs.next()) {
					if (rs.getInt(1) == 0) {
						rs.close();
						stmt.execute("create table SHAREPOINT_AER(Id Integer,ItemNumber varchar2(1000),Team varchar2(100),System varchar2(100),Assignment varchar2(100),Owner varchar2(100),AERID varchar2(100),Title varchar2(1000))");
					}
				}
				// get id
				int id = 0;
				sql = "select  max(id) from SHAREPOINT_AER";
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

				pstmt = con.prepareStatement("insert into SHAREPOINT_AER(Id,ItemNumber,Team,System,Assignment,Owner,AERID,Title) values(?,?,?,?,?,?,?,?)");
				int count = 0;
				while (rows.hasNext()) {
					List<String> dataCols = readDataCells(rows.next());
					String aerId = readColValue("ID", headerMap, dataCols);
					sql = "select count(1) from SHAREPOINT_AER where AERID = '" + aerId + "'";
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
					pstmt.setString(2, readColValue("Item Number", headerMap, dataCols));
					pstmt.setString(3, readColValue("Team", headerMap, dataCols));
					pstmt.setString(4, readColValue("System", headerMap, dataCols));
					pstmt.setString(5, readColValue("Tech Assignment", headerMap, dataCols));
					pstmt.setString(6, readColValue("Functional Owner", headerMap, dataCols));
					pstmt.setString(7, aerId);
					pstmt.setString(8, readColValue("Title", headerMap, dataCols));
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
