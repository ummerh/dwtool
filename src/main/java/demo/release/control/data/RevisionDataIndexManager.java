/**
 * 
 */
package demo.release.control.data;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Configuration;
import com.lndb.dwtool.erm.util.Connections;

/**
 * 
 */
public class RevisionDataIndexManager {
	public static final String[] DTT_COLS = new String[] { "ITEMID", "DEFECTNAME", "AERNUMBER", "REPORTER", "ASSIGNMENT", "CNTRB" };
	public static final String[] AER_COLS = new String[] { "ITEMNUMBER", "ASSIGNMENT", "TITLE", "CNTRB" };
	public static final String[] FISHEYE_COLS = new String[] { "PATH", "AUTHOR", "COMMENTS", "PROJECT" };
	public static final String[] ASR_COLS = new String[] { "ITEMID", "ITEMNUMBER", "TITLE", "GROUPNM" };
	public static String AER_LOC = Configuration.getProperty("lucene.index.dir") + "sharepoint-aer";
	public static String DTT_LOC = Configuration.getProperty("lucene.index.dir") + "sharepoint-dtt";
	public static String ASR_LOC = Configuration.getProperty("lucene.index.dir") + "sharepoint-asr";
	public static String FISHEYE_LOC = Configuration.getProperty("lucene.index.dir") + "fisheye-revs";
	private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
	private static Searcher dttSearcher = null;
	private static Searcher fisheyeSearcher = null;
	public static String AER_PRJS = "('Finance','OOI', 'Other')";
	public static String DTT_PRJS = "('Finance','OOI')";
	public static String ASR_PRJS = "('Finance','Misc')";

