/**
 * 
 */
package demo.release.control.data.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.SimpleFSDirectory;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Connections;

import demo.release.control.data.RevisionDataIndexManager;

public class RevisionDataSearcher {

	private static Searcher aerSearcher = null;
	private static Searcher dttSearcher = null;
	private static Searcher asrSearcher = null;
	private static Searcher fisheyeSearcher = null;

	public static List<AerDetail> buildAERDetails(Set<String> ids) throws Exception {
		List<AerDetail> aerDetails = new ArrayList<AerDetail>();
		if (ids.isEmpty()) {
			return aerDetails;
		}
		String keys = buildKeys(ids);
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select itemnumber, title,team, assignment, aerid, cntrb from sharepoint_aer where id in (" + keys + ")");
		while (rs != null && rs.next()) {
			AerDetail aerDetail = new AerDetail();
			aerDetail.setItemnumber(rs.getString("itemnumber"));
			aerDetail.setTitle(rs.getString("title"));
			aerDetail.setTeam(rs.getString("team"));
			aerDetail.setAssignment(rs.getString("assignment"));
			aerDetail.setAerid(rs.getString("aerid"));
			aerDetail.setCntrb(rs.getString("cntrb"));
			aerDetails.add(aerDetail);
		}
		return aerDetails;
	}

	public static List<DttDetail> buildDTTDetails(Set<String> ids) throws Exception {
		List<DttDetail> dttDetails = new ArrayList<DttDetail>();
		if (ids.isEmpty()) {
			return dttDetails;
		}
		String keys = buildKeys(ids);
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select itemid, defectname, aernumber,team, reporter, assignment, cntrb, cntrbstatus  from sharepoint_dtt where id in (" + keys + ")");
		while (rs != null && rs.next()) {
			DttDetail detail = new DttDetail();
			detail.setItemid(rs.getString("itemid"));
			detail.setDefectname(rs.getString("defectname"));
			detail.setAernumber(rs.getString("aernumber"));
			detail.setTeam(rs.getString("team"));
			detail.setReporter(rs.getString("reporter"));
			detail.setAssignment(rs.getString("assignment"));
			detail.setCntrb(rs.getString("cntrb"));
			detail.setCntrbStatus(rs.getString("cntrbstatus"));
			dttDetails.add(detail);
		}
		DatabaseConnection.release(rs, stmt, con);
		return dttDetails;
	}

	public static List<AsrDetail> buildASRDetails(Set<String> ids) throws Exception {
		List<AsrDetail> asrDetails = new ArrayList<AsrDetail>();
		if (ids.isEmpty()) {
			return asrDetails;
		}
		String keys = buildKeys(ids);
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select itemid, itemnumber, title, groupnm from sharepoint_asr where id in (" + keys + ")");
		while (rs != null && rs.next()) {
			AsrDetail detail = new AsrDetail();
			detail.setItemid(rs.getString("itemid"));
			detail.setItemnumber(rs.getString("itemnumber"));
			detail.setTitle(rs.getString("title"));
			detail.setGroupnm(rs.getString("groupnm"));
			asrDetails.add(detail);
		}
		DatabaseConnection.release(rs, stmt, con);
		return asrDetails;
	}

	/**
	 * @throws Exception
	 */

	public static List<FisheyeDetail> buildFisheyeDetails(Set<String> ids) throws Exception {
		List<FisheyeDetail> fisheyeDetails = new ArrayList<FisheyeDetail>();

		if (ids.isEmpty()) {
			return fisheyeDetails;
		}
		String keys = buildKeys(ids);
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id, csid, comments, path, project from fisheye_revs where id in (" + keys
				+ ") and path not like '%.jar' and path not like '%.war' order by path, csid");
		while (rs != null && rs.next()) {
			String path = rs.getString("path");
			String displayPath = getDisplayablePath(path);
			String csid = rs.getString("csid");
			FisheyeDetail detail = new FisheyeDetail();
			detail.setId(rs.getInt("id"));
			detail.setCsid(csid);
			String comments = rs.getString("comments");
			if (comments != null && comments.contains("Committed on the Free edition")) {
				comments = comments.substring(0, comments.indexOf("Committed on the Free edition"));
			}
			detail.setComments(comments);
			detail.setPath(path);
			detail.setDisplayPath(displayPath);
			detail.setProject(rs.getString("project"));
			fisheyeDetails.add(detail);
		}
		DatabaseConnection.release(rs, stmt, con);
		return fisheyeDetails;
	}

	/**
	 * @param path
	 * @return
	 */
	private static String getDisplayablePath(String path) {
		String displayPath = path;
		String dir = new File(path).getParent();
		if (dir == null) {
			dir = "";
		}
		String fileNm = new File(path).getName();
		int size = 30;
		if (path.length() > size) {
			if (dir.length() > size - 10) {
				displayPath = dir.substring(0, size - 10) + "... \\";
			} else {
				displayPath = dir + "\\ ";
			}
			if (fileNm.length() > size - 10) {
				displayPath = displayPath + fileNm.substring(0, size - 10);
			} else {
				displayPath = displayPath + fileNm;
			}
		}
		return displayPath;
	}

