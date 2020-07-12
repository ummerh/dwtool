package com.lndb.dwtool.code.search;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class SourceCodeSearcher {
	public static void main(String[] args) {
		try {
			IndexSearcher searcher = new IndexSearcher(IndexReader.open(new SimpleFSDirectory(new File("/TEMP/lucene/sourcecode"))));
			Set<String> paths = performRawSearch("content:\"Administer Batch File\"", searcher, new String[] { "content" });
			for (String string : paths) {
				System.out.println(string);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Set<String> performRawSearch(String searchTerm, Searcher searcher, String[] searchColumns) throws Exception {
		HashSet<String> paths = new HashSet<String>();
		if (StringUtils.isBlank(searchTerm)) {
			return paths;
		}
		try {
			if (StringUtils.isBlank(searchTerm)) {
				return paths;
			}
			Query query = new QueryParser(Version.LUCENE_30, "", new StandardAnalyzer(Version.LUCENE_30)).parse(searchTerm);
			TopDocs results = searcher.search(query, searcher.maxDoc());
			if (results.totalHits == 0) {
				return paths;
			}
			for (int i = 0; i < results.totalHits; i++) {
				Document doc = searcher.doc(results.scoreDocs[i].doc);
				paths.add(doc.get("path"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paths;
	}
}
