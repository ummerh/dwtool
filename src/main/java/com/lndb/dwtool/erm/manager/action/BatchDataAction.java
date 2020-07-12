package com.lndb.dwtool.erm.manager.action;

import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.db.StatementPool;
import com.lndb.dwtool.erm.db.data.CSVTextProcessor;
import com.lndb.dwtool.erm.db.data.ExcelProcessor;
import com.lndb.dwtool.erm.db.data.FileProcessor;
import com.lndb.dwtool.erm.db.data.FileStatistics;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.util.Configuration;

/**
 * 
 * @author harsha07
 * 
 */
public class BatchDataAction extends DataAction {
    private String resultFile;
    private String suffix;
    private List<FileStatistics> statList = new ArrayList<FileStatistics>();
    private String[] excludeList;
    private int processedCount = 0;
    /**
     * Data files filter
     */
    public static FileFilter dataFilter = new FileFilter() {
	public boolean accept(File pathname) {
	    String fileName = pathname.getName().toLowerCase();
	    return pathname.isFile() && ((fileName.endsWith(".xls") || fileName.endsWith(".csv") || fileName.endsWith(".txt")) && (!fileName.contains("good") && !fileName.contains("bad")));
	}

    };

    /**
     * Result data files filter
     */
    public static FileFilter resultDataFilter = new FileFilter() {
	public boolean accept(File pathname) {
	    String fileName = pathname.getName().toLowerCase();
	    return pathname.isFile() && ((fileName.endsWith(".xls") || fileName.endsWith(".csv") || fileName.endsWith(".txt")) && (fileName.contains("good") || fileName.contains("bad")));
	}

    };

    /**
     * 
     */
    private static final long serialVersionUID = 6799324062405233342L;

    /**
     * Displays batch files available
     * 
     * @return
     * @throws Exception
     */
    public String displayBatchFiles() throws Exception {
	clearResults();
	File filesDir = getBatchLoadDirectory();
	File[] dataFiles = null;
	dataFiles = getDataFiles(filesDir);
	if (dataFiles != null) {
	    for (File file : dataFiles) {
		FileStatistics stat = new FileStatistics();
		stat.setFile(file);
		statList.add(stat);
	    }
	}
	return SUCCESS;
    }

    /**
     * Download result data file
     * 
     * @return
     * @throws Exception
     */
    public String downloadResult() throws Exception {
	File file = new File(getBatchLoadDirectory(), prepareFileName(resultFile, suffix));
	downloadContent(file);
	return null;
    }

    /**
     * Validates batch files
     * 
     * @return
     * @throws Exception
     */
    public String processBatchFiles() throws Exception {
	clearResults();
	File filesDir = getBatchLoadDirectory();
	File[] dataFiles = null;
	dataFiles = getDataFiles(filesDir);
	OJBMap ojbMap = OJBMapCache.getOJBMap();
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
	FileProcessor processor = null;
	DBMap currMap = new DBMap(true);
	Map<String, FileStatistics> statMap = new HashMap<String, FileStatistics>();
	HashSet<String> tables = new HashSet<String>();
	if (dataFiles != null) {
	    prepareForProcessing(dataFiles, dbMap, currMap, statMap, tables);
	}
	Connection con = null;
	try {
	    ConnectionDetail connectionDetail = ConnectionDetail.configure(getConnectionName());
	    con = DatabaseConnection.newConnection(connectionDetail);
	    con.setAutoCommit(false);
	    if (dataFiles != null) {
		for (String tbl : currMap.defineOrder()) {
		    FileStatistics result = statMap.get(tbl);
		    if (result == null) {
			continue;
		    }
		    File file = result.getFile();
		    String name = file.getName();
		    File good = null;
		    File bad = null;
		    try {
			if (name.toLowerCase().endsWith(".xls")) {
			    processor = new ExcelProcessor();
			} else {
			    processor = new CSVTextProcessor();
			}
			good = new File(filesDir, prepareFileName(name, "good"));
			bad = new File(filesDir, prepareFileName(name, "bad"));

			processor.process(con, result, getConnectionName(), dbMap.getTableDescriptor(tbl), file, isHeaderIncluded(), joinMetaData, good, bad);
			result.setFileError("Done");
			if (result.getGoodCount() > 0) {
			    try {
				// this will ensure that data within excel sheet
				// are already in the db before performing the
				// validation of dependent files
				processor.performUncommitedUpdates(con, dbMap.getTableDescriptor(tbl), good);
			    } catch (Exception e) {
				// ignore the exception
			    }
			}
			if (result.getBadCount() == 0) {
			    bad.delete();
			}
			if (result.getGoodCount() == 0) {
			    good.delete();
			}
			this.processedCount++;
		    } catch (Exception e) {
			// do nothing
			result.setFileError(e.getMessage());
			// e.printStackTrace();
			System.out.println("Failed for " + name + " - " + e.getMessage());
			if (good != null) {
			    good.delete();
			}
			if (bad != null) {
			    bad.delete();
			}
		    }
		}
	    }
	} finally {
	    if (con != null) {
		StatementPool.release();
		con.rollback();
		DatabaseConnection.release(con);
	    }
	}
	return SUCCESS;
    }