	static void initIndexReaders() {
		try {
			dttSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(DTT_LOC))));
			fisheyeSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(FISHEYE_LOC))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void closeIndexReaders() {
		try {
			if (dttSearcher != null) {
				dttSearcher.close();
			}
			if (fisheyeSearcher != null) {
				fisheyeSearcher.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void index() {
		try {
			new File(AER_LOC).mkdirs();
			new File(DTT_LOC).mkdirs();
			new File(FISHEYE_LOC).mkdirs();
			indexAERRevisions(AER_LOC);
			indexDTTRevisions(DTT_LOC);
			indexASRRevisions(ASR_LOC);
			indexFisheyeRevisions(FISHEYE_LOC);
			initIndexReaders();
			establishAERMap();
			establishDTTMap();
			establishASRMap();
			establishPreAERMap();
			establishAERDTTMapBasedOnFisheye();
			closeIndexReaders();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		index();
	}

	/**
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	static Connection getDBConnection() {
		return Connections.REVMETADB.newConnection();
	}

	static void indexFisheyeRevisions(String loc) throws Exception {
		IndexWriter writer = new IndexWriter(new SimpleFSDirectory(new File(loc)), analyzer, true, MaxFieldLength.UNLIMITED);
		System.out.println("Indexing to directory '" + loc + "'...");
		String sql = "select ID, PATH, AUTHOR, COMMENTS, PROJECT from FISHEYE_REVS t where not exists (select 1 from fisheye_ignored b where b.csid=t.csid)";
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		int count = 0;
		try {
			while (rs.next()) {
				Document d = new Document();
				d.add(new Field("ID", "" + rs.getString("ID"), Field.Store.YES, Field.Index.NO));
				d.add(new Field("PATH", "" + rs.getString("PATH"), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("AUTHOR", "" + rs.getString("AUTHOR"), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("COMMENTS", ("" + rs.getString("COMMENTS")), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("PROJECT", ("" + rs.getString("PROJECT")), Field.Store.NO, Field.Index.ANALYZED));
				writer.addDocument(d);
				count++;
			}
		} finally {
			DatabaseConnection.release(rs, stmt, conn);
		}
		System.out.println("Indexed " + count + " records");
		writer.optimize();
		writer.close();
		System.out.println("Indexing finished...");
	}

	static void indexAERRevisions(String loc) throws Exception {
		IndexWriter writer = new IndexWriter(new SimpleFSDirectory(new File(loc)), analyzer, true, MaxFieldLength.UNLIMITED);
		System.out.println("Indexing to directory '" + loc + "'...");
		String sql = "select ID, ITEMNUMBER, ASSIGNMENT, TITLE, CNTRB from sharepoint_aer where team in " + AER_PRJS;
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		int count = 0;
		try {
			while (rs.next()) {
				Document d = new Document();
				d.add(new Field("ID", "" + rs.getString("ID"), Field.Store.YES, Field.Index.NO));
				d.add(new Field("ITEMNUMBER", ("" + prepareAERKeyTerm(rs.getString("ITEMNUMBER"))), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("ASSIGNMENT", "" + rs.getString("ASSIGNMENT"), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("TITLE", ("" + prepareAERKeyTerm(rs.getString("TITLE"))), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("CNTRB", ("" + prepareAERKeyTerm(rs.getString("CNTRB"))), Field.Store.NO, Field.Index.ANALYZED));
				writer.addDocument(d);
				count++;
			}
		} finally {
			DatabaseConnection.release(rs, stmt, conn);
		}
		System.out.println("Indexed " + count + " records");
		writer.optimize();
		writer.close();
		System.out.println("Indexing finished...");
	}

	private static String prepareAERKeyTerm(String key) {
		if (key != null) {
			String[] tkns = key.split("_");
			String result1 = "";
			String result2 = "";
			for (String string : tkns) {
				result1 = result1 + string;
				result2 = result2 + " " + string;
			}
			return result1 + " " + result2;

		}
		return "";
	}

	private static String prepareDTTKeyTerm(String key) {
		if (key != null) {
			String[] tkns = key.split("-");
			String result1 = "";
			String result2 = "";
			for (String string : tkns) {
				result1 = result1 + string;
				result2 = result2 + " " + string;
			}
			return result1 + " " + result2 + " DTT-" + key;

		}
		return "";
	}

	static void indexDTTRevisions(String loc) throws Exception {
		IndexWriter writer = new IndexWriter(new SimpleFSDirectory(new File(loc)), analyzer, true, MaxFieldLength.UNLIMITED);
		System.out.println("Indexing to directory '" + loc + "'...");
		String sql = "SELECT ID, ITEMID, DEFECTNAME, AERNUMBER, REPORTER, ASSIGNMENT, CNTRB FROM SHAREPOINT_DTT where team in " + DTT_PRJS;
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		int count = 0;
		try {
			while (rs.next()) {
				Document d = new Document();
				d.add(new Field("ID", "" + rs.getString("ID"), Field.Store.YES, Field.Index.NO));
				d.add(new Field("ITEMID", "" + prepareDTTKeyTerm(rs.getString("ITEMID")), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("DEFECTNAME", ("" + rs.getString("DEFECTNAME")), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("AERNUMBER", ("" + extractAERKey(rs.getString("DEFECTNAME"), rs.getString("AERNUMBER"))), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("REPORTER", "" + rs.getString("REPORTER"), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("ASSIGNMENT", "" + rs.getString("ASSIGNMENT"), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("CNTRB", "" + rs.getString("CNTRB"), Field.Store.NO, Field.Index.ANALYZED));
				writer.addDocument(d);
				count++;
			}
		} finally {
			DatabaseConnection.release(rs, stmt, conn);
		}
		System.out.println("Indexed " + count + " records");
		writer.optimize();
		writer.close();
		System.out.println("Indexing finished...");
	}

	public static String extractAERKey(String defectName, String aerNumber) {
		String exp = "([\\d]{3,}+)";
		Pattern p = Pattern.compile(exp);
		if (StringUtils.isNotBlank(aerNumber)) {
			Matcher matcher = p.matcher(aerNumber);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		if (StringUtils.isNotBlank(defectName)) {
			Matcher matcher = p.matcher(defectName);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return aerNumber;
	}

	static void indexASRRevisions(String loc) throws Exception {
		IndexWriter writer = new IndexWriter(new SimpleFSDirectory(new File(loc)), analyzer, true, MaxFieldLength.UNLIMITED);
		System.out.println("Indexing to directory '" + loc + "'...");
		String sql = "SELECT ID, ITEMID, ITEMNUMBER, TITLE, GROUPNM FROM SHAREPOINT_ASR";
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		int count = 0;
		try {
			while (rs.next()) {
				Document d = new Document();
				d.add(new Field("ID", "" + rs.getString("ID"), Field.Store.YES, Field.Index.NO));
				d.add(new Field("ITEMID", "" + prepareDTTKeyTerm(rs.getString("ITEMID")), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("ITEMNUMBER", ("" + rs.getString("ITEMNUMBER")), Field.Store.NO, Field.Index.ANALYZED));
				d.add(new Field("TITLE", ("" + rs.getString("TITLE")), Field.Store.NO, Field.Index.ANALYZED));
				writer.addDocument(d);
				count++;
			}
		} finally {
			DatabaseConnection.release(rs, stmt, conn);
		}
		System.out.println("Indexed " + count + " records");
		writer.optimize();
		writer.close();
		System.out.println("Indexing finished...");
	}

	public static Set<String> performSearch(String searchedTerm, Searcher searcher, String[] searchColumns, boolean wildcard) throws Exception {
		String searchTerm = prepareSearchTerm(searchedTerm, wildcard);
		return performRawSearch(searchTerm, searcher, searchColumns);
	}

	public static Set<String> performRawSearch(String searchTerm, Searcher searcher, String[] searchColumns) throws Exception {
		HashSet<String> ids = new HashSet<String>();
		if (StringUtils.isBlank(searchTerm)) {
			return ids;
		}
		try {
			if (StringUtils.isBlank(searchTerm)) {
				return ids;
			}
			Query query = new MultiFieldQueryParser(Version.LUCENE_30, searchColumns, analyzer).parse(searchTerm);
			TopDocs results = searcher.search(query, searcher.maxDoc());
			if (results.totalHits == 0) {
				return ids;
			}
			for (int i = 0; i < results.totalHits; i++) {
				Document doc = searcher.doc(results.scoreDocs[i].doc);
				ids.add(doc.get("ID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}

	/**
	 * @param searchTerm
	 */
	private static String prepareSearchTerm(String searchTerm, boolean wildcard) {
		if (searchTerm == null || "null".equals(searchTerm)) {
			return "";
		}
		String wc = "";
		if (wildcard) {
			wc = "*";
		}
		String cleanedTerm = handleSpecialChars(searchTerm);
		String[] terms = cleanedTerm.split(" ");
		String formtted = "";
		for (String term : terms) {
			if (term == null || term.trim().length() == 0 || StopAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase())) {
				continue;
			}
			if (formtted.equals("")) {
				formtted = " +" + term + wc;
			} else {
				formtted = formtted + " +" + term + wc;
			}
		}
		return formtted.trim();
	}

	/**
	 * @param searchTerm
	 * @return
	 */
	public static String handleSpecialChars(String searchTerm) {
		int pos = 0;
		char[] tmp = new char[searchTerm.length() * 2];
		char[] chars = searchTerm.toCharArray();
		for (char c : chars) {
			if (c == ',' || c == '/' || c == '`' || c == '@' || c == '#' || c == '$' || c == '%' || c == '=' || c == ';' || c == '<' || c == '>') {
				continue;
			}
			if (c == '_') {
				tmp[pos++] = ' ';
				continue;
			}
			if (c == '+' || c == '-' || c == '&' || c == '|' || c == '!' || c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '^' || c == '"' || c == '~' || c == '*'
					|| c == '?' || c == ':' || c == '\\') {
				tmp[pos++] = '\\';
			}
			tmp[pos++] = c;
		}
		String cleanedTerm = new String(tmp, 0, pos);
		return cleanedTerm;
	}

	public static String extractKey(String val) {
		String exp = "([\\d]{3,}+)";
		Pattern p = Pattern.compile(exp);
		Matcher matcher = p.matcher(val);
		if (matcher.find()) {
			String key = matcher.group(1);
			return key + " OR \"" + val + "*\" NOT \"ASR " + key + "\" NOT \"DTT " + key + "\"";
		}
		return val;
	}

	private static void establishAERMap() {
		try {
			Connection conn = getDBConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("delete from aer_to_dtt_map");
			stmt.execute("delete from aer_to_fisheye_map");
			ResultSet rs = stmt.executeQuery("select id, itemnumber from sharepoint_aer where team in " + AER_PRJS);
			PreparedStatement aerToDtt = conn.prepareStatement("insert into aer_to_dtt_map(aerid, dttid) values(?,?)");
			PreparedStatement aerToFisheye = conn.prepareStatement("insert into aer_to_fisheye_map(aerid, fisheyeid) values(?,?)");
			try {
				while (rs.next()) {
					String aerId = rs.getString("id");
					String aerKey = extractAERKey("", rs.getString("itemnumber"));
					Set<String> dttIds = performRawSearch(aerKey, dttSearcher, new String[] { "DEFECTNAME", "AERNUMBER" });
					for (String dttId : dttIds) {
						aerToDtt.setString(1, aerId);
						aerToDtt.setString(2, dttId);
						aerToDtt.addBatch();
					}
					String aerNumber = extractKey(rs.getString("itemnumber"));
					Set<String> fisheyeIds = performRawSearch(aerNumber, fisheyeSearcher, new String[] { "COMMENTS" });
					for (String fishid : fisheyeIds) {
						aerToFisheye.setString(1, aerId);
						aerToFisheye.setString(2, fishid);
						aerToFisheye.addBatch();
					}
				}
				aerToDtt.executeBatch();
				aerToFisheye.executeBatch();

			} finally {
				aerToDtt.close();
				aerToFisheye.close();
				DatabaseConnection.release(rs, stmt, conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void establishPreAERMap() {
		try {
			HashMap<String, Integer> aerKeysMap = new HashMap<String, Integer>();
			Connection conn = getDBConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select id, itemnumber from sharepoint_aer where team in " + AER_PRJS);
			PreparedStatement updatePreAer = conn.prepareStatement("update sharepoint_pre_aer t set t.ref_aer=? where id= ?");
			try {
				while (rs.next()) {
					String aerKey = extractAERKey("", rs.getString("itemnumber"));
					if (StringUtils.isNotBlank(aerKey)) {
						aerKeysMap.put(aerKey, rs.getInt("id"));
					}
				}
				rs.close();
				rs = stmt.executeQuery("select id, title from sharepoint_pre_aer");
				while (rs.next()) {
					String aerRefKey = extractAERKey(rs.getString("title"), "");
					if (StringUtils.isNotBlank(aerRefKey)) {
						if (aerKeysMap.containsKey(aerRefKey)) {
							updatePreAer.setInt(1, aerKeysMap.get(aerRefKey));
							updatePreAer.setInt(2, rs.getInt("id"));
							updatePreAer.addBatch();
						}
					}
				}
				updatePreAer.executeBatch();

			} finally {
				updatePreAer.close();
				DatabaseConnection.release(rs, stmt, conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void establishDTTMap() {
		try {
			Connection conn = getDBConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("delete from dtt_to_fisheye_map");
			ResultSet rs = stmt.executeQuery("select id, itemid from sharepoint_dtt where team in " + DTT_PRJS);
			PreparedStatement dttToFisheye = conn.prepareStatement("insert into dtt_to_fisheye_map(dttid, fisheyeid) values(?,?)");
			try {
				while (rs.next()) {
					String dttId = rs.getString("id");
					String dttNumber = rs.getString("itemid");
					Set<String> fisheyeIds = performRawSearch("\"DTT " + dttNumber + "\"", fisheyeSearcher, FISHEYE_COLS);
					Set<String> addIds = performRawSearch("\"Defect " + dttNumber + "\"", fisheyeSearcher, FISHEYE_COLS);

					for (String fishid : fisheyeIds) {
						dttToFisheye.setString(1, dttId);
						dttToFisheye.setString(2, fishid);
						dttToFisheye.addBatch();
					}

					for (String fishid : addIds) {
						dttToFisheye.setString(1, dttId);
						dttToFisheye.setString(2, fishid);
						dttToFisheye.addBatch();
					}
				}
				dttToFisheye.executeBatch();

			} finally {
				dttToFisheye.close();
				DatabaseConnection.release(rs, stmt, conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void establishASRMap() {
		try {
			Connection conn = getDBConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("delete from asr_to_fisheye_map");
			ResultSet rs = stmt.executeQuery("select id, itemid from sharepoint_asr where groupnm in " + ASR_PRJS);
			PreparedStatement dttToFisheye = conn.prepareStatement("insert into asr_to_fisheye_map(asrid, fisheyeid) values(?,?)");
			try {
				while (rs.next()) {
					String dttId = rs.getString("id");
					String dttNumber = rs.getString("itemid");
					Set<String> fisheyeIds = performRawSearch("\"ASR " + dttNumber + "\"", fisheyeSearcher, FISHEYE_COLS);
					for (String fishid : fisheyeIds) {
						dttToFisheye.setString(1, dttId);
						dttToFisheye.setString(2, fishid);
						dttToFisheye.addBatch();
					}
				}
				dttToFisheye.executeBatch();

			} finally {
				dttToFisheye.close();
				DatabaseConnection.release(rs, stmt, conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void establishAERDTTMapBasedOnFisheye() {
		try {
			Connection conn = getDBConnection();
			Statement stmt = conn.createStatement();
			try {
				stmt.execute("insert into aer_to_dtt_map select a.aerid, b.dttid from aer_to_fisheye_map a join dtt_to_fisheye_map b on a.fisheyeid = b.fisheyeid where not exists (select 1 from aer_to_dtt_map c where c.aerid = a.aerid and c.dttid = b.dttid)");
			} finally {
				DatabaseConnection.release(stmt, conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
