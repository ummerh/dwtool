package com.lndb.dwtool.code.search;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class SourceCodeIndexer {
	public static void main(String[] args) {
		try {
			String srcLocation = "C:\\java\\git-repos\\kfs\\work";
			File indexDir = new File("/TEMP/lucene/sourcecode");
			indexDir.mkdirs();
			IndexWriter writer = new IndexWriter(new SimpleFSDirectory(indexDir), new StandardAnalyzer(Version.LUCENE_30), true, MaxFieldLength.UNLIMITED);
			Collection<File> listFiles = FileUtils.listFiles(new File(srcLocation), new String[] { "java", "xml", "properties", "tag", "jsp" }, true);
			for (File file : listFiles) {
				SourceCodeFile srcFile = new SourceCodeFile();
				srcFile.load(file);
				writer.addDocument(srcFile.toDocument());
				System.out.println("Indexed... " + file.getAbsolutePath());
			}
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