    /**
     * 
     * @param dataFiles
     * @param dbMap
     * @param currMap
     * @param statMap
     * @param tables
     */
    private void prepareForProcessing(File[] dataFiles, DBMap dbMap, DBMap currMap, Map<String, FileStatistics> statMap, HashSet<String> tables) {
	for (File file : dataFiles) {
	    FileStatistics result = new FileStatistics();
	    statList.add(result);
	    result.setFile(file);
	    String name = file.getName();
	    String tblName = name.substring(0, name.lastIndexOf('.')).toUpperCase();
	    if (isExcluded(file)) {
		result.setFileError("excluded");
		result.setExcluded(true);
		continue;
	    }
	    if (dbMap.getTableDescriptor(tblName) == null) {
		result.setFileError("table not found");
		continue;
	    }
	    if (!tables.add(tblName)) {
		result.setFileError("duplicate for table");
		continue;
	    }
	    if (currMap.getTableDescriptor(tblName) == null && dbMap.getTableDescriptor(tblName) != null) {
		statMap.put(tblName, result);
	    }
	}
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    public String updateDbWithGood() throws Exception {
	File filesDir = getBatchLoadDirectory();
	File[] dataFiles = null;
	dataFiles = getDataFiles(filesDir);
	DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
	FileProcessor processor = null;
	DBMap currMap = new DBMap(true);
	Map<String, FileStatistics> statMap = new HashMap<String, FileStatistics>();
	HashSet<String> tables = new HashSet<String>();
	if (dataFiles != null) {
	    prepareForProcessing(dataFiles, dbMap, currMap, statMap, tables);
	}
	if (dataFiles != null) {
	    for (String tbl : currMap.defineOrder()) {
		FileStatistics result = statMap.get(tbl);
		if (result == null) {
		    continue;
		}
		File file = result.getFile();
		String name = file.getName();
		try {
		    if (name.toLowerCase().endsWith(".xls")) {
			processor = new ExcelProcessor();
		    } else {
			processor = new CSVTextProcessor();
		    }
		    File good = new File(filesDir, prepareFileName(name, "good"));
		    if (good.exists()) {
			processor.performDBUpdates(result, getConnectionName(), dbMap.getTableDescriptor(tbl), good);
			result.setFileError("Done [Updates-" + result.getUpdateRecordCount() + "] [Inserts-" + result.getInsertRecordCount() + "]");
		    }
		} catch (Exception e) {
		    // do nothing
		    result.setFileError(e.getMessage());
		    e.printStackTrace();
		}
	    }
	}
	clearResults();
	return SUCCESS;
    }

    /**
     * Remove all files with bad and good suffixes
     */
    private void clearResults() {
	File filesDir = getBatchLoadDirectory();
	if (filesDir != null && filesDir.exists()) {
	    File[] results = filesDir.listFiles(resultDataFilter);
	    for (File result : results) {
		if (!result.delete()) {
		    System.out.println("Delete failed " + result.getAbsolutePath());
		}
	    }
	}
    }

    /**
     * Checks if file is exclude from processing
     * 
     * @param file
     * @return
     */
    private boolean isExcluded(File file) {
	if (excludeList != null) {
	    for (String exclude : excludeList) {
		if (exclude.equalsIgnoreCase(file.getName())) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * 
     * @return
     */
    public File getBatchLoadDirectory() {
	File filesDir = new File(Configuration.getProperty("batch.data.files.dir") + getConnectionName());
	return filesDir;
    }

    /**
     * 
     * @param filesDir
     * @return
     */
    private File[] getDataFiles(File filesDir) {
	File[] dataFiles = null;
	if (filesDir.exists() && filesDir.isDirectory()) {
	    dataFiles = filesDir.listFiles(dataFilter);
	}
	return dataFiles;
    }

    public List<FileStatistics> getStatList() {
	return statList;
    }

    public void setStatList(List<FileStatistics> statList) {
	this.statList = statList;
    }

    public String[] getExcludeList() {
	return excludeList;
    }

    public void setExcludeList(String[] excludeList) {
	this.excludeList = excludeList;
    }

    public String getResultFile() {
	return resultFile;
    }

    public void setResultFile(String resultFile) {
	this.resultFile = resultFile;
    }

    public String getSuffix() {
	return suffix;
    }

    public void setSuffix(String suffix) {
	this.suffix = suffix;
    }

    public int getProcessedCount() {
	return processedCount;
    }

    public void setProcessedCount(int processedCount) {
	this.processedCount = processedCount;
    }
}
