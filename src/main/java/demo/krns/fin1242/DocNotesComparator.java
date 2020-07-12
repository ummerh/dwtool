package demo.krns.fin1242;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;

public class DocNotesComparator {
	public static void main(String[] args) {
		try {
			Connection finprd = finstress();
			List<String> docIds = FileUtils.readLines(new File("/TEMP/notes-lost.txt"));
			System.out.println("Begin...");
			for (String docId : docIds) {
				readRecords22(finprd, docId);
			}
			System.out.println("End...");
			DatabaseConnection.release(finprd);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static Connection finstress() throws Exception {
		ConnectionDetail fintstbp = new ConnectionDetail();
		fintstbp.setName("finstress");
		fintstbp.setDriver("oracle.jdbc.driver.OracleDriver");
		// FIXME - removed configuration
		return DatabaseConnection.newConnection(fintstbp);
	}

	private static void readRecords(Connection con, String docId) throws Exception {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select t.doc_cntnt from krns_maint_doc_t t where t.doc_hdr_id ='" + docId + "'");
		while (rs != null && rs.next()) {
			String xml = rs.getString("doc_cntnt");
			String flattendedXml = xml.replace("\r", "").replace("\n", "");
			Matcher boNotesExtractor = Pattern.compile("(.*<org.kuali.rice.kns.bo.Note\\>(.*)</org.kuali.rice.kns.bo.Note\\>.*)").matcher(
					flattendedXml);
			if (boNotesExtractor.matches()) {
				int grpCount = boNotesExtractor.groupCount();
				for (int i = 2; i <= grpCount; i++) {
					String notestxt = boNotesExtractor.group(2);
					if (!notestxt.contains("noteIdentifier")) {
						FileUtils.write(new File("/TEMP/FIN-1242", docId + ".xml"), xml);
					}
				}
			} else {
			}
		}
		rs.close();
		stmt.close();
	}

	private static void readRecords22(Connection con, String docId) throws Exception {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select t.doc_cntnt from krns_maint_doc_t t where t.doc_hdr_id ='" + docId + "'");
		while (rs != null && rs.next()) {
			String xml = rs.getString("doc_cntnt");
			String flattendedXml = xml.replace("\r", "").replace("\n", "");
			Matcher boNotesExtractor = Pattern.compile("(.*<org.kuali.rice.kns.bo.Note\\>(.*)</org.kuali.rice.kns.bo.Note\\>.*)").matcher(
					flattendedXml);
			if (boNotesExtractor.matches()) {
				int grpCount = boNotesExtractor.groupCount();
				for (int i = 2; i <= grpCount; i++) {
					String notestxt = boNotesExtractor.group(2);
					Matcher authorExtractor = Pattern.compile("(.*<authorUniversalIdentifier\\>(.*)</authorUniversalIdentifier\\>.*)")
							.matcher(notestxt);
					if (!notestxt.contains("noteIdentifier")) {
						if (authorExtractor.matches() && !authorExtractor.group(2).equals("2")) {
							Connection finprd = finstress();
							Statement detStmt = finprd.createStatement();
							String sql = "select hdr.doc_hdr_id, hdr.doc_hdr_stat_cd, dt.doc_typ_nm, auth.prncpl_nm author, email.email_addr from rice.krew_doc_hdr_t hdr join rice.krim_prncpl_t intr on intr.prncpl_id = hdr.initr_prncpl_id join rice.krew_doc_typ_t dt on dt.doc_typ_id = hdr.doc_typ_id, rice.krim_prncpl_t auth, rice.krim_entity_email_t email where hdr.doc_hdr_id = '"
									+ docId
									+ "' and email.entity_id = auth.entity_id and auth.prncpl_id = '"
									+ authorExtractor.group(2)
									+ "'";
							ResultSet detRs = detStmt.executeQuery(sql);
							if (detRs.next()) {
								System.out.println(detRs.getString(1) + ", " + detRs.getString(2) + ", " + detRs.getString(3) + ", "
										+ detRs.getString(4) + ", " + detRs.getString(5));
							}
							detRs.close();
							detStmt.close();
							DatabaseConnection.release(finprd);
						}
						if (!authorExtractor.matches()) {
							System.err.println(docId + ",");
						}
					}
				}
			} else {
			}
		}
		rs.close();
		stmt.close();
	}
}
