/**
 * 
 */
package demo.lucene.catalog.search;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import com.lndb.dwtool.erm.util.StopWatch;

import demo.lucene.catalog.search.SearchResult.ItemResult;

public class CatalogGroupSearcher {
    private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
    private static Searcher searcher;
    static {
	try {
	    searcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File("/temp/lucene"))));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static SearchResult search(String groupCode, String subGroupId, String searchTerm) {
	SearchResult snapshot = new SearchResult();
	try {
	    if (StringUtils.isBlank(groupCode) && StringUtils.isBlank(subGroupId)) {
		snapshot = performSearch(null, searchTerm);
	    } else if (StringUtils.isNotBlank(subGroupId)) {
		Node node = CatalogTree.getNodeById(subGroupId);
		List<Node> ancestors = node.getDescendants();
		String ids = subGroupId;
		for (Node node2 : ancestors) {
		    ids = ids + "," + node2.getId();
		}
		snapshot = performSearch(ids, searchTerm);
	    } else if (StringUtils.isNotBlank(groupCode)) {
		String ids = subGroupId;
		List<Node> grpNodes = CatalogTree.getNodesByGroupCode(groupCode);
		for (Node node : grpNodes) {
		    ids = ids + "," + node.getId();
		}
		snapshot = performSearch(ids, searchTerm);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	return snapshot;
    }

    private static SearchResult performSearch(String subgroupIds, String searchedTerm) {
	String searchTerm = prepareSearchTerm(searchedTerm);
	System.out.println("Searching [" + subgroupIds + "," + searchTerm + "]");
	SearchResult snapshot = new SearchResult();
	List<ItemResult> items = snapshot.getResults();
	try {

	    BooleanQuery blq = new BooleanQuery();
	    if (searchTerm != null && searchTerm.trim().length() > 0) {
		Query query = new MultiFieldQueryParser(Version.LUCENE_30, new String[] { "catalog_desc", "distributor_nbr" }, analyzer).parse(searchTerm);
		blq.add(query, BooleanClause.Occur.MUST);
	    }
	    if (subgroupIds != null) {
		String[] ids = subgroupIds.split(",");
		BooleanQuery blq2 = new BooleanQuery();
		for (String id : ids) {
		    Query preQry = new TermQuery(new Term("catalog_subgroup_id", id));
		    blq2.add(preQry, BooleanClause.Occur.SHOULD);
		}
		blq.add(blq2, BooleanClause.Occur.MUST);

	    }
	    StopWatch.start();
	    TopDocs results = searcher.search(blq, searcher.maxDoc());
	    StopWatch.stop();
	    System.out.println("Results found [" + results.totalHits + "]");
	    if (results.totalHits == 0) {
		return snapshot;
	    }
	    HashSet<String> catalogSubgrpIds = new HashSet<String>();
	    HashSet<String> catalogItemIds = new HashSet<String>();

	    for (int i = 0; i < results.totalHits; i++) {
		Document doc = searcher.doc(results.scoreDocs[i].doc);
		String catalogSubgrpId = doc.get("catalog_subgroup_id");
		String catalogItemId = doc.get("catalog_item_id");
		if (catalogSubgrpId != null && !"null".equals(catalogSubgrpId)) {
		    catalogSubgrpIds.add(catalogSubgrpId);
		    snapshot.addToSubGroup(catalogSubgrpId, catalogItemId);
		    Node subGrpNode = CatalogTree.getNodeById(catalogSubgrpId);
		    snapshot.addToGroup(subGrpNode.getGroupCode(), catalogItemId);
		    List<Node> ancestors = subGrpNode.getAncestors();
		    for (Node node : ancestors) {
			snapshot.addToGroup(node.getGroupCode(), catalogItemId);
			snapshot.addToSubGroup(node.getId(), catalogItemId);
		    }
		}
		if (catalogItemIds.add(catalogItemId)) {
		    String orderCount = doc.get("order_count");
		    String price = doc.get("catalog_prc");
		    items.add(new ItemResult(Integer.parseInt(catalogItemId), i, Integer.parseInt(orderCount == null ? "0" : orderCount), Double.parseDouble(price == null ? "0" : price)));
		}
	    }
	    snapshot.getCatalogSubgrpIds().addAll(catalogSubgrpIds);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return snapshot;
    }

    /**
     * @param searchTerm
     */
    private static String prepareSearchTerm(String searchTerm) {
	if (searchTerm == null) {
	    return "";
	}
	String cleanedTerm = handleSpecialChars(searchTerm);
	String[] terms = cleanedTerm.split(" ");
	String formtted = "";
	for (String term : terms) {
	    if (term == null || term.trim().length() == 0 || StopAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase())) {
		continue;
	    }
	    if (formtted.equals("")) {
		formtted = "+" + term + "* ";
	    } else {
		formtted = formtted + " +" + term + "*";
	    }
	}
	return formtted.trim();
    }

    /**
     * @param searchTerm
     * @return
     */
    protected static String handleSpecialChars(String searchTerm) {
	int pos = 0;
	char[] tmp = new char[searchTerm.length() * 2];
	char[] chars = searchTerm.toCharArray();
	for (char c : chars) {
	    if (c == '.' || c == ',' || c == '/' || c == '`' || c == '@' || c == '#' || c == '$' || c == '%' || c == '_' || c == '=' || c == ';' || c == '<' || c == '>') {
		// ignore
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
}
