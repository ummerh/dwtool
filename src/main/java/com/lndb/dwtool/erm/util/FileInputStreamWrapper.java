package com.lndb.dwtool.erm.util;

import java.io.File;
import java.io.InputStream;

public class FileInputStreamWrapper {
    private InputStream inputStream;
    private String fileName;
    private File file;

    public FileInputStreamWrapper(InputStream inputStream, File file, String fileName) {
	super();
	this.inputStream = inputStream;
	this.file = file;
	this.fileName = new File(fileName).getName();
    }

    public InputStream getInputStream() {
	return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
	this.inputStream = inputStream;
    }

    public String getFileName() {
	return fileName;
    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    /**
     * @return the file
     */
    public File getFile() {
	return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(File file) {
	this.file = file;
    }

}
