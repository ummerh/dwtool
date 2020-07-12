package com.lndb.dwtool.erm.manager.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;

import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.db.data.CSVTextProcessor;
import com.lndb.dwtool.erm.db.data.ExcelProcessor;
import com.lndb.dwtool.erm.db.data.FileProcessor;
import com.lndb.dwtool.erm.db.data.FileStatistics;
import com.lndb.dwtool.erm.manager.web.SessionRepository;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.util.FileUtil;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class supports all file import actions supported against a database
 * 
 * @author harsha07
 * 
 */
public class DataAction extends ActionSupport {
	/**
	 * Serial Id
	 */
	private static final long serialVersionUID = 4111597780793244418L;
	private String tableName;
	private boolean headerIncluded = true;
	private String inputFileName;
	private String message;
	private String connectionName;
	private boolean badRecords;
	private boolean goodRecords;
	private boolean dbUpdated;

	private FileStatistics statistics;

	public boolean isBadRecords() {
		return badRecords;
	}

	public void setBadRecords(boolean badRecords) {
		this.badRecords = badRecords;
	}

	public boolean isGoodRecords() {
		return goodRecords;
	}

	public void setGoodRecords(boolean goodRecords) {
		this.goodRecords = goodRecords;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean isHeaderIncluded() {
		return headerIncluded;
	}

	public void setHeaderIncluded(boolean headerIncluded) {
		this.headerIncluded = headerIncluded;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String dataFile) {
		this.inputFileName = dataFile;
	}

	/**
	 * Private method which handles the import file <li>Check if file name matches expected</li> <li>Create DB map</li> <li>Create OJB map</li> <li>
	 * Create Schema join map</li> <li>Invoke processor to process the file contents</li> <li>Store process result</li>
	 * 
	 * @param fileNames
	 * @param files
	 * @throws Exception
	 */
	private void handleDataFile(String[] fileNames, File[] files) throws Exception {
		String inputFlleName = fileNames[0];
		String lowerCaseName = inputFlleName.toLowerCase();
		if (lowerCaseName.endsWith(".txt") || lowerCaseName.endsWith(".csv") || lowerCaseName.endsWith(".xls")) {
			setMessage("File " + inputFlleName + " processed. See results below.");
			File sessionDirectory = SessionRepository.getSessionDirectory();
			OJBMap ojbMap = OJBMapCache.getOJBMap();
			DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
			SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
			FileProcessor processor = null;
			if (lowerCaseName.endsWith(".xls")) {
				processor = new ExcelProcessor();
			} else {
				processor = new CSVTextProcessor();
			}
			File good = new File(sessionDirectory, prepareFileName(inputFlleName, "good"));
			File bad = new File(sessionDirectory, prepareFileName(inputFlleName, "bad"));

			FileStatistics result = processor.process(getConnectionName(), dbMap.getTableDescriptor(getTableName()), files[0], isHeaderIncluded(), joinMetaData, good, bad);
			setStatistics(result);
			HttpServletRequest request = ServletActionContext.getRequest();
			HttpSession session = request.getSession(true);
			if (!result.isImportable()) {
				setMessage(result.getFileError());
				FileUtil.deleteContents(sessionDirectory);
			} else {
				if (result.getBadCount() > 0) {
					setBadRecords(true);
					session.setAttribute("badCount", result.getBadCount());
				}
				if (result.getGoodCount() > 0) {
					setGoodRecords(true);
					session.setAttribute("goodCount", result.getGoodCount());
				}
			}
			setInputFileName(inputFlleName);
		} else {
			setMessage("File was not processed, didn't meet the criteria.");
		}
	}

	public String prepareFileName(String inputFlleName, String suffix) {
		return inputFlleName.substring(0, inputFlleName.lastIndexOf('.')) + "_" + suffix + inputFlleName.substring(inputFlleName.lastIndexOf('.'));
	}

	/**
	 * Displays file import input screen
	 * 
	 * @return
	 * @throws Exception
	 */
	public String displayInput() throws Exception {
		FileUtil.deleteContents(SessionRepository.getSessionDirectory());
		return SUCCESS;
	}

	/**
	 * Uploads the data file
	 * 
	 * @return
	 * @throws Exception
	 */
	public String uploadDataFile() throws Exception {
		MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
		setBadRecords(false);
		setGoodRecords(false);
		FileUtil.deleteContents(SessionRepository.getSessionDirectory());
		if (multiWrapper.getErrors() == null || multiWrapper.getErrors().isEmpty()) {
			String[] fileNames = multiWrapper.getFileNames("dataFile");
			File[] files = multiWrapper.getFiles("dataFile");

			if (files != null && files.length == 1 && fileNames != null && fileNames.length == 1) {
				handleDataFile(fileNames, files);
			} else {
				setMessage("No file found for upload. ");
			}
		} else {
			setMessage("Upload FAILED. ");
		}
		return SUCCESS;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectioName) {
		this.connectionName = connectioName;
	}

	/**
	 * Action method to download good file contents
	 * 
	 * @return
	 * @throws Exception
	 */
	public String downloadGood() throws Exception {
		File file = new File(SessionRepository.getSessionDirectory(), prepareFileName(getInputFileName(), "good"));
		downloadContent(file);
		return null;
	}

	/**
	 * Action method to update db with good records
	 * 
	 * @return
	 * @throws Exception
	 */
	public String updateDBWithGood() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession(true);
		String goodFileName = prepareFileName(getInputFileName(), "good");
		File file = new File(SessionRepository.getSessionDirectory(), goodFileName);
		String lowerCaseName = goodFileName.toLowerCase();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		FileProcessor processor = null;
		if (lowerCaseName.endsWith(".xls")) {
			processor = new ExcelProcessor();
		} else {
			processor = new CSVTextProcessor();
		}
		FileStatistics statistics = processor.performDBUpdates(getConnectionName(), dbMap.getTableDescriptor(getTableName()), file);
		Integer goodCnt = (Integer) session.getAttribute("goodCount");
		if (goodCnt != null) {
			statistics.setGoodCount(goodCnt);
		}
		Integer badCnt = (Integer) session.getAttribute("badCount");
		if (badCnt != null) {
			statistics.setBadCount(badCnt);
		}
		setStatistics(statistics);
		setDbUpdated(true);
		return SUCCESS;
	}

