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

public class RoleResponsibilityComparator {

	public static void main(String[] args) {
		try {
			Connection fintstCon = DatabaseConnection.newConnection(fintst());
			Connection fintstbpCon = DatabaseConnection.newConnection(fintstbp());
			final HashSet<RoleResponsibility> allRecs = new HashSet<RoleResponsibility>();
			HashMap<String, RoleResponsibility> tstmap = new HashMap<String, RoleResponsibility>();
			readRecords(fintstCon, tstmap);

			HashMap<String, RoleResponsibility> bpmap = new HashMap<String, RoleResponsibility>();
			readRecords(fintstbpCon, bpmap);

			Collection<RoleResponsibility> values = tstmap.values();
			for (RoleResponsibility rec : values) {
				Collection<RoleResponsibility> values2 = bpmap.values();
				boolean matchNotFound = true;
				for (RoleResponsibility role2 : values2) {
					if (rec.matches(role2)) {
						matchNotFound = false;
						break;
					}
				}
				if (matchNotFound) {
					rec.setMessage("LNDB" + rec.getMessage());
				} else {
					rec.setMessage("Matched");
				}
				allRecs.add(rec);
			}

			values = bpmap.values();
			for (RoleResponsibility rec : values) {
				Collection<RoleResponsibility> values2 = tstmap.values();
				boolean matchNotFound = true;
				for (RoleResponsibility rec2 : values2) {
					if (rec.matches(rec2)) {
						matchNotFound = false;
						break;
					}
				}
				if (matchNotFound) {
					rec.setMessage("Foundation" + rec.getMessage());
					allRecs.add(rec);
				}
			}

			DatabaseConnection.release(fintstCon);
			DatabaseConnection.release(fintstbpCon);

			new ExcelConverter().writeOut(new FileOutputStream("/TEMP/kim-rolersp-diff-report.xls"), new ExcelConverter.ExcelDataSource() {
				private ArrayList<RoleResponsibility> records;
				int pos = 0;

				public String[] nextRow() {
					return records.get(pos++).toStringArray();
				}

				public boolean hasNext() {
					return pos < records.size() - 1;
				}

				public String[] getHeaders() {
					if (records == null) {
						records = new ArrayList<RoleResponsibility>();
						records.addAll(allRecs);
					}
					return new String[] { "NMSPC_CD", "ROLE_RSP_ID", "ROLE_ID", "ROLE_NM", "RSPNMSPC", "RSPTMPLID", "RSP_ID", "RSP_ATTR",
							"ACTN_ID", "ACTN_TYP_CD", "ACTN_PLCY_CD", "FRC_ACTN", "DIFF" };
				}
			});
			System.out.println("Done...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void readRecords(Connection fintstCon, HashMap<String, RoleResponsibility> map) throws SQLException {
		Statement stmt = fintstCon.createStatement();
		ResultSet rs = stmt
				.executeQuery("select t2.role_rsp_id, t1.role_id, t1.role_nm, t1.nmspc_cd, t1.kim_typ_id roleTypId, "
						+ "t3.nmspc_cd rspnmspc, t3.rsp_tmpl_id rspTmplId, t3.rsp_id, t3.nm, "
						+ "t4.role_rsp_actn_id actn_id, t4.actn_typ_cd, t4.actn_plcy_cd, t4.frc_actn, t4.role_mbr_id, t5.kim_attr_defn_id, t5.attr_val, t5.attr_data_id attr_id "
						+ "from krim_role_t t1 join krim_role_rsp_t t2 on t1.role_id = t2.role_id join krim_rsp_t t3 on t3.rsp_id = t2.rsp_id left join krim_role_rsp_actn_t t4 on t4.role_rsp_id = t2.role_rsp_id and (t4.role_mbr_id is null or t4.role_mbr_id = '*' or t4.role_mbr_id = '') left join krim_rsp_attr_data_t t5 on t5.rsp_id = t3.rsp_id where t2.actv_ind = 'Y' order by t2.role_rsp_id, t1.role_id, t3.rsp_id");
		while (rs != null && rs.next()) {
			RoleResponsibility rec = map.get(rs.getString("ROLE_RSP_ID"));
			if (rec == null) {
				rec = new RoleResponsibility();
				rec.setId(rs.getString("ROLE_RSP_ID"));
				rec.setActionType(rs.getString("ACTN_TYP_CD"));
				rec.setActionPolicy(rs.getString("ACTN_PLCY_CD"));
				rec.setActionForced(rs.getString("FRC_ACTN"));
				rec.setActionId(rs.getString("ACTN_ID"));
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
			Responsibility rsp = rec.getResponsibility();
			if (rsp == null) {
				rsp = new Responsibility();
				rsp.setNmspcCd(rs.getString("RSPNMSPC"));
				rsp.setRspTmplId(rs.getString("RSPTMPLID"));
				rsp.setRspId(rs.getString("RSP_ID"));
				rsp.setNm("nm");
				rec.setResponsibility(rsp);
			}
			if (rs.getString("KIM_ATTR_DEFN_ID") != null && rs.getString("ATTR_VAL") != null) {
				rsp.getAttrs().put(rs.getString("KIM_ATTR_DEFN_ID"), rs.getString("ATTR_VAL") + ",attr_data_id=" + rs.getString("attr_id"));
			}
		}
	}

	private static ConnectionDetail fintstbp() {
		ConnectionDetail fintstbp = new ConnectionDetail();
		fintstbp.setName("fintstbp");
		fintstbp.setDriver("oracle.jdbc.driver.OracleDriver");
		// FIXME - removed configuration return fintstbp;
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
