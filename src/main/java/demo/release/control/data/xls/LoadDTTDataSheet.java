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

public class LoadDTTDataSheet extends DataSheet {

	public static void main(String[] args) {
		try {
			run("/java/git-repos/util/java/kuali-erd-web/external/sharepoint/dtt.xls");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void run(String location) {
		try {
			Connection con = Connections.REVMETADB.newConnection();
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
						stmt.execute("create table sharepoint_dtt(id integer, itemid varchar2(100),defectname varchar2(1000),status varchar2(100),aernumber varchar2(100),team varchar2(100),reporter varchar2(100),assignment varchar2(1000),cntrb varchar2(1000), cntrbstatus varchar2(100), test50 varchar2(10), cntrb50 varchar2(10), cntrb50jira varchar2(100))");
					}
				}

				PreparedStatement updt = con
						.prepareStatement("update sharepoint_dtt set itemid = ?, defectname = ?, status = ?, aernumber = ?, team = ?, reporter = ?, assignment = ?, cntrb = ?, cntrbstatus = ?, test50 =?, cntrb50=? , cntrb50jira=? where itemid = ?");
				PreparedStatement insert = con
						.prepareStatement("insert into sharepoint_dtt(id, itemid,defectname,status,aernumber,team,reporter,assignment, cntrb, cntrbstatus,test50, cntrb50, cntrb50jira) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");

				int id = 0;
				sql = "select max(id) from SHAREPOINT_DTT";
				stmt = con.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs != null && rs.next()) {
					id = rs.getInt(1);
				}

				FileInputStream dataInputStream = new FileInputStream(location);
				Workbook wb = new HSSFWorkbook(new POIFSFileSystem(dataInputStream));
				Iterator<Row> rows = wb.getSheetAt(0).iterator();
				Row headerRow = (rows != null && rows.hasNext()) ? rows.next() : null;
				Map<String, Integer> headerMap = readHeaderMap(headerRow);
				int count = 0;
				int updateCount = 0;
				int insertCount = 0;
				while (rows.hasNext()) {
					List<String> dataCols = readDataCells(rows.next());
					String dttItemid = readColValue("ID", headerMap, dataCols);
					sql = "select count(1) from SHAREPOINT_DTT where ItemID = '" + dttItemid + "'";
					rs.close();
					stmt.close();
					stmt = con.createStatement();
					rs = stmt.executeQuery(sql);
					if (rs != null && rs.next()) {
						if (rs.getInt(1) > 0) {
							updateCount++;
							pstmt = updt;
						} else {
							System.out.println(dttItemid);
							insertCount++;
							pstmt = insert;
						}
					}
					count++;
					id++;
					int pstpos = 1;
					if (pstmt == insert) {
						pstmt.setInt(pstpos++, id);
					}
					pstmt.setString(pstpos++, dttItemid);
					String cntrb = readColValue("5.0 Contrib #", headerMap, dataCols);
					if (cntrb == null) {
						cntrb = "";
					} else {
						cntrb = cntrb.replace('/', ' ');
					}
					pstmt.setString(pstpos++, readColValue("Defect Name", headerMap, dataCols));
					pstmt.setString(pstpos++, readColValue("Defect Status", headerMap, dataCols));
					pstmt.setString(pstpos++, readColValue("Development AER Number", headerMap, dataCols));
					pstmt.setString(pstpos++, readColValue("Team", headerMap, dataCols));
					pstmt.setString(pstpos++, readColValue("Person Reporting the Defect", headerMap, dataCols));
					pstmt.setString(pstpos++, readColValue("Assigned To", headerMap, dataCols));
					pstmt.setString(pstpos++, cntrb);
					pstmt.setString(pstpos++, readColValue("5.0 LNDB Stage Status:", headerMap, dataCols));
					pstmt.setString(pstpos++, readColValue("5.0 Needs Testing", headerMap, dataCols));
					pstmt.setString(pstpos++, readColValue("5.0 Contribution", headerMap, dataCols));
					pstmt.setString(pstpos++, readColValue("5.0 Jira #", headerMap, dataCols).replace('/', ' '));

					if (pstmt == updt) {
						pstmt.setString(pstpos++, dttItemid);
					}
					pstmt.addBatch();
					if (count % 1000 == 0) {
						pstmt.executeBatch();
						pstmt.clearBatch();
					}
				}
				insert.executeBatch();
				updt.executeBatch();
				pstmt.clearBatch();
				pstmt.close();
				System.out.println("Total record count - " + count + "; updates - " + updateCount + "; inserts-" + insertCount);

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