	/**
	 * Private method to support download text file
	 * 
	 * @param file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected void downloadContent(File file) throws IOException, FileNotFoundException {
		HttpServletResponse response = ServletActionContext.getResponse();
		String fileName = file.getName();
		response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
		if (fileName.toLowerCase().endsWith(".xls")) {
			FileUtil.streamOut(new FileInputStream(file), response.getOutputStream(), true);
		} else {
			PrintWriter out = response.getWriter();
			FileUtil.write(new FileReader(file), out);
		}
	}

	/**
	 * Action method to download bad records
	 * 
	 * @return
	 * @throws Exception
	 */
	public String downloadBad() throws Exception {
		File file = new File(SessionRepository.getSessionDirectory(), prepareFileName(getInputFileName(), "bad"));
		downloadContent(file);
		return null;
	}

	public String downloadBlankSheet() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/xls");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getTableName() + "_blank.xls\"");
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(getTableName());
		HSSFRow row = sheet.createRow(0);
		TableDescriptor tableDescriptor = DBMapCache.getDBMap(getConnectionName()).getTableDescriptor(getTableName());
		List<ColumnDescriptor> columns = tableDescriptor.getColumns();
		short i = 0;
		for (ColumnDescriptor col : columns) {
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(new HSSFRichTextString(col.getName()));
			i++;
		}
		wb.write(response.getOutputStream());
		return null;
	}

	public String downloadBlankCsv() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/csv");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getTableName() + "_blank.csv\"");
		TableDescriptor tableDescriptor = DBMapCache.getDBMap(getConnectionName()).getTableDescriptor(getTableName());
		List<ColumnDescriptor> columns = tableDescriptor.getColumns();
		BufferedOutputStream bfos = new BufferedOutputStream(response.getOutputStream());
		int len = 0;
		String str = "";
		for (ColumnDescriptor col : columns) {
			str += col.getName();
			if (len < columns.size()) {
				str += ",";
			}
			len++;
		}
		bfos.write(str.getBytes());
		bfos.flush();
		bfos.close();
		return null;
	}

	public FileStatistics getStatistics() {
		return statistics;
	}

	public void setStatistics(FileStatistics statistics) {
		this.statistics = statistics;
	}

	public boolean isDbUpdated() {
		return dbUpdated;
	}

	public void setDbUpdated(boolean dbUpdated) {
		this.dbUpdated = dbUpdated;
	}

}
