/**
 * 
 */
package demo.lucene.catalog.search;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Connections;
import com.lndb.dwtool.erm.util.StopWatch;

public class CatalogGroupIndexerApp {
    public static void main(String[] args) {
	try {
	    performIndex();
	} catch (Exception e) {
	    e.printStackTrace();
	}
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
     * @param conn
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     * @throws Exception
     */
    static void performIndex() throws CorruptIndexException, LockObtainFailedException, IOException, Exception {
	StopWatch.start();
	StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
	IndexWriter writer = new IndexWriter(new SimpleFSDirectory(new File("/temp/lucene")), analyzer, true, MaxFieldLength.UNLIMITED);
	System.out.println("Indexing to directory '" + "/temp/lucene" + "'...");
	indexDocs(writer);
	writer.optimize();
	writer.close();
	System.out.println("Indexing finished...");
	StopWatch.stop();
    }

    static void indexDocs(IndexWriter writer) throws Exception {
	StopWatch.start();
	String sql = "select catalog_item_id, distributor_nbr, catalog_desc, catalog_prc, catalog_subgroup_id, order_count from mm_catalog_item_search_t";
	Connection conn = getDBConnection();
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery(sql);
	int count = 0;
	try {
	    while (rs.next()) {
		Document d = new Document();
		d.add(new Field("catalog_item_id", "" + rs.getString("catalog_item_id"), Field.Store.YES, Field.Index.NO));
		d.add(new Field("catalog_prc", "" + rs.getString("catalog_prc"), Field.Store.YES, Field.Index.NO));
		d.add(new Field("order_count", "" + rs.getString("order_count"), Field.Store.YES, Field.Index.NO));
		d.add(new Field("catalog_subgroup_id", "" + rs.getString("catalog_subgroup_id"), Field.Store.YES, Field.Index.NOT_ANALYZED));
		// Indexed fields below
		d.add(new Field("distributor_nbr", "" + rs.getString("distributor_nbr"), Field.Store.NO, Field.Index.ANALYZED));
		d.add(new Field("catalog_desc", rs.getString("catalog_desc"), Field.Store.NO, Field.Index.ANALYZED));
		writer.addDocument(d);
		count++;
	    }
	} finally {
	    DatabaseConnection.release(rs, stmt, conn);
	}

	System.out.println("Indexed " + count + " records");
	StopWatch.stop();
    }
}
