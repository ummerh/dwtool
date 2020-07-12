package demo.lucene.catalog.search;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Connections;
import com.lndb.dwtool.erm.util.StopWatch;

import demo.lucene.catalog.search.SearchResult.ItemResult;

/**
 * Servlet implementation class for Servlet: CatalogSearchServlet
 * 
 */
public class CatalogSearchServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    static String styles = "<link rel=\"stylesheet\" href=\"./styles/blueprint/screen.css\" type=\"text/css\" media=\"screen, projection\"><link rel=\"stylesheet\" href=\"./styles/blueprint/print.css\" type=\"text/css\" media=\"print\"><link rel=\"stylesheet\" href=\"blueprint/ie.css\" type=\"text/css\" media=\"screen, projection\" />";

    private static class HtmlCatalogView {
	private String results = "";
	private String tree = "";

	public String getResults() {
	    return results;
	}

	public String getTree() {
	    return tree;
	}

	public void setResults(String results) {
	    this.results = results;
	}

	public void setTree(String tree) {
	    this.tree = tree;
	}

    }

    public static final int PAGE_SIZE = 10;
    static final long serialVersionUID = 1L;

    static Connection getDBConnection() {
	return Connections.KMMDEMO.newConnection();
    }

    public CatalogSearchServlet() {
	super();
    }

    /**
     * @param request
     * @param view
     */
    private void displayHome(HttpServletRequest request, HtmlCatalogView view) {
	view.setTree(loadFullCatalogTree());
	request.getSession().setAttribute("searchTerm", "");
    }

    /**
     * @param request
     * @param view
     * @param page
     * @return
     */
    private SearchResult displayPage(HttpServletRequest request, HtmlCatalogView view, int page) {
	SearchResult searchResult;
	searchResult = (SearchResult) request.getSession().getAttribute("searchResult");
	view.setResults(prepareResults(searchResult, page));
	view.setTree(searchResult.getMenuTree());
	return searchResult;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	process(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	process(request, response);
    }

    private String indent(int level) {
	String space = "<br/>";
	for (int i = 0; i < level; i++) {
	    space = space + "&nbsp;&nbsp;";
	}
	return space;
    }

    private String loadAncestorPath(Node node, String pathVal, int level, SearchResult searchResult) {
	String path = pathVal;
	path = indent(level) + "<a href=\"searchCatalog?subgroupId=" + node.getId() + "&level=" + level + "\">" + node.getName() + prepareSubGrpCount(searchResult, node) + "</a>" + path;
	if (node.getParent() != null) {
	    return loadAncestorPath(node.getParent(), path, level - 1, searchResult);
	}
	return path;
    }

    /**
     * @param tree
     * @return
     */
    private String loadFullCatalogTree() {
	String tree = "";
	List<Node> nodes = CatalogTree.getNodesByLevel(0);
	tree = loadImmediateNodes(nodes, true, 2, null, tree);
	return tree;
    }

    /**
     * @param tree
     * @param nodes
     * @return
     */
    private String loadImmediateNodes(List<Node> nodes, boolean addGrp, int level, SearchResult searchResult, String treeVal) {
	String tree = treeVal;
	HashMap<String, List<Node>> groupMap = new HashMap<String, List<Node>>();
	for (Node node : nodes) {
	    String grp = node.getGroup();
	    List<Node> grpList = groupMap.get(grp);
	    if (grpList == null) {
		grpList = new ArrayList<Node>();
		groupMap.put(grp, grpList);
	    }
	    grpList.add(node);
	}
	Set<String> grps = groupMap.keySet();
	ArrayList<String> sorted = new ArrayList<String>();
	sorted.addAll(grps);
	Collections.sort(sorted);
	for (String grp : sorted) {
	    String grpTree = "";
	    List<Node> grpNodes = groupMap.get(grp);
	    for (Node node : grpNodes) {
		if ((level > 2 || node.getParent() == null) && (searchResult == null || searchResult.getSubGroupCount(node.getId()) > 0)) {
		    grpTree = grpTree + indent(level) + "<a href=\"searchCatalog?subgroupId=" + node.getId() + "&level=" + level + "\">" + node.getName() + prepareSubGrpCount(searchResult, node)
			    + "</a>";
		}
	    }
	    if (addGrp) {
		grpTree = "<a href=\"searchCatalog?level=0&group=" + grpNodes.get(0).getGroupCode() + "\">" + grp + prepareGrpCount(searchResult, grpNodes.get(0)) + "</a>" + grpTree;
	    }
	    tree = tree + grpTree + "<br/>";
	}
	return tree;
    }

    /**
     * @param request
     * @param subgroupId
     * @param level
     * @param group
     * @param view
     * @param page
     * @return
     */
    private SearchResult navigateResults(HttpServletRequest request, String subgroupId, String level, String group, HtmlCatalogView view, int page) {
	SearchResult searchResult;
	String searchTerm;
	// NAVIGATE SEARCH RESULTS
	searchTerm = (String) request.getSession().getAttribute("searchTerm");
	searchResult = CatalogGroupSearcher.search(group, subgroupId, searchTerm);
	view.setResults(prepareResults(searchResult, page));
	if (StringUtils.isNotBlank(subgroupId)) {
	    int lvl = Integer.parseInt(level);
	    Node subGrpNode = CatalogTree.getNodeById(subgroupId);
	    view.setTree(prepareChildPath(view.getTree(), lvl, subGrpNode, searchResult));
	    view.setTree(loadAncestorPath(subGrpNode, view.getTree(), lvl, searchResult));
	    view.setTree("<a href=\"searchCatalog?level=0&group=" + subGrpNode.getGroupCode() + "\">" + subGrpNode.getGroup() + "</a>" + view.getTree());
	} else {
	    List<Node> nodesByGroupCode = CatalogTree.getNodesByGroupCode(group);
	    view.setTree(loadImmediateNodes(nodesByGroupCode, true, 2, searchResult, view.getTree()));
	}
	return searchResult;
    }

    private String navigationHtml(SearchResult result, int page) {
	if (result == null) {
	    return "";
	}
	int total = result.getResults().size();
	int pageCount = total / PAGE_SIZE;
	if (total % PAGE_SIZE != 0) {
	    pageCount++;
	}
	String html = "<p style=\"text-align:center;\">" + "Results: " + total + ", Pages:" + pageCount + "<br>";
	if (result.prevPage(page, PAGE_SIZE)) {
	    html = html + "<a href='searchCatalog?page=" + (page - 1) + "'>" + "prev</a>&nbsp;";
	} else {
	    html = html + "prev&nbsp;";
	}
	html = html + "<input type='text' name='page' size='3' value='" + page + "'> <input type='submit' value='go'>";
	if (result.nextPage(page, PAGE_SIZE)) {
	    html = html + "&nbsp;<a href='searchCatalog?page=" + (page + 1) + "'>" + "next</a>";
	}
	return html + "</p></form>";
    }

    /**
     * @param request
     * @param searchTerm
     * @param view
     * @param page
     * @return
     */
    private SearchResult performSearch(HttpServletRequest request, String searchTerm, HtmlCatalogView view, int page) {
	SearchResult searchResult;
	// TERM SEARCH
	request.getSession().setAttribute("searchTerm", searchTerm);
	searchResult = CatalogGroupSearcher.search(null, null, searchTerm);
	List<String> catalogSubgrpIds = searchResult.getCatalogSubgrpIds();
	HashSet<Node> nodes = new HashSet<Node>();
	for (String id : catalogSubgrpIds) {
	    if (id != null) {
		nodes.add(CatalogTree.getParent(id, 1));
	    }
	}
	ArrayList<Node> nodesList = new ArrayList<Node>();
	nodesList.addAll(nodes);

	view.setResults(prepareResults(searchResult, page));
	view.setTree(loadImmediateNodes(nodesList, true, 2, searchResult, view.getTree()));
	return searchResult;
    }

    /**
     * @param tree
     * @param lvl
     * @param subGrpNode
     * @param searchResult
     * @return
     */
    private String prepareChildPath(String treeVal, int lvl, Node subGrpNode, SearchResult searchResult) {
	String tree = treeVal;
	List<String> catalogSubgrpIds = searchResult.getCatalogSubgrpIds();
	HashSet<Node> nodes = new HashSet<Node>();
	for (String id : catalogSubgrpIds) {
	    if (id != null) {
		nodes.add(CatalogTree.getParent(id, lvl));
	    }
	}
	ArrayList<Node> nodesList = new ArrayList<Node>();
	nodesList.addAll(nodes);
	nodesList.remove(subGrpNode);
	tree = loadImmediateNodes(nodesList, false, lvl + 1, searchResult, tree);
	return tree;
    }

    /**
     * @param searchResult
     * @param subGrpNode
     * @return
     */
    private String prepareGrpCount(SearchResult searchResult, Node subGrpNode) {
	if (searchResult == null) {
	    return "";
	}
	String countHtml = " (" + searchResult.getGroupCount(subGrpNode.getGroupCode()) + ")";
	return countHtml;
    }

    /**
     * @param results
     * @param searchResult
     * @param pageNum
     *            TODO
     * @return
     */
    private String prepareResults(SearchResult searchResult, int pageNum) {
	List<ItemResult> page = searchResult.getPage(pageNum, PAGE_SIZE);
	String ids = "";
	for (int i = 0; i < page.size(); i++) {
	    ids = ids + String.valueOf(page.get(i).getItemId());
	    if (i < page.size() - 1) {
		ids = ids + ",";
	    }
	}
	HashMap<Integer, String> resultMap = new HashMap<Integer, String>(page.size());
	Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;
	try {
	    try {
		con = getDBConnection();
		stmt = con.createStatement();
		rs = stmt.executeQuery("select t.catalog_item_id, t.catalog_desc, t.catalog_prc, j.catalog_image_url "
			+ "from mm_catalog_item_t t left join mm_catalog_item_image_t i on i.catalog_item_id = t.catalog_item_id "
			+ " left join mm_catalog_image_t j on j.catalog_image_id = i.catalog_image_id where t.catalog_item_id in (" + ids + ")");
		while (rs.next()) {
		    resultMap.put(rs.getInt("catalog_item_id"),
			    "<p><img width='100' height='100' src='" + rs.getString("catalog_image_url") + "'>" + rs.getString("catalog_desc") + " [$" + rs.getString("catalog_prc") + "] " + "</p>");
		}
	    } finally {
		DatabaseConnection.release(rs, stmt, con);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	String results = "";
	for (int i = 0; i < page.size(); i++) {
	    int id = page.get(i).getItemId();
	    results = results + resultMap.get(id);
	}
	return results;
    }

    private String prepareSubGrpCount(SearchResult searchResult, Node subGrpNode) {
	if (searchResult == null) {
	    return "";
	}
	String countHtml = " (" + searchResult.getSubGroupCount(subGrpNode.getId()) + ")";
	return countHtml;
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
	StopWatch.start();
	SearchResult searchResult = null;
	CatalogTree.initialize();
	String subgroupId = request.getParameter("subgroupId");
	String searchTerm = request.getParameter("searchTerm");
	String level = request.getParameter("level");
	String group = request.getParameter("group");
	String pageStr = request.getParameter("page");
	String sort = request.getParameter("sort");
	HtmlCatalogView view = new HtmlCatalogView();

	int page = 1;
	if (StringUtils.isNotBlank(sort)) {
	    searchResult = sortResults(request, sort, view, page);

	} else if (StringUtils.isBlank(searchTerm) && StringUtils.isNotBlank(pageStr)) {
	    page = Integer.parseInt(pageStr);
	    searchResult = displayPage(request, view, page);

	} else if (StringUtils.isBlank(group) && StringUtils.isBlank(subgroupId) && StringUtils.isBlank(searchTerm)) {
	    displayHome(request, view);

	} else if (StringUtils.isNotBlank(searchTerm)) {
	    searchResult = performSearch(request, searchTerm, view, page);
	} else {
	    searchResult = navigateResults(request, subgroupId, level, group, view, page);
	}
	if (searchResult != null) {
	    searchResult.setMenuTree(view.getTree());
	    request.getSession().setAttribute("searchResult", searchResult);
	}
	String html = "<html><head>"
		+ styles
		+ "</head><body><table style=\"width:100%; border:none;\" cellpadding=\"0\" cellspacing=\"0\">"
		+ "<tr height=\"10%\"><td align=\"center\">"
		+ "<table style=\"width:100%;border-bottom:solid;border-width:1px;border-color:gray;\">"
		+ "<tr><td style=\"font-size:4em;\"><a href='./searchCatalog'>Bazaar</a></td></tr></table></td></tr><tr><td>"
		+ "<table style=\"width:100%;cellpadding:0;cellspacing:0;\" cellpadding=\"0\" cellspacing=\"0\">"
		+ "<tr><td style=\"width:20%;cellpadding:0;cellspacing:0;vertical-align:text-top; border-right:solid;border-width:1px;border-color:gray;\">"
		+ view.getTree()
		+ "<br/><br/><br/>"
		+ "</td><td style=\"cellpadding:0;cellspacing:0;vertical-align:text-top;\">"
		+ "<form><input type='text' name='searchTerm'><input type='submit' name='go' value='go'><br>"
		+ (searchResult != null ? "Sort by <a href='searchCatalog?sort=a'>Relevance</a>&nbsp;<a href='searchCatalog?sort=b'>Cost</a>&nbsp;<a href='searchCatalog?sort=c'>Best Selling</a><br/>"
			: "") + view.getResults() + "<br/><br/><br/>" + navigationHtml(searchResult, page) + "</td></tr></table></td></tr></table></body></html>";
	response.getOutputStream().write(html.getBytes());
	response.getOutputStream().flush();
	response.getOutputStream().close();
	StopWatch.stop();
	// HttpSessionDataLogger.logSession(request.getSession());
    }

    /**
     * @param request
     * @param sort
     * @param view
     * @param page
     * @return
     */
    private SearchResult sortResults(HttpServletRequest request, String sort, HtmlCatalogView view, int page) {
	SearchResult searchResult;
	searchResult = (SearchResult) request.getSession().getAttribute("searchResult");
	if (searchResult.getCurrentSort().equals(sort)) {
	    searchResult.reverseSort();
	} else {
	    if ("a".equals(sort)) {
		searchResult.sortByRelevance();
	    }
	    if ("b".equals(sort)) {
		searchResult.sortByCost();
	    }
	    if ("a".equals(sort)) {
		searchResult.sortByBestSeller();
	    }
	    searchResult.setCurrentSort(sort);
	}

	view.setResults(prepareResults(searchResult, page));
	view.setTree(searchResult.getMenuTree());
	return searchResult;
    }
}