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
import java.util.List;
import java.util.Map;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.ExcelConverter;

public class RolePermissionComparator {

	static String qry = "select t2.role_perm_id, t1.role_id,t1.role_nm, t1.nmspc_cd,t1.kim_typ_id  roleTypId,t3.nmspc_cd   permnmspc,  t3.perm_tmpl_id perTmplId ,t3.perm_id, t3.nm, t4.attr_data_id attr_id, t4.kim_attr_defn_id, t4.attr_val"
			+ "  from krim_role_t t1  join krim_role_perm_t t2   on t1.role_id = t2.role_id  join krim_perm_t t3    on t3.perm_id = t2.perm_id  left join krim_perm_attr_data_t t4 "
			+ " on t4.perm_id = t3.perm_id where t2.actv_ind ='Y' order by t2.role_perm_id, t1.role_id, t3.perm_id ";

	public static void main(String[] args) {
		try {
			Connection fintstCon = DatabaseConnection.newConnection(fintst());
			Connection fintstbpCon = DatabaseConnection.newConnection(fintstbp());

			final HashSet<RolePermission> mismatches = new HashSet<RolePermission>();
			final Map<String, List<RolePermission>> tstEmptyAttrs = new HashMap<String, List<RolePermission>>();
			final Map<String, List<RolePermission>> bpEmptyAttrs = new HashMap<String, List<RolePermission>>();

			HashMap<String, RolePermission> tstmap = new HashMap<String, RolePermission>();
			readRecords(fintstCon, tstmap);

			HashMap<String, RolePermission> bpmap = new HashMap<String, RolePermission>();
			readRecords(fintstbpCon, bpmap);

			HashSet<String> tstIdMap = new HashSet<String>();
			HashSet<String> bpIdMap = new HashSet<String>();

			// populate tstemptyAttrs
			for (RolePermission rec : tstmap.values()) {
				if (rec.getPermission().getAttrs().isEmpty()) {
					tstIdMap.add(rec.getRole().getRoleId() + "-" + rec.getPermission().getPermId());
					List<RolePermission> list = tstEmptyAttrs.get(rec.getPermission().getNmspcCd() + "-"
							+ rec.getPermission().getPermTmplId());
					if (list == null) {
						list = new ArrayList<RolePermission>();
						tstEmptyAttrs.put(rec.getPermission().getNmspcCd() + "-" + rec.getPermission().getPermTmplId(), list);
					}
					list.add(rec);
				}
			}

			// populate bpemptyAttrs
			for (RolePermission rec : bpmap.values()) {
				if (rec.getPermission().getAttrs().isEmpty()) {
					bpIdMap.add(rec.getRole().getRoleId() + "-" + rec.getPermission().getPermId());
					List<RolePermission> list = bpEmptyAttrs.get(rec.getPermission().getNmspcCd() + "-"
							+ rec.getPermission().getPermTmplId());
					if (list == null) {
						list = new ArrayList<RolePermission>();
						bpEmptyAttrs.put(rec.getPermission().getNmspcCd() + "-" + rec.getPermission().getPermTmplId(), list);
					}
					list.add(rec);
				}
			}

			for (RolePermission rec : tstmap.values()) {
				boolean matchNotFound = true;
				Collection<RolePermission> values2 = bpmap.values();
				for (RolePermission rec2 : values2) {
					if (rec.matches(rec2)) {
						mismatches.add(rec);
						rec.setMessage("Matched");
						matchNotFound = false;
						break;
					}

				}
				if (matchNotFound) {
					rec.setMessage("LNDB" + rec.getMessage());
					mismatches.add(rec);
				}
			}

			// lndb added
			for (String string : tstEmptyAttrs.keySet()) {
				if (bpEmptyAttrs.get(string) == null || tstEmptyAttrs.get(string).size() != bpEmptyAttrs.get(string).size()) {
					List<RolePermission> dt = tstEmptyAttrs.get(string);
					for (RolePermission rec : dt) {
						if (!bpIdMap.contains(rec.getRole().getRoleId() + "-" + rec.getPermission().getPermId())) {
							rec.setMessage("LNDB Added");
							mismatches.add(rec);
						}
					}
				}
			}

			for (RolePermission rec : bpmap.values()) {
				Collection<RolePermission> values2 = tstmap.values();
				boolean matchNotFound = true;
				for (RolePermission rec2 : values2) {
					if (rec.matches(rec2)) {
						matchNotFound = false;
						break;
					}
				}
				if (matchNotFound) {
					rec.setMessage("Foundation" + rec.getMessage());
					mismatches.add(rec);
				}
			}

			// foundation added
			for (String string : bpEmptyAttrs.keySet()) {
				if (tstEmptyAttrs.get(string) == null || bpEmptyAttrs.get(string).size() != tstEmptyAttrs.get(string).size()) {
					List<RolePermission> dt = bpEmptyAttrs.get(string);
					for (RolePermission rec : dt) {
						if (!tstIdMap.contains(rec.getRole().getRoleId() + "-" + rec.getPermission().getPermId())) {
							rec.setMessage("Foundation Added");
							mismatches.add(rec);
						}
					}
				}
			}

			DatabaseConnection.release(fintstCon);
			DatabaseConnection.release(fintstbpCon);

			new ExcelConverter().writeOut(new FileOutputStream("/TEMP/kim-roleperm-diff-report.xls"), new ExcelConverter.ExcelDataSource() {

				private ArrayList<RolePermission> records;
				int pos = 0;

				public String[] nextRow() {
					return records.get(pos++).toStringArray();

				}

				public boolean hasNext() {
					return pos < records.size() - 1;
				}

				public String[] getHeaders() {
					if (records == null) {
						records = new ArrayList<RolePermission>();
						records.addAll(mismatches);

					}
					return new String[] { "NMSPC_CD", "ROLE_PERM_ID", "ROLE_ID", "ROLE_NM", "PERMNMSPC", "PERMTMPLID", "PERM_ID",
							"PERM_NM", "PERM_ATTR", "DIFF" };
				}
			});

			System.out.println("Done...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void readRecords(Connection fintstCon, HashMap<String, RolePermission> map) throws SQLException {
		Statement stmt = fintstCon.createStatement();
		ResultSet rs = stmt.executeQuery(qry);
		while (rs != null && rs.next()) {
			RolePermission rec = map.get(rs.getString("ROLE_PERM_ID"));
			if (rec == null) {
				rec = new RolePermission();
				rec.setId(rs.getString("ROLE_PERM_ID"));

				map.put(rec.getId(), rec);
			}
			Role role = rec.getRole();
			if (role == null) {
				role = new Role();
				rec.setRole(role);
				role.setRoleNm(rs.getString("ROLE_NM"));
				role.setNmspcCd(rs.getString("NMSPC_CD"));
				role.setTypeId(rs.getString("ROLETYPID"));
				role.setRoleId(rs.getString("ROLE_ID"));
			}
			Permission perm = rec.getPermission();
			if (perm == null) {
				perm = new Permission();
				perm.setNmspcCd(rs.getString("permnmspc"));
				perm.setPermTmplId(rs.getString("perTmplId"));
				perm.setPermId(rs.getString("PERM_ID"));
				perm.setNm(rs.getString("NM"));
				rec.setPermission(perm);
			}
			if (rs.getString("KIM_ATTR_DEFN_ID") != null && rs.getString("ATTR_VAL") != null) {
				perm.getAttrs().put(rs.getString("KIM_ATTR_DEFN_ID"),
						rs.getString("ATTR_VAL") + ", attr_data_id=" + rs.getString("ATTR_ID"));
			}
		}
	}

	private static ConnectionDetail fintstbp() {
		ConnectionDetail fintstbp = new ConnectionDetail();
		fintstbp.setName("fintstbp");
		fintstbp.setDriver("oracle.jdbc.driver.OracleDriver");
		// FIXME - removed configuration
		return fintstbp;
	}

	private static ConnectionDetail fintst() {
		ConnectionDetail fintst = new ConnectionDetail();
		fintst.setName("fintst");
		fintst.setDriver("oracle.jdbc.driver.OracleDriver");
		// FIXME - removed configuration
		return fintst;
	}

}
