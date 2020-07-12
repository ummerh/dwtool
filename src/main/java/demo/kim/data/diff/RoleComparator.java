package demo.kim.data.diff;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.ExcelConverter;

public class RoleComparator {
	public static void main(String[] args) {
		try {
			Connection fintstCon = DatabaseConnection.newConnection(fintst());
			Connection fintstbpCon = DatabaseConnection.newConnection(fintstbp());
			final ArrayList<Role> mismatches = new ArrayList<Role>();
			HashMap<String, Role> tstMap = new HashMap<String, Role>();
			readRecords(fintstCon, tstMap);
			HashMap<String, Role> bpMap = new HashMap<String, Role>();
			readRecords(fintstbpCon, bpMap);
			Collection<Role> values = tstMap.values();
			for (Role role : values) {
				Collection<Role> values2 = bpMap.values();
				boolean matchNotFound = true;
				for (Role role2 : values2) {
					if (role.matches(role2)) {
						matchNotFound = false;
						break;
					}
				}
				if (matchNotFound) {
					role.setMessage("LNDB" + role.getMessage());
					mismatches.add(role);
				}
			}

			values = bpMap.values();
			for (Role role : values) {
				Collection<Role> values2 = tstMap.values();
				boolean matchNotFound = true;
				for (Role role2 : values2) {
					if (role.matches(role2)) {
						matchNotFound = false;
						break;
					}
				}
				if (matchNotFound) {
					role.setMessage("Foundation" + role.getMessage());
					mismatches.add(role);
				}
			}

			DatabaseConnection.release(fintstCon);
			DatabaseConnection.release(fintstbpCon);

			new ExcelConverter().writeOut(new FileOutputStream("/TEMP/kim-role-diff-report.xls"), new ExcelConverter.ExcelDataSource() {
				private ArrayList<Role> records;
				int pos = 0;

				public String[] nextRow() {
					return records.get(pos++).toStrings();
				}

				public boolean hasNext() {
					return pos < records.size() - 1;
				}

				public String[] getHeaders() {
					if (records == null) {
						records = mismatches;
					}
					return new String[] { "NMSPC_CD", "TYP_NMSPC_CD", "TYP_NM", "ROLEID", "NAME", "DESC", "ACTIVE", "MEMBERS", "DIFF" };
				}
			});
			System.out.println("Done...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void readRecords(Connection fintstCon, HashMap<String, Role> tstMap) throws SQLException {
		Statement stmt = fintstCon.createStatement();
		ResultSet rs = stmt
				.executeQuery("select t1.role_id, t1.role_nm, t1.nmspc_cd, t1.desc_txt, t4.nmspc_cd KIM_TYP_NMSPC, t4.nm KIM_TYP_NM, t1.actv_ind, "
						+ "t3.role_id mbrId,t3.kim_typ_id mbrTypId, t3.role_nm mbrNm, t3.nmspc_cd mbrNmspc, t3.desc_txt mbrDesc, t3.actv_ind mbrActv, t2.role_mbr_id "
						+ "from krim_role_t t1 left join krim_role_mbr_t t2 on t2.role_id = t1.role_id and t2.mbr_typ_cd='R' left join krim_role_t t3 on t3.role_id = t2.mbr_id left join krim_typ_t t4 on t4.kim_typ_id=t1.kim_typ_id");
		while (rs != null && rs.next()) {
			Role rec = tstMap.get(rs.getString("ROLE_ID"));
			if (rec == null) {
				rec = new Role();
				rec.setRoleId(rs.getString("ROLE_ID"));
				rec.setTypeNmspc(rs.getString("KIM_TYP_NMSPC"));
				rec.setTypeName(rs.getString("KIM_TYP_NM"));
				rec.setNmspcCd(rs.getString("NMSPC_CD"));
				rec.setRoleNm(rs.getString("ROLE_NM"));
				rec.setDesc(rs.getString("DESC_TXT"));
				rec.setActive(rs.getString("ACTV_IND"));
				tstMap.put(rs.getString("ROLE_ID"), rec);
			}
			if (rs.getString("mbrId") != null) {
				Role mbr = new Role();
				mbr.setRoleMbrId(rs.getString("role_mbr_id"));
				mbr.setRoleId(rs.getString("mbrId"));
				mbr.setTypeId(rs.getString("mbrTypId"));
				mbr.setNmspcCd(rs.getString("mbrNmspc"));
				mbr.setRoleNm(rs.getString("mbrNm"));
				mbr.setDesc(rs.getString("mbrDesc"));
				mbr.setActive(rs.getString("mbrActv"));
				rec.getMemberRoles().add(mbr);
			}
		}
	}

	private static ConnectionDetail fintstbp() {
		ConnectionDetail fintstbp = new ConnectionDetail();
		fintstbp.setName("fintstbp");
		fintstbp.setDriver("oracle.jdbc.driver.OracleDriver");
		//FIXME - removed configuration
		return fintstbp;
	}

	private static ConnectionDetail fintst() {
		ConnectionDetail fintst = new ConnectionDetail();
		fintst.setName("fintst");
		fintst.setDriver("oracle.jdbc.driver.OracleDriver");
		//FIXME - removed configuration
		return fintst;
	}
}
