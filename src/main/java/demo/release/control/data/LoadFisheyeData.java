/**
 * 
 */
package demo.release.control.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
public class LoadFisheyeData {

	public static void main(String[] args) {
		run("C:\\java\\git-repos\\util\\java\\kuali-erd-web\\external\\fisheye\\rice.txt", "rice");
		run("C:\\java\\git-repos\\util\\java\\kuali-erd-web\\external\\fisheye\\kfs.txt", "kfs");
		run("C:\\java\\git-repos\\util\\java\\kuali-erd-web\\external\\fisheye\\kmm.txt", "kmm");
	}

	static Connection getDBConnection() {
		return Connections.REVMETADB.newConnection();
	}

	public static void run(String location, String project) {
		if (!new File(location).exists()) {
			return;
		}
		formatFile(location);
		try {
			Connection con = getDBConnection();
			Statement stmt = null;
			ResultSet rs = null;
			PreparedStatement pstmt = null;
			try {
				String sql = "select count(1) from user_tables where table_name = 'FISHEYE_REVS'";
				stmt = con.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs != null && rs.next()) {
					if (rs.getInt(1) == 0) {
						rs.close();
						stmt.execute("create table FISHEYE_REVS(Id integer, path varchar2(200),revision varchar2(100),author varchar2(100),csid varchar2(100),comments varchar2(4000), project varchar2(50))");
					}
				}
				int idval = 0;
				rs = stmt.executeQuery("select max(id) from FISHEYE_REVS");
				if (rs != null && rs.next()) {
					idval = rs.getInt(1);
				}
				// load data
				BufferedReader rdr = new BufferedReader(new FileReader(location));
				String line = null;
				pstmt = con.prepareStatement("insert into FISHEYE_REVS(Id, path,revision,author,csid,comments, project) values(?,?,?,?,?,?,?)");
				int lineCnt = 0;

				while ((line = rdr.readLine()) != null) {
					lineCnt++;
					if (lineCnt == 1) {
						// ignore the first one
						continue;
					}
					String[] tkns = StringUtil.parseQuoted(',', line);
					if (tkns.length == 5) {
						pstmt.setInt(1, ++idval);
						pstmt.setString(2, tkns[0]);
						pstmt.setString(3, tkns[1]);
						pstmt.setString(4, tkns[2]);
						pstmt.setString(5, tkns[3]);
						pstmt.setString(6, removeSpecialChars(tkns[4]));
						pstmt.setString(7, project);
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
				System.out.println("Inserted count - " + lineCnt);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DatabaseConnection.release(rs, stmt, con);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void formatFile(String location) {
		// format file
		try {
			BufferedReader rdr = new BufferedReader(new FileReader(location));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = rdr.readLine()) != null) {
				builder.append(line.trim() + " ");
			}
			rdr.close();
			new File(location).delete();
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(location)));
			out.write(builder.toString().replaceAll("\" \"", "\"\n\""));
			out.flush();
			out.close();
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	protected static String removeSpecialChars(String contnt) {
		if (contnt == null) {
			return "";
		}
		String content = contnt.trim();
		int pos = 0;
		char[] tmp = new char[content.length() * 2];
		char[] chars = content.toCharArray();
		for (char c : chars) {
			if (c == '_' || c == '.' || c == ',' || c == '/' || c == '`' || c == '@' || c == '#' || c == '$' || c == '%' || c == '=' || c == ';' || c == '<' || c == '>' || c == '+' || c == '-'
					|| c == '&' || c == '|' || c == '!' || c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '^' || c == '"' || c == '~' || c == '*' || c == '?' || c == ':'
					|| c == '\\') {
				tmp[pos++] = ' ';
				continue;
			}
			tmp[pos++] = c;
		}
		String cleanedTerm = new String(tmp, 0, pos);
		return cleanedTerm.trim();
	}
}
