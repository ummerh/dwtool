package com.lndb.dwtool.erm.kom;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;

public class TreeBuilder {
	public OrgNode buildOrgTree(String rootOrgCode) {
		OrgNode root = new OrgNode(rootOrgCode, rootOrgCode, null);
		try {
			Connection con = DatabaseConnection.newConnection(ConnectionDetail.configure("ooidev"));
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select distinct d.org_code, d.to_org_code, d.lvl from "
					+ "(select t.org_code, t.to_org_code, t.struct_type, t.start_date, t.end_date, level lvl "
					+ "from ooi_org_tree_v t connect by to_org_code = prior org_code start with t.to_org_code = '" + rootOrgCode
					+ "') d where d.struct_type = '" + "RS" + "' " + "and current_date between d.start_date and d.end_date order by d.lvl");
			HashMap<String, OrgNode> nodeMap = new HashMap<String, OrgNode>();
			nodeMap.put(rootOrgCode, root);

			while (rs != null && rs.next()) {
				String toOrgCode = rs.getString("to_org_code");
				if (nodeMap.get(toOrgCode) == null) {
					nodeMap.put(toOrgCode, new OrgNode(toOrgCode, toOrgCode, null));
				}
				String orgCode = rs.getString("org_code");
				if (nodeMap.get(orgCode) == null) {
					nodeMap.put(orgCode, new OrgNode(orgCode, orgCode, null));
				}
				nodeMap.get(toOrgCode).addChild(nodeMap.get(orgCode));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return root;
	}
}
