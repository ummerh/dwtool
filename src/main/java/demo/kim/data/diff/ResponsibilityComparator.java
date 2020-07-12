package demo.kim.data.diff;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.ExcelConverter;

public class ResponsibilityComparator {
	public static void main(String[] args) {
		try {
			Connection fintstCon = DatabaseConnection.newConnection(fintst());
			Connection fintstbpCon = DatabaseConnection.newConnection(fintstbp());
			final HashSet<Responsibility> mismatches = new HashSet<Responsibility>();
			HashMap<String, Responsibility> tstMap = new HashMap<String, Responsibility>();
			readRecords(fintstCon, tstMap);
			HashMap<String, Responsibility> bpMap = new HashMap<String, Responsibility>();
			readRecords(fintstbpCon, bpMap);
			Collection<Responsibility> values = tstMap.values();
			for (Responsibility responsibility : values) {
				Collection<Responsibility> values2 = bpMap.values();
				boolean matchNotFound = true;
				for (Responsibility responsibility2 : values2) {
					if (responsibility.matches(responsibility2)) {
						matchNotFound = false;
						break;
					}
				}
				if (matchNotFound) {
					responsibility.setMessage("LNDB-Only");
					mismatches.add(responsibility);
				}
			}

			values = bpMap.values();
			for (Responsibility responsibility : values) {
				Collection<Responsibility> values2 = tstMap.values();
				boolean matchNotFound = true;
				for (Responsibility responsibility2 : values2) {
					if (responsibility.matches(responsibility2)) {
						matchNotFound = false;
						break;
					}
				}
				if (matchNotFound) {
					responsibility.setMessage("Foundation-Only");
					mismatches.add(responsibility);
				}
			}

			DatabaseConnection.release(fintstCon);
			DatabaseConnection.release(fintstbpCon);

			new ExcelConverter().writeOut(new FileOutputStream("/TEMP/kim-rsp-diff-report.xls"), new ExcelConverter.ExcelDataSource() {
				private ArrayList<Responsibility> records;
				int pos = 0;

				public String[] nextRow() {
					return records.get(pos++).toStrings();
				}

				public boolean hasNext() {
					return pos < records.size() - 1;
				}

				public String[] getHeaders() {
					if (records == null) {
						records = new ArrayList<Responsibility>();
						records.addAll(mismatches);
					}
					return new String[] { "NMSPC_Cd", "TMPL_ID", "ID", "NAME", "DESC", "ACTIVE", "ATTRS", "DIFF" };
				}
			});
			System.out.println("Done...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void readRecords(Connection fintstCon, HashMap<String, Responsibility> tstMap) throws SQLException {
		Statement tstStmt = fintstCon.createStatement();
		ResultSet tstRs = tstStmt
				.executeQuery("select * from krim_rsp_t a join krim_rsp_attr_data_t b on a.rsp_id = b.rsp_id order by a.rsp_id");
		while (tstRs != null && tstRs.next()) {
			Responsibility rec = tstMap.get(tstRs.getString("RSP_ID"));
			if (rec == null) {
				rec = new Responsibility();
				rec.setRspId(tstRs.getString("RSP_ID"));
				rec.setRspTmplId(tstRs.getString("RSP_TMPL_ID"));
				rec.setNmspcCd(tstRs.getString("NMSPC_CD"));
				rec.setNm(tstRs.getString("NM"));
				rec.setDescTxt(tstRs.getString("DESC_TXT"));
				rec.setActvInd(tstRs.getString("ACTV_IND"));
				tstMap.put(tstRs.getString("RSP_ID"), rec);
			}
			rec.getAttrs().put(tstRs.getString("KIM_ATTR_DEFN_ID"), tstRs.getString("ATTR_VAL"));
		}
	}

	private static ConnectionDetail fintstbp() {
		ConnectionDetail fintstbp = new ConnectionDetail();
		fintstbp.setName("fintstbp");
		// FIXME - removed configuration
		return fintstbp;
	}

	private static ConnectionDetail fintst() {
		ConnectionDetail fintst = new ConnectionDetail();
		fintst.setName("fintst");
		// FIXME - removed configuration
		return fintst;
	}
}