	private static void buildReverseFromAerToDttMap(Set<String> aerIds, Set<String> dttIds) throws SQLException {
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select aerid from aer_to_dtt_map where dttid in (" + buildKeys(dttIds) + ")");
		try {
			while (rs.next()) {
				aerIds.add(rs.getString("aerid"));
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
	}

	private static void buildFromAerToDttMap(Set<String> dttIds, Set<String> aerIds) throws SQLException {
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select dttid from aer_to_dtt_map where aerid in (" + buildKeys(aerIds) + ")");
		try {
			while (rs.next()) {
				dttIds.add(rs.getString("dttid"));
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
	}

	/**
	 * @param aerIds
	 * @param fisheyeIds
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private static void buildReverseFromAerToFisheyeMap(Set<String> aerIds, Set<String> fisheyeIds) throws SQLException {
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select aerid from aer_to_fisheye_map where fisheyeid in (" + buildKeys(fisheyeIds) + ")");
		try {
			while (rs.next()) {
				aerIds.add(rs.getString("aerid"));
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
	}

	private static void buildFromAerToFisheyeMap(Set<String> fisheyeIds, Set<String> aerIds) throws SQLException {
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select fisheyeid from aer_to_fisheye_map where aerid in (" + buildKeys(aerIds) + ")");
		try {
			while (rs.next()) {
				fisheyeIds.add(rs.getString("fisheyeid"));
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
	}

	private static void buildReverseFromDTTToFisheyeMap(Set<String> dttIds, Set<String> fisheyeIds) throws SQLException {
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select dttid from dtt_to_fisheye_map where fisheyeid in (" + buildKeys(fisheyeIds) + ")");
		try {
			while (rs.next()) {
				dttIds.add(rs.getString("dttid"));
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
	}

	private static void buildReverseFromASRToFisheyeMap(Set<String> asrIds, Set<String> fisheyeIds) throws SQLException {
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select asrid from asr_to_fisheye_map where fisheyeid in (" + buildKeys(fisheyeIds) + ")");
		try {
			while (rs.next()) {
				asrIds.add(rs.getString("asrid"));
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
	}

	private static void buildFromDTTToFisheyeMap(Set<String> fisheyeIds, Set<String> dttIds) throws SQLException {
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select fisheyeid from dtt_to_fisheye_map where dttid in (" + buildKeys(dttIds) + ")");
		try {
			while (rs.next()) {
				fisheyeIds.add(rs.getString("fisheyeid"));
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
	}

	/**
	 * @param ids
	 * @return
	 */
	private static String buildKeys(Set<String> ids) {
		String keys = "";
		int count = 0;
		for (String id : ids) {
			count++;
			if (count <= 1000) {
				keys = keys + id + ",";
			} else {
				break;
			}
		}
		if (!ids.isEmpty()) {
			keys = keys.substring(0, keys.length() - 1);
		}
		return keys;
	}

	public static void buildSearchResultKeys(String term, Set<String> aerIds, Set<String> dttIds, HashSet<String> asrIds, Set<String> fisheyeIds) throws Exception {
		aerIds.addAll(RevisionDataIndexManager.performSearch(term, aerSearcher, RevisionDataIndexManager.AER_COLS, true));
		dttIds.addAll(RevisionDataIndexManager.performSearch(term, dttSearcher, RevisionDataIndexManager.DTT_COLS, true));
		asrIds.addAll(RevisionDataIndexManager.performSearch(term, asrSearcher, RevisionDataIndexManager.ASR_COLS, true));
		fisheyeIds.addAll(RevisionDataIndexManager.performRawSearch("\"" + term.replace("DTT-", " ").replace("ASR-", " ") + "\"", fisheyeSearcher, RevisionDataIndexManager.FISHEYE_COLS));

		// iterate mappings
		int depth = 1;
		for (int i = 0; i < depth; i++) {
			if (!fisheyeIds.isEmpty()) {
				buildReverseFromAerToFisheyeMap(aerIds, fisheyeIds);
				buildReverseFromDTTToFisheyeMap(dttIds, fisheyeIds);
				buildReverseFromASRToFisheyeMap(asrIds, fisheyeIds);
				// dont reverse map further if fisheye match is found
				break;
			}
			if (!dttIds.isEmpty()) {
				buildReverseFromAerToDttMap(aerIds, dttIds);
				buildFromDTTToFisheyeMap(fisheyeIds, dttIds);
			}
			if (!aerIds.isEmpty()) {
				buildFromAerToDttMap(dttIds, aerIds);
				buildFromAerToFisheyeMap(fisheyeIds, aerIds);
			}
		}
	}

	/**
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	static Connection getDBConnection() {
		return Connections.REVMETADB.newConnection();
	}

	public static void init() {
		try {
			if (aerSearcher == null) {
				aerSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(RevisionDataIndexManager.AER_LOC))));
			}
			if (dttSearcher == null) {
				dttSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(RevisionDataIndexManager.DTT_LOC))));
			}
			if (asrSearcher == null) {
				asrSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(RevisionDataIndexManager.ASR_LOC))));
			}
			if (fisheyeSearcher == null) {
				fisheyeSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(RevisionDataIndexManager.FISHEYE_LOC))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void reset() {
		try {
			if (aerSearcher != null) {
				aerSearcher.close();
				aerSearcher = null;
				aerSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(RevisionDataIndexManager.AER_LOC))));
			}
			if (dttSearcher != null) {
				dttSearcher.close();
				dttSearcher = null;
				dttSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(RevisionDataIndexManager.DTT_LOC))));
			}
			if (asrSearcher != null) {
				asrSearcher.close();
				asrSearcher = null;
				asrSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(RevisionDataIndexManager.ASR_LOC))));
			}
			if (fisheyeSearcher != null) {
				fisheyeSearcher.close();
				fisheyeSearcher = null;
				fisheyeSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(RevisionDataIndexManager.FISHEYE_LOC))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static List<AerDetail> getAersByProject(String project) throws SQLException {
		List<AerDetail> details = new ArrayList<AerDetail>();
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct c.itemnumber, c.title, c.team, c.assignment, c.aerid from aer_to_fisheye_map a, "
				+ "fisheye_revs b, sharepoint_aer c where c.id = a.aerid and a.fisheyeid = b.id and b.project = '" + project + "' order by itemnumber");
		try {
			while (rs.next()) {
				AerDetail aerDetail = new AerDetail();
				aerDetail.setItemnumber(rs.getString("itemnumber"));
				aerDetail.setTitle(rs.getString("title"));
				aerDetail.setTeam(rs.getString("team"));
				aerDetail.setAssignment(rs.getString("assignment"));
				aerDetail.setAerid(rs.getString("aerid"));
				details.add(aerDetail);
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
		return details;
	}

	public static List<DttDetail> getDttsByProject(String project) throws SQLException {
		List<DttDetail> details = new ArrayList<DttDetail>();
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct c.itemid, c.defectname, c.aernumber,c.team, c.reporter, c.assignment, c.cntrb, c.cntrbstatus from dtt_to_fisheye_map a, fisheye_revs b, "
				+ "sharepoint_dtt c where c.id = a.dttid and a.fisheyeid = b.id and b.project = '" + project + "' order by itemid");
		try {
			while (rs.next()) {
				DttDetail detail = new DttDetail();
				detail.setItemid(rs.getString("itemid"));
				detail.setDefectname(rs.getString("defectname"));
				detail.setAernumber(rs.getString("aernumber"));
				detail.setTeam(rs.getString("team"));
				detail.setReporter(rs.getString("reporter"));
				detail.setAssignment(rs.getString("assignment"));
				detail.setCntrb(rs.getString("cntrb"));
				detail.setCntrbStatus(rs.getString("cntrbstatus"));
				details.add(detail);
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
		return details;
	}

	public static List<AsrDetail> getAsrsByProject(String project) throws SQLException {
		List<AsrDetail> details = new ArrayList<AsrDetail>();
		Connection con = getDBConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct c.itemid, c.itemnumber, c.title, c.groupnm from asr_to_fisheye_map a, fisheye_revs b, "
				+ "sharepoint_asr c where c.id = a.asrid and a.fisheyeid = b.id and b.project = '" + project + "' order by itemid");
		try {
			while (rs.next()) {
				AsrDetail detail = new AsrDetail();
				detail.setItemid(rs.getString("itemid"));
				detail.setItemnumber(rs.getString("itemnumber"));
				detail.setTitle(rs.getString("title"));
				detail.setGroupnm(rs.getString("groupnm"));
				details.add(detail);
			}
		} finally {
			DatabaseConnection.release(rs, stmt, con);
		}
		return details;
	}

	public static void main(String[] args) {

		findJiraStatuses();

	}

	private static void findJiraStatuses() {
		try {
			init();
			HashSet<String> dttIds = new HashSet<String>();
			BufferedReader rdr = new BufferedReader(new FileReader(new File("/java/projects/kuali-erd-web/external/jira/jiras.txt")));
			String term = null;
			while ((term = rdr.readLine()) != null) {
				HashSet<String> csids = new HashSet<String>();
				dttIds.addAll(RevisionDataIndexManager.performSearch(term, dttSearcher, RevisionDataIndexManager.DTT_COLS, true));
				csids.addAll(RevisionDataIndexManager.performRawSearch("\"" + term.replace("DTT-", " ").replace("ASR-", " ") + "\"", fisheyeSearcher, RevisionDataIndexManager.FISHEYE_COLS));
				if (!csids.isEmpty()) {
					System.out.println("Code change found for " + term);
				}
			}

			List<DttDetail> dttDetails = RevisionDataSearcher.buildDTTDetails(dttIds);
			for (DttDetail dttDetail : dttDetails) {
				System.out.println(dttDetail.getItemid() + " - " + dttDetail.getCntrb() + " - " + dttDetail.getCntrbStatus());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
