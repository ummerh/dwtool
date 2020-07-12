/**
 * 
 */
package demo.lucene.catalog.search;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.lndb.dwtool.erm.util.Connections;

public class CatalogTree {
    private static final HashMap<String, Node> nodeMap = new HashMap<String, Node>();
    private static final TreeMap<Integer, List<Node>> levelMap = new TreeMap<Integer, List<Node>>();
    private static final HashMap<String, List<Node>> groupMap = new HashMap<String, List<Node>>();

    private CatalogTree() {
	super();
    }

    /**
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void initialize() {
	if (nodeMap.isEmpty()) {
	    try {
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		int level = -1;
		int found = -1;
		while (found == -1 || found > 0) {
		    level++;
		    found = findNodes(stmt, level);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * @param stmt
     * @param level
     * @return
     * @throws SQLException
     */
    private static int findNodes(Statement stmt, int level) throws SQLException {
	int found = 0;
	ResultSet rs = stmt.executeQuery(buildQuery(level));
	while (rs.next()) {
	    String id = rs.getString("catalog_subgroup_id");
	    String parentId = rs.getString("prior_catalog_subgroup_id");
	    String name = rs.getString("catalog_subgroup_desc");
	    String grp = rs.getString("catalog_group_nm");
	    String grpCode = rs.getString("catalog_group_cd");
	    Node parent = null;
	    Node child = null;
	    if (parentId != null) {
		parent = nodeMap.get(parentId);
		child = new Node(parent, id, name);
		if (parent != null) {
		    parent.addChild(child);
		}
	    } else {
		child = new Node(null, id, name);
	    }
	    child.setGroup(grp);
	    child.setGroupCode(grpCode);
	    nodeMap.put(id, child);

	    // begin - add to level map
	    List<Node> levelList = levelMap.get(level);
	    if (levelList == null) {
		levelList = new ArrayList<Node>();
		levelMap.put(level, levelList);
	    }
	    levelList.add(child);
	    // end - add to level map

	    // begin - add to group map
	    List<Node> grpList = groupMap.get(grpCode);
	    if (grpList == null) {
		grpList = new ArrayList<Node>();
		groupMap.put(grpCode, grpList);
	    }
	    grpList.add(child);
	    // end - add to group map

	    found++;
	}
	return found;
    }

    /**
     * @return
     */
    private static String buildQuery(int level) {
	String piece = "select catalog_subgroup_id from mm_catalog_subgroup_t where prior_catalog_subgroup_id ";
	String sql = "";
	for (int i = 0; i <= level; i++) {
	    sql = piece + (i == 0 ? " is null " : ("in (" + sql + ") "));
	}
	sql = sql.substring("select catalog_subgroup_id from mm_catalog_subgroup_t where".length());
	sql = "select a.catalog_subgroup_id, a.prior_catalog_subgroup_id, a.catalog_subgroup_desc, a.catalog_group_cd, b.catalog_group_nm from mm_catalog_subgroup_t a join mm_catalog_group_t b on  a.catalog_group_cd=b.catalog_group_cd where"
		+ sql;
	return sql;
    }

    /**
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    static Connection getDBConnection() {
	return Connections.KMMDEMO.newConnection();
    }

    /**
     * @param list
     */
    public static void checkCyclicLevel(List<Node> list) {
	for (Node node : list) {
	    List<Node> parents = node.getAncestors();
	    for (Node node3 : parents) {
		if (node.getId().equals(node3.getId())) {
		    System.out.println("Child already in parent " + node3.getId());
		    return;
		}
	    }
	    List<Node> children = node.getDescendants();

	    for (Node node2 : children) {
		for (Node node3 : parents) {
		    if (node.getId().equals(node3.getId()) || node3.getId().equals(node2.getId())) {
			System.out.println("Parent found in children " + node3.getId());
			return;
		    }
		}
	    }
	}
    }

    public static Node getNodeById(String id) {
	return nodeMap.get(id);
    }

    public static Node getParent(String id, int level) {
	Node node = nodeMap.get(id);
	List<Node> ancestors = node.getAncestors();
	if (!ancestors.isEmpty() && ancestors.size() - level > -1) {
	    return ancestors.get(ancestors.size() - level);
	}
	return node;
    }

    public static List<Node> getNodesByLevel(int level) {
	return levelMap.get(level);
    }

    public static List<Node> getNodesByGroupCode(String groupCode) {
	return groupMap.get(groupCode);
    }
}
