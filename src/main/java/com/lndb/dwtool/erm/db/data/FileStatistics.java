package com.lndb.dwtool.erm.db.data;

import java.io.File;

public class FileStatistics {
    private File file;
    private String[] refheaders;
    private String fileError;
    private boolean importable = true;
    private int updateRecordCount;
    private int insertRecordCount;
    private int totalRecordCount;
    private int badCount;
    private int goodCount;
    private boolean excluded;

    public void setBadCount(int badCount) {
	this.badCount = badCount;
    }

    public void setGoodCount(int goodCount) {
	this.goodCount = goodCount;
    }

    public String getFileError() {
	return fileError;
    }

    public void setFileError(String fileError) {
	this.fileError = fileError;
    }

    public void incrementBadLineCount() {
	this.badCount++;
    }

    public void incrementGoodLineCount() {
	this.goodCount++;
    }

    public boolean isImportable() {
	return importable;
    }

    public void setImportable(boolean importable) {
	this.importable = importable;
    }

    public String[] getRefheaders() {
	return refheaders;
    }

    public void setRefheaders(String[] refheaders) {
	this.refheaders = refheaders;
    }

    public String headerLine() {
	if (this.refheaders == null) {
	    return null;
	}
	StringBuilder line = new StringBuilder();
	for (int i = 0; i < refheaders.length; i++) {
	    line.append(refheaders[i]);
	    if (i < refheaders.length - 1) {
		line.append(",");
	    }
	}
	return line.toString();
    }

    public int getUpdateRecordCount() {
	return updateRecordCount;
    }

    public void setUpdateRecordCount(int updateRecordCount) {
	this.updateRecordCount = updateRecordCount;
    }

    public int getInsertRecordCount() {
	return insertRecordCount;
    }

    public void setInsertRecordCount(int insertRecordCount) {
	this.insertRecordCount = insertRecordCount;
    }

    public int getTotalRecordCount() {
	return totalRecordCount;
    }

    public void setTotalRecordCount(int totalRecordCount) {
	this.totalRecordCount = totalRecordCount;
    }

    public int incrementUpdateRecordCount(int count) {
	this.updateRecordCount += count;
	this.totalRecordCount += count;
	return this.updateRecordCount;
    }

    public int incrementInsertRecordCount(int count) {
	this.insertRecordCount += count;
	this.totalRecordCount += count;
	return this.insertRecordCount;
    }

    public int getBadCount() {
	return this.badCount;
    }

    public int getGoodCount() {
	return this.goodCount;
    }

    public File getFile() {
	return file;
    }

    public void setFile(File file) {
	this.file = file;
    }

    public boolean isExcluded() {
	return excluded;
    }

    public void setExcluded(boolean excluded) {
	this.excluded = excluded;
    }
}
