package demo.kim.data.diff;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.ExcelConverter;

public class PermissionComparator {
	public static void main(String[] args) {
		try {
			Connection fintstCon = DatabaseConnection.newConnection(fintst());
			Connection fintstbpCon = DatabaseConnection.newConnection(fintstbp());
			final HashSet<Permission> mismatches = new HashSet<Permission>();
			HashMap<String, Permission> tstMap = new HashMap<String, Permission>();
			readRecords(fintstCon, tstMap);
			HashMap<String, Permission> bpMap = new HashMap<String, Permission>();
			readRecords(fintstbpCon, bpMap);

			HashSet<String> tstIds = new HashSet<String>();
			HashSet<String> bpIds = new HashSet<String>();

			HashMap<String, List<Permission>> tstEmptyAttr = new HashMap<String, List<Permission>>();
			for (Permission perm : tstMap.values()) {
				Map<String, String> attrs = perm.getAttrs();
				if (attrs.isEmpty()) {
					tstIds.add(perm.getPermId());
					List<Permission> list = tstEmptyAttr.get(perm.getNmspcCd() + "-" + perm.getPermTmplId());
					if (list == null) {
						list = new ArrayList<Permission>();
						tstEmptyAttr.put(perm.getNmspcCd() + "-" + perm.getPermTmplId(), list);
					}
					list.add(perm);
				}
			}

			HashMap<String, List<Permission>> bpEmptyAttr = new HashMap<String, List<Permission>>();
			for (Permission perm : bpMap.values()) {
				Map<String, String> attrs = perm.getAttrs();
				if (attrs.isEmpty()) {
					bpIds.add(perm.getPermId());
					List<Permission> list = bpEmptyAttr.get(perm.getNmspcCd() + "-" + perm.getPermTmplId());
					if (list == null) {
						list = new ArrayList<Permission>();
						bpEmptyAttr.put(perm.getNmspcCd() + "-" + perm.getPermTmplId(), list);
					}
					list.add(perm);
				}
			}

			for (Permission permission : tstMap.values()) {
				boolean matchNotFound = true;
				for (Permission permission2 : bpMap.values()) {
					if (permission.matches(permission2)) {
						matchNotFound = false;
						if (!permission.getNm().equals(permission2.getNm())) {
							System.out.println("update krim_perm_t set nm='" + permission2.getNm() + "' where perm_id='" + permission.getPermId() + "';");
						}
						// break;
					}
				}
				if (matchNotFound) {
					permission.setMessage("LNDB ONLY");
					mismatches.add(permission);

				}
			}

			for (String string : tstEmptyAttr.keySet()) {
				if (bpEmptyAttr.get(string) == null || tstEmptyAttr.get(string).size() != bpEmptyAttr.get(string).size()) {
					List<Permission> dt = tstEmptyAttr.get(string);
					for (Permission permission : dt) {
						if (!bpIds.contains(permission.getPermId())) {
							if (mismatches.add(permission)) {
								permission.setMessage("LNDB Added");
							}
						}
					}
				}
			}

			for (Permission perm : bpMap.values()) {
				boolean matchNotFound = true;
				for (Permission responsibility2 : tstMap.values()) {
					if (perm.matches(responsibility2)) {
						matchNotFound = false;
						break;
					}
				}
				if (matchNotFound) {
					perm.setMessage("Foundation ONLY");
					mismatches.add(perm);
				}
			}

			for (String string : bpEmptyAttr.keySet()) {
				if (tstEmptyAttr.get(string) == null || tstEmptyAttr.get(string).size() != bpEmptyAttr.get(string).size()) {
					List<Permission> dt = bpEmptyAttr.get(string);
					for (Permission permission : dt) {
						if (!tstIds.contains(permission.getPermId())) {
							if (mismatches.add(permission)) {
								permission.setMessage("Foundation Added");
							}
						}
					}
				}
			}

			new ExcelConverter().writeOut(new FileOutputStream("/TEMP/kim-perm-diff-report.xls"), new ExcelConverter.ExcelDataSource() {

				private ArrayList<Permission> records;
				int pos = 0;

				public String[] nextRow() {
					return records.get(pos++).toStrings();

				}

				public boolean hasNext() {
					return pos < records.size() - 1;
				}

				public String[] getHeaders() {
					if (records == null) {
						records = new ArrayList<Permission>();
						records.addAll(mismatches);

					}
					return new String[] { "NMSPC_CD", "PERM_TMPL_ID", "PERM_ID", "PERM_NM", "DESC", "ACT_IND", "PERM_ATTR", "DIFF" };
				}
			});

			DatabaseConnection.release(fintstCon);
			DatabaseConnection.release(fintstbpCon);
			System.out.println("Done....");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void readRecords(Connection fintstCon, HashMap<String, Permission> tstMap) throws SQLException {
		Statement tstStmt = fintstCon.createStatement();
		ResultSet tstRs = tstStmt.executeQuery("select * from krim_perm_t a left join krim_perm_attr_data_t b on a.perm_id = b.perm_id order by a.perm_id");
		while (tstRs != null && tstRs.next()) {
			Permission rec = tstMap.get(tstRs.getString("PERM_ID"));
			if (rec == null) {
				rec = new Permission();
				rec.setPermId(tstRs.getString("PERM_ID"));
				rec.setPermTmplId(tstRs.getString("PERM_TMPL_ID"));
				rec.setNmspcCd(tstRs.getString("NMSPC_CD"));
				rec.setNm(tstRs.getString("NM"));
				rec.setDescTxt(tstRs.getString("DESC_TXT"));
				rec.setActvInd(tstRs.getString("ACTV_IND"));
				tstMap.put(tstRs.getString("PERM_ID"), rec);
			}
			if (tstRs.getString("KIM_ATTR_DEFN_ID") != null && tstRs.getString("ATTR_VAL") != null) {
				rec.getAttrs().put(tstRs.getString("KIM_ATTR_DEFN_ID"), tstRs.getString("ATTR_VAL"));
			}
		}
	}

	private static ConnectionDetail fintstbp() {
		ConnectionDetail fintstbp = new ConnectionDetail();
		fintstbp.setName("fintstbp");
		return fintstbp;
	}

	private static ConnectionDetail fintst() {
		ConnectionDetail fintst = new ConnectionDetail();
		fintst.setName("fintst");
		return fintst;
	}
}
