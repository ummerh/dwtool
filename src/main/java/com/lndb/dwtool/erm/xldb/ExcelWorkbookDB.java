package com.lndb.dwtool.erm.xldb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Configuration;
import com.lndb.dwtool.erm.util.Connections;
import com.lndb.dwtool.erm.util.FileUtil;

public class ExcelWorkbookDB {
	private static final HashMap<String, List<ExcelTable>> DB_MAP = new HashMap<String, List<ExcelTable>>();

	public static class WorkerThread extends Thread {
		@Override
		public void run() {
			String excelDir = Configuration.getProperty("excel.repository");
			if (new File(excelDir, "hsqldb").exists()) {
				// cleanup db before start
				FileUtil.deleteContents(new File(excelDir, "hsqldb"));
			}
			loadAll();
		}
	}

	private List<ExcelTable> excelTables = new ArrayList<ExcelTable>();

	public static void loadAll() {
		try {
			Connection con = null;
			Statement stmt = null;
			try {
				con = Connections.MEMORYDB.newConnection();
				stmt = con.createStatement();
				stmt.execute("drop schema public cascade");
			} catch (Exception e) {
				System.err.println("Could not drop memory db schema");
				e.printStackTrace();
			} finally {
				DatabaseConnection.release(stmt, con);
			}

			File[] files = listExcelFiles();
			for (File file : files) {
				ExcelWorkbookDB workbookDB = new ExcelWorkbookDB();
				workbookDB.load(file);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static String getExcelDir() {
		String excelDir = Configuration.getProperty("excel.repository");
		if (!new File(excelDir).exists()) {
			new File(excelDir).mkdirs();
		}
		return excelDir;
	}

	public static File[] listExcelFiles() {
		String excelDir = getExcelDir();
		File[] list = new File(excelDir).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xls");
			}
		});
		return list;
	}

	public void load(String filePath) throws FileNotFoundException, IOException {
		load(new File(filePath));
	}

	public void load(File file) throws FileNotFoundException, IOException {
		Workbook wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(file)));
		int pos = 0;
		while (pos < wb.getNumberOfSheets() && wb.getSheetAt(pos) != null) {
			ExcelTable sheetTable = new ExcelTable(file.getAbsolutePath(), wb, pos);
			sheetTable.load();
			if (!sheetTable.isError()) {
				List<ExcelTable> tables = DB_MAP.get(file.getName().toLowerCase());
				if (tables == null) {
					tables = new ArrayList<ExcelTable>();
					DB_MAP.put(file.getName().toLowerCase(), tables);
				}
				tables.add(sheetTable);
			}
			pos++;
		}
	}

	public void drop(File file) throws FileNotFoundException, IOException {
		Workbook wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(file)));
		int pos = 0;
		while (pos < wb.getNumberOfSheets() && wb.getSheetAt(pos) != null) {
			ExcelTable sheetTable = new ExcelTable(file.getAbsolutePath(), wb, pos);
			sheetTable.drop();
			pos++;
		}
		DB_MAP.remove(file.getName().toLowerCase());
	}

	public static void main(String[] args) {
		try {

			String path1 = "/java/projects/kuali-erd-web/external/sharepoint/dtt.xls";
			ExcelWorkbookDB excelWorkbookDB1 = new ExcelWorkbookDB();
			excelWorkbookDB1.load(path1);

			String path2 = "/Users/harsha07/Downloads/camille_list.xls";

			ExcelWorkbookDB excelWorkbookDB2 = new ExcelWorkbookDB();
			excelWorkbookDB2.load(path2);

			Connection con = Connections.MEMORYDB.newConnection();
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("select t1.a,  t2.BM from camille_list_s1 t1 join dtt_s1 t2 on t1.a= t2.a ");
			ResultSetMetaData metaData = null;
			if (rs != null) {
				metaData = rs.getMetaData();
			}
			while (rs.next()) {
				for (int k = 1; k <= metaData.getColumnCount(); k++) {
					System.out.print(metaData.getColumnName(k) + " = " + rs.getString(k) + ",");
				}
				System.out.println();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<ExcelTable> getExcelTables() {
		return excelTables;
	}

	public void setExcelTables(List<ExcelTable> excelTables) {
		this.excelTables = excelTables;
	}

	public static Map<String, List<ExcelTable>> getDBMap() {
		return Collections.unmodifiableMap(DB_MAP);
	}
}
