package demo.docsearch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import com.lndb.dwtool.erm.util.LuceneUtil;
import com.lndb.dwtool.erm.util.StopWatch;

public class IndexCommandLine {

	public static final String TEMP_GL_LUCENE = "/temp/gl/findev/lucene";
	public static final String TEMP_DOC_LUCENE_TEXT = "/temp/doc/lucene/text";
	public static final String TEMP_DOC_LUCENE_DATE = "/temp/doc/lucene/date";
	public static final String TEMP_DOC_LUCENE_FLOAT = "/temp/doc/lucene/float";
	public static final String TEMP_DOC_LUCENE_LONG = "/temp/doc/lucene/long";
	private static IndexSearcher glSearchIndexer;

	public static void main(String[] args) {
		try {
			// searchGL();
			searchText();
			searchFloat();
			searchLong();
			searchDate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static HashSet<String> searchGL() throws Exception {
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
		HashSet<String> records = new HashSet<String>();
		LuceneIndexNodeManager indexNodeManager = new LuceneIndexNodeManager(TEMP_GL_LUCENE);
		if ((indexNodeManager.isReady() && glSearchIndexer == null) || indexNodeManager.isReload()) {
			glSearchIndexer = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(TEMP_GL_LUCENE))));
			indexNodeManager.finishReloading();
			System.out.println("Index reloaded");
		}

		if (!indexNodeManager.isReady()) {
			System.out.println("Index not ready");
			return records;
		}

		BooleanQuery blq = new BooleanQuery();

		long createDate = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse("12/13/2010 00:00:00").getTime();
		NumericRangeQuery<Long> q1 = NumericRangeQuery.newLongRange("crte_dt", createDate, null, true, false);
		Query q2 = new QueryParser(Version.LUCENE_30, "ACCOUNT_NBR", analyzer).parse(LuceneUtil.prepareSearchTerm("GU015363"));
		Query q3 = new QueryParser(Version.LUCENE_30, "FIN_COA_CD", analyzer).parse(LuceneUtil.prepareSearchTerm("MS"));
		Query q4 = new QueryParser(Version.LUCENE_30, "FDOC_TYP_CD", analyzer).parse(LuceneUtil.prepareSearchTerm("PO"));

		// blq.add(q1, BooleanClause.Occur.MUST);
		blq.add(q2, BooleanClause.Occur.MUST);
		blq.add(q3, BooleanClause.Occur.MUST);
		blq.add(q4, BooleanClause.Occur.MUST);

		StopWatch.start();
		TopDocs results = glSearchIndexer.search(blq, glSearchIndexer.maxDoc());
		StopWatch.stop();
		for (int i = 0; i < results.totalHits; i++) {
			Document doc = glSearchIndexer.doc(results.scoreDocs[i].doc);
			records.add(doc.get("key"));
		}
		for (String id : records) {
			// System.out.println("REC - " + id);
		}
		System.out.println("Total Hits: " + records.size());
		return records;
	}

	private static HashSet<String> searchText() throws Exception {
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(TEMP_DOC_LUCENE_TEXT))));
		BooleanQuery blq = new BooleanQuery();

		long createDate = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse("12/13/2010 00:00:00").getTime();
		NumericRangeQuery<Long> q1 = NumericRangeQuery.newLongRange("crte_dt", createDate, null, true, false);
		Query q2 = new QueryParser(Version.LUCENE_30, "accountNumber", analyzer).parse(LuceneUtil.prepareSearchTerm("GU015363"));
		Query q3 = new QueryParser(Version.LUCENE_30, "chartOfAccountsCode", analyzer).parse(LuceneUtil.prepareSearchTerm("MS"));
		Query q4 = new QueryParser(Version.LUCENE_30, "doc_typ_nm", analyzer).parse(LuceneUtil.prepareSearchTerm("PO"));

		// blq.add(q1, BooleanClause.Occur.MUST);
		blq.add(q2, BooleanClause.Occur.MUST);
		blq.add(q3, BooleanClause.Occur.MUST);
		blq.add(q4, BooleanClause.Occur.MUST);

		HashSet<String> records = new HashSet<String>();
		StopWatch.start();
		TopDocs results = indexSearcher.search(blq, indexSearcher.maxDoc());
		StopWatch.stop();
		for (int i = 0; i < results.totalHits; i++) {
			Document doc = indexSearcher.doc(results.scoreDocs[i].doc);
			records.add(doc.get("doc_hdr_id"));
		}
		for (String id : records) {
			// System.out.println("REC - " + id);
		}
		System.out.println("Total Hits: " + records.size());
		return records;
	}

	private static HashSet<String> searchFloat() throws Exception {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(TEMP_DOC_LUCENE_FLOAT))));
		BooleanQuery blq = new BooleanQuery();

		NumericRangeQuery<Float> q1 = NumericRangeQuery.newFloatRange("financialDocumentTotalAmount", new Float(4534.50d), null, true, false);
		blq.add(q1, BooleanClause.Occur.MUST);

		HashSet<String> records = new HashSet<String>();
		StopWatch.start();
		TopDocs results = indexSearcher.search(blq, indexSearcher.maxDoc());
		StopWatch.stop();
		for (int i = 0; i < results.totalHits; i++) {
			Document doc = indexSearcher.doc(results.scoreDocs[i].doc);
			records.add(doc.get("doc_hdr_id"));
		}
		for (String id : records) {
			System.out.println("REC - " + id);
		}
		System.out.println("Total Hits: " + records.size());
		return records;
	}

	private static HashSet<String> searchLong() throws Exception {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(TEMP_DOC_LUCENE_LONG))));
		BooleanQuery blq = new BooleanQuery();

		NumericRangeQuery<Long> q1 = NumericRangeQuery.newLongRange("purapDocumentIdentifier", new Long(108571), new Long(108571), true, true);
		blq.add(q1, BooleanClause.Occur.MUST);

		HashSet<String> records = new HashSet<String>();
		StopWatch.start();
		TopDocs results = indexSearcher.search(blq, indexSearcher.maxDoc());
		StopWatch.stop();
		for (int i = 0; i < results.totalHits; i++) {
			Document doc = indexSearcher.doc(results.scoreDocs[i].doc);
			records.add(doc.get("doc_hdr_id"));
		}
		for (String id : records) {
			System.out.println("REC - " + id);
		}
		System.out.println("Total Hits: " + records.size());
		return records;
	}

	private static HashSet<String> searchDate() throws Exception {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File(TEMP_DOC_LUCENE_DATE))));
		BooleanQuery blq = new BooleanQuery();

		long dtVal = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse("11/27/2012 00:00:00").getTime();

		NumericRangeQuery<Long> q1 = NumericRangeQuery.newLongRange("purchaseOrderLastTransmitTimestamp", new Long(dtVal), new Long(dtVal), true, true);
		blq.add(q1, BooleanClause.Occur.MUST);

		HashSet<String> records = new HashSet<String>();
		StopWatch.start();
		TopDocs results = indexSearcher.search(blq, indexSearcher.maxDoc());
		StopWatch.stop();
		for (int i = 0; i < results.totalHits; i++) {
			Document doc = indexSearcher.doc(results.scoreDocs[i].doc);
			records.add(doc.get("doc_hdr_id"));
		}
		for (String id : records) {
			System.out.println("REC - " + id);
		}
		System.out.println("Total Hits: " + records.size());
		return records;
	}
}
