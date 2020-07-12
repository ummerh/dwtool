package com.lndb.dwtool.erm.manager.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.manager.web.jquery.EasyUiTreeNode;
import com.lndb.dwtool.erm.manager.web.jquery.QueryResultView;
import com.lndb.dwtool.erm.util.Configuration;
import com.lndb.dwtool.erm.util.Connections;
import com.lndb.dwtool.erm.util.ExcelConverter;
import com.lndb.dwtool.erm.util.FileUtil;
import com.lndb.dwtool.erm.util.ExcelConverter.ExcelDataSource;
import com.lndb.dwtool.erm.xldb.ExcelTable;
import com.lndb.dwtool.erm.xldb.ExcelWorkbookDB;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class that supports all OJB repository handling actions
 * 
 * @author harsha07
 * 
 */
public class ExcelDBAction extends ActionSupport {

	private String sqlText;

	private static class WorkerThread extends Thread {
		private File file;

		public WorkerThread(File file) {
			this.file = file;
		}

		@Override
		public void run() {
			ExcelWorkbookDB workbookDB = new ExcelWorkbookDB();
			try {
				workbookDB.load(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static final long serialVersionUID = 8307527275066616243L;
	private String fileName;
	private List<String> availableFiles = new ArrayList<String>();

	public String display() throws Exception {
		File[] list = ExcelWorkbookDB.listExcelFiles();
		for (File fl : list) {
			if (fl.exists() && fl.isFile()) {
				availableFiles.add(fl.getName());
			}
		}
		return SUCCESS;
	}

	public String upload() throws Exception {
		String excelDir = ExcelWorkbookDB.getExcelDir();
		MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
		if (multiWrapper.getErrors() == null || multiWrapper.getErrors().isEmpty()) {
			String[] fileNames = multiWrapper.getFileNames("excelFile");
			File[] files = multiWrapper.getFiles("excelFile");

			if (files != null && files.length == 1 && fileNames != null && fileNames.length == 1) {
				String jsonReply = null;
				File dest = new File(excelDir, fileNames[0]);
				if (!dest.exists()) {
					files[0].renameTo(dest);
					new WorkerThread(dest).start();
					jsonReply = buildJsonMessage(fileNames);
				} else {
					jsonReply = "{\"status\":\"File " + fileNames[0] + " already exists.\"}";
				}
				HttpServletResponse response = ServletActionContext.getResponse();
				PrintWriter out = response.getWriter();
				out.print(jsonReply);
				out.flush();
			}
		}
		return null;
	}

	private String buildJsonMessage(String[] fileNames) {
		String jsonReply;
		jsonReply = "{\"status\":\"<a href='./excelDBAction!download.action?fileName=" + fileNames[0] + "'>" + "<img height='20' width='20' src='images/excel-icon.jpg'>" + "</a>&nbsp;" + fileNames[0]
				+ " uploaded successfully.&nbsp;&nbsp;<a href='./excelDBAction!delete.action?fileName=" + fileNames[0] + "'><img src='images/delete.jpg'></a>\"}";
		return jsonReply;
	}

	public String download() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/xml");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getFileName() + "\"");
		String excelDir = Configuration.getProperty("excel.repository");
		FileUtil.write(new FileInputStream(new File(excelDir + getFileName())), response.getOutputStream());
		return null;
	}

	public String delete() throws Exception {
		String excelDir = Configuration.getProperty("excel.repository");
		File file = new File(excelDir, getFileName());
		try {
			ExcelWorkbookDB workbookDB = new ExcelWorkbookDB();
			workbookDB.drop(file);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			file.delete();
		}
		return display();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<String> getAvailableFiles() {
		return availableFiles;
	}

	public void setAvailableFiles(List<String> availableFiles) {
		this.availableFiles = availableFiles;
	}

	public String fileView() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out = response.getWriter();
		File[] excelFiles = ExcelWorkbookDB.listExcelFiles();
		int id = 0;

		List<EasyUiTreeNode> rootNodes = new ArrayList<EasyUiTreeNode>();
		EasyUiTreeNode root = new EasyUiTreeNode();
		root.setId(++id);
		root.setText("DB");
		root.setIconCls("icon-reload");
		rootNodes.add(root);
		for (File file : excelFiles) {
			EasyUiTreeNode node = new EasyUiTreeNode();
			node.setId(++id);
			node.setText(file.getName());
			root.getChildren().add(node);
			List<ExcelTable> tables = ExcelWorkbookDB.getDBMap().get(file.getName().toLowerCase());
			if (tables != null) {
				for (ExcelTable excelTable : tables) {
					EasyUiTreeNode child = new EasyUiTreeNode();
					child.setId(++id);
					child.setText(excelTable.getTableName());
					node.getChildren().add(child);
				}
			} else {
				EasyUiTreeNode child = new EasyUiTreeNode();
				child.setId(++id);
				child.setText("Failed to load..");
				node.getChildren().add(child);
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, rootNodes);
		out.flush();
		return null;
	}

	public String query() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		QueryResultView resultView = new QueryResultView();
		executeQuery(resultView);
		new ObjectMapper().writeValue(response.getOutputStream(), resultView);
		return null;
	}

	public String exportResult() throws Exception {
		try {
			final QueryResultView resultView = new QueryResultView();
			executeQuery(resultView);
			HttpServletResponse response = ServletActionContext.getResponse();
			String fileName = "excel_results.xls";
			response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
			ExcelConverter converter = new ExcelConverter();
			converter.writeOut(response.getOutputStream(), new ExcelDataSource() {
				private QueryResultView queryResultView;
				private int pos = 0;

				public String[] nextRow() {
					if (queryResultView == null) {
						queryResultView = resultView;
					}
					String[] data = new String[this.queryResultView.getData().get(pos).size()];
					this.queryResultView.getData().get(pos).toArray(data);
					pos++;
					return data;
				}

				public boolean hasNext() {
					if (queryResultView == null) {
						queryResultView = resultView;
					}
					return pos < queryResultView.getData().size();
				}

				public String[] getHeaders() {
					if (queryResultView == null) {
						queryResultView = resultView;
					}
					String[] headers = new String[this.queryResultView.getHeaders().size()];
					this.queryResultView.getHeaders().toArray(headers);
					return headers;
				}
			});
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	private void executeQuery(final QueryResultView resultView) throws SQLException {
		if (StringUtils.isNotBlank(getSqlText())) {
			Connection con = null;
			Statement stmt = null;
			try {
				con = Connections.MEMORYDB.newConnection();
				stmt = con.createStatement();
				String sqlStr = getSqlText().trim().replace(";", "");
				if (sqlStr.toLowerCase().startsWith("select")) {
					ResultSet rs = stmt.executeQuery(sqlStr);
					ResultSetMetaData metaData = null;
					if (rs != null) {
						metaData = rs.getMetaData();
					}
					for (int k = 1; k <= metaData.getColumnCount(); k++) {
						resultView.getHeaders().add(metaData.getColumnName(k));
					}
					while (rs.next()) {
						List<String> data = new ArrayList<String>();
						resultView.getData().add(data);
						for (int k = 1; k <= metaData.getColumnCount(); k++) {
							data.add(rs.getString(k));
						}
					}
				} else {
					int cnt = stmt.executeUpdate(sqlStr);
					List<String> data = new ArrayList<String>();
					resultView.getData().add(data);
					data.add("Updated records count - " + cnt);
				}

			} catch (Exception e) {
				resultView.getHeaders().clear();
				resultView.getHeaders().add("Error");
				List<String> data = new ArrayList<String>();
				resultView.getData().add(data);
				data.add("Error occurred - " + e.getMessage());
				e.printStackTrace();
			} finally {
				DatabaseConnection.release(stmt, con);
			}
		} else {
			if (resultView.getHeaders().isEmpty()) {
				resultView.getHeaders().add("Result");
				List<String> data = new ArrayList<String>();
				resultView.getData().add(data);
				data.add("No data found");
			}
		}
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}
}
