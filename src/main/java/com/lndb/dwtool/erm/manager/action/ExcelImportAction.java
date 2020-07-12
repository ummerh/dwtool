package com.lndb.dwtool.erm.manager.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;

import com.lndb.dwtool.code.diff.FileUtil;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.dml.ConverterUtil;
import com.lndb.dwtool.erm.dml.DMLConverterFactory;
import com.lndb.dwtool.erm.dml.DMLConverterInterface;
import com.lndb.dwtool.erm.dml.SimpleConverter;
import com.lndb.dwtool.erm.util.Configuration;
import com.lndb.dwtool.erm.util.ConnectionRepository;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class imports Microsoft Excel files, translates and generates DML for
 * target DB
 * 
 * @author ZHANGMA
 * 
 */
public class ExcelImportAction extends ActionSupport {
	private static final long serialVersionUID = 306381603106424144L;
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(ExcelImportAction.class);
	private File upload;
	private String uploadFileName;
	private String uploadContentType;

	private String connectionName;
	private List<ConnectionDetail> connections = new ArrayList<ConnectionDetail>();
	private String strategyName;

	private static final List<String> strategyList = new ArrayList<String>();
	static {
		strategyList.add(DMLConverterInterface.ONE_ON_ONE);
		strategyList.add(DMLConverterInterface.ROLES);
		strategyList.add(DMLConverterInterface.PERMISSIONS);
		strategyList.add(DMLConverterInterface.RESPONSIBILITIES);
		strategyList.add(DMLConverterInterface.ROLE_PERMISSIONS);
		strategyList.add(DMLConverterInterface.ROLE_RESPONSIBILITIES);
	}

	public String display() {
		return INPUT;
	}

	
	/**
	 * @return the upload
	 */
	public File getUpload() {
		return upload;
	}


	/**
	 * @param upload the upload to set
	 */
	public void setUpload(File upload) {
		this.upload = upload;
	}


	/**
	 * @return the uploadFileName
	 */
	public String getUploadFileName() {
		return uploadFileName;
	}


	/**
	 * @param uploadFileName the uploadFileName to set
	 */
	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}


	/**
	 * @return the uploadContentType
	 */
	public String getUploadContentType() {
		return uploadContentType;
	}


	/**
	 * @param uploadContentType the uploadContentType to set
	 */
	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}


	public String upload() throws Exception {
		
		String outputDir = Configuration.getProperty("dml.output.dir");

		cleanupDir(outputDir);
		
		try {
			LOG.info("*** " + upload + "\t" + upload.length());
	
			DBMap targetDb = DBMapCache.getDBMap(getConnectionName());
			BufferedWriter report = ConverterUtil.getReportOutputFileStream(outputDir, getUploadFileName());
			BufferedWriter errorReport = ConverterUtil.getErrorOutputFileStream(outputDir, getUploadFileName());
	
			DMLConverterInterface converter = DMLConverterFactory
						.getConverter(getUploadFileName());
	
			boolean hasError = converter.convertToSQL(new FileInputStream(upload), targetDb,
						outputDir, report, errorReport);
	
			report.close();
			errorReport.close();
			
			if (!hasError) {
				// remove error report file
				File errorFile = new File(ConverterUtil.getErrorFileName(outputDir, getUploadFileName()));
				if (errorFile.delete()) {
					System.out.println("Deleted empty error report" + errorFile.getPath());
				}
			}
			
			createZipFileForOut(outputDir);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("error", e);
		}
		finally {
			ServletActionContext.getResponse().getOutputStream().flush();
			ServletActionContext.getResponse().getOutputStream().close();
		}
		LOG.info("load successfully");
		return null;
	}

	private void cleanupDir(String outputDir) {
		if (!new File(outputDir).exists()) {
			new File(outputDir).mkdirs();
		}
		
		File od = new File(outputDir);
		FileUtil.deleteContents(od);
	}

	private void createZipFileForOut(String outputDir) throws IOException, FileNotFoundException {
		File outputFileDir = new File(outputDir);

		File[] dmlFiles = outputFileDir.listFiles();
		if (dmlFiles == null || dmlFiles.length == 0) {
			return;
		}

		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/zip");
		response.setHeader("Content-disposition", "attachment; filename=\"" + StringUtils.substringBefore(getUploadFileName(), ".") + "_dml.zip\"");

		ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());

		for (File file : dmlFiles) {
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
	}

	/**
	 * @return the connectionName
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/**
	 * @param connectionName
	 *            the connectionName to set
	 */
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	private void prepareConnectionList() {
		this.connections.clear();
		List<ConnectionDetail> allConnections = ConnectionRepository
				.getAllConnections();
		ConnectionRepository.validateConnections(allConnections);
		for (ConnectionDetail connectionDetail : allConnections) {
			if (connectionDetail.isValid()) {
				this.connections.add(connectionDetail);
			}
		}
	}

	/**
	 * @return the connections
	 */
	public List<ConnectionDetail> getConnections() {
		prepareConnectionList();
		return connections;
	}

	/**
	 * @param connections
	 *            the connections to set
	 */
	public void setConnections(List<ConnectionDetail> connections) {
		this.connections = connections;
	}

	/**
	 * @return the strategyList
	 */
	public List<String> getStrategyList() {
		return strategyList;
	}

	/**
	 * @return the strategyName
	 */
	public String getStrategyName() {
		return strategyName;
	}

	/**
	 * @param strategyName
	 *            the strategyName to set
	 */
	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

}
