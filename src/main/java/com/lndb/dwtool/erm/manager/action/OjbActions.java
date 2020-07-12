package com.lndb.dwtool.erm.manager.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;

import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.ojb.OJBMap.OjbXmlFilter;
import com.lndb.dwtool.erm.util.Configuration;
import com.lndb.dwtool.erm.util.FileFinder;
import com.lndb.dwtool.erm.util.FileInputStreamWrapper;
import com.lndb.dwtool.erm.util.FileUtil;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class that supports all OJB repository handling actions
 * 
 * @author harsha07
 * 
 */
public class OjbActions extends ActionSupport {
	private String ojbFileName;
	private static final long serialVersionUID = 3255791585620780538L;
	private File[] fileLists;
	private String xmlFile;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getXmlFile() {
		return xmlFile;
	}

	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}

	/**
	 * Action method displays all available list of OJB files
	 * 
	 * @return
	 * @throws Exception
	 */
	public String displayAllFiles() throws Exception {
		String ojbDir = Configuration.getProperty("ojbRepositoryDir");
		fileLists = new File(ojbDir).listFiles(new OJBMap.OjbXmlFilter());
		return SUCCESS;
	}

	public File[] getFileLists() {
		return fileLists;
	}

	public void setFileLists(File[] fileLists) {
		this.fileLists = fileLists;
	}

	/**
	 * Action method that supports deletion of an OJB XML file from the repository
	 * 
	 * @return
	 * @throws Exception
	 */
	public String deleteXMLFile() throws Exception {
		String ojbDir = Configuration.getProperty("ojbRepositoryDir");
		File fileToDelete = new File(ojbDir + ojbFileName);
		if (fileToDelete.isFile()) {
			if (fileToDelete.delete()) {
				setMessage("Delete - " + ojbFileName + " - SUCCESS");
			} else {
				setMessage("Delete - FAILED");
			}
		} else {
			setMessage("Delete - FAILED");
		}
		return displayAllFiles();
	}

	public String deleteAll() throws Exception {
		File ojbDir = new File(Configuration.getProperty("ojbRepositoryDir"));
		FileUtil.deleteContents(ojbDir);
		setMessage("DONE!");
		return displayAllFiles();
	}

	/**
	 * Action method that supports downloading of OJB XML file
	 * 
	 * @return
	 * @throws Exception
	 */
	public String downloadXMLFile() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/xml");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getOjbFileName() + "\"");
		String ojbDir = Configuration.getProperty("ojbRepositoryDir");
		PrintWriter out = response.getWriter();
		FileUtil.write(new FileReader(ojbDir + ojbFileName), out);
		return null;
	}

	/**
	 * Archives all the OJB XML files and downloads it to the browser
	 * 
	 * @return
	 * @throws Exception
	 */
	public String downloadAllXMLFiles() throws Exception {
		File ojbDir = new File(Configuration.getProperty("ojbRepositoryDir"));
		File[] ojbFiles = ojbDir.listFiles(new OJBMap.OjbXmlFilter());
		if (ojbFiles == null || ojbFiles.length == 0) {
			// no files to download
			setMessage("No files available for download");
			return SUCCESS;
		}
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/zip");
		response.setHeader("Content-disposition", "attachment; filename=\"all-ojb-xmls.zip\"");
		ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
		for (File file : ojbFiles) {
			ZipEntry entry = new ZipEntry(file.getName());
			zos.putNextEntry(entry);
			FileInputStream is = new FileInputStream(file);
			FileUtil.streamOut(is, zos, false);
			is.close();
			zos.closeEntry();
			zos.flush();
		}
		zos.finish();
		zos.close();
		return null;
	}

	/**
	 * Views the XML within the browser
	 * 
	 * @return
	 * @throws Exception
	 */
	public String viewXMLFile() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html");
		String ojbDir = Configuration.getProperty("ojbRepositoryDir");
		PrintWriter out = response.getWriter();
		out.print("<html><title>" + ojbFileName + "</title><body><p><pre>");
		FileUtil.htmlStream(new FileReader(ojbDir + ojbFileName), out);
		out.print("</pre></p></body></html>");
		out.flush();
		return null;
	}

	/**
	 * Action method that supports uploading of OJB XML file/ Archive file containing OJB files
	 * 
	 * @return
	 * @throws Exception
	 */
	public String uploadXMLFile() throws Exception {
		String ojbDir = Configuration.getProperty("ojbRepositoryDir");
		if (!new File(ojbDir).exists()) {
			new File(ojbDir).mkdirs();
		}
		MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
		if (multiWrapper.getErrors() == null || multiWrapper.getErrors().isEmpty()) {
			String[] fileNames = multiWrapper.getFileNames("xmlFile");
			File[] files = multiWrapper.getFiles("xmlFile");

			if (files != null && files.length == 1 && fileNames != null && fileNames.length == 1) {
				handleXmlFile(ojbDir, fileNames, files);
				handleArchiveFile(ojbDir, fileNames, files);
			}
		} else {
			setMessage("Upload FAILED. ");
		}
		OJBMapCache.refreshMap();
		return displayAllFiles();
	}

	public String refreshMap() throws Exception {
		OJBMapCache.refreshMap();
		return displayAllFiles();
	}

	/**
	 * private method that handle archive files containing OJB definition files
	 * 
	 * @param ojbDir
	 * @param fileNames
	 * @param files
	 * @throws IOException
	 */
	private void handleArchiveFile(String ojbDir, String[] fileNames, File[] files) throws IOException {
		StringBuilder msg = new StringBuilder("Files extracted are ");
		if (fileNames[0].endsWith(".jar") || fileNames[0].endsWith(".zip")) {
			List<FileInputStreamWrapper> filesFromJar = FileFinder.getInstance().findFromJarFile(files[0], new OJBMap.OjbXmlFilter());
			if (!filesFromJar.isEmpty()) {
				for (FileInputStreamWrapper fileInputStreamWrapper : filesFromJar) {
					FileUtil.writeOut(fileInputStreamWrapper.getInputStream(), new File(ojbDir + fileInputStreamWrapper.getFileName()));
					msg.append(fileInputStreamWrapper.getFileName() + ", ");
				}
				setMessage(msg.toString());
			} else {
				setMessage("No files extracted out of this archive file");
			}
		}
	}

	/**
	 * Private method that supports individual OJB XML files
	 * 
	 * @param ojbDir
	 * @param fileNames
	 * @param files
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void handleXmlFile(String ojbDir, String[] fileNames, File[] files) throws IOException, FileNotFoundException {
		String fileName = fileNames[0];
		if (fileName.toLowerCase().matches(OjbXmlFilter.OJB_FILE_NAME_PATTERN)) {
			FileUtil.writeOut(new FileInputStream(files[0]), new File(ojbDir + fileName));
			setMessage("File " + fileName + " was successfully uploaded.");
		} else {
			setMessage("File was not saved, didn't meet the criteria.");
		}
	}

	public String getOjbFileName() {
		return ojbFileName;
	}

	public void setOjbFileName(String ojbFileName) {
		this.ojbFileName = ojbFileName;
	}
}
