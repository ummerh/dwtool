package com.lndb.dwtool.erm.xldb;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.util.Connections;

public class ExcelTable {

	private String filePath;
	private Workbook workbook;
	private Sheet sheet;
	private List<String> colNames = new ArrayList<String>();

	private String tableName;
	private int sheetIndex;
	private boolean error;

	public ExcelTable(String filePath, Workbook wb, int sheetIndexPos) {
		this.filePath = filePath;
		this.workbook = wb;
		this.sheetIndex = sheetIndexPos;
	}

	protected void createTable() {
		try {
			Connection con = Connections.MEMORYDB.newConnection();
			Statement stmt = con.createStatement();
			drop();
			String sql = "create table " + getTableName() + " (SN INTEGER";
			int colSize = this.colNames.size();
			for (int i = 0; i < colSize; i++) {
				sql = sql + ", " + this.colNames.get(i) + " varchar(1000) ";
			}
			sql = sql + ")";
			stmt.execute(sql);
		} catch (Exception exception) {
			throw new RuntimeException("", exception);
		}
	}

	public void drop() {
		generateTablename();
		Connection con = Connections.MEMORYDB.newConnection();
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("drop table " + getTableName());
			System.out.println("Dropped table " + getTableName());
		} catch (Exception e) {
			System.err.println("Could not drop table " + getTableName());
		} finally {
			try {
				DatabaseConnection.release(stmt, con);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	protected void generateColumnNames(Row row) {
		Iterator<Cell> cells = row.iterator();
		int colChar = 65;
		int prefixChar = 64;
		while (cells.hasNext()) {
			cells.next();
			String colName = (prefixChar > 64 ? (char) prefixChar : "") + String.valueOf((char) colChar++);
			// special keyword
			if ("BY".equals(colName) || "AS".equals(colName) || "AT".equals(colName)) {
				colName = "A" + colName;
			}
			colNames.add(colName);
			if (colChar == 91) {
				colChar = 65;
				prefixChar++;
			}
			if (prefixChar == 90) {
				break;
			}
		}
	}

	protected void generateTablename() {
		File file = new File(getFilePath());
		this.tableName = ("WB_" + file.getName() + "_S" + (this.sheetIndex + 1)).replace(".xls", "").replace("-", "_").replace(" ", "_").replace(".", "_").toUpperCase();
	}

	public String getFilePath() {
		return filePath;
	}

	public String getTableName() {
		return tableName;
	}

	protected void init() {
		this.sheet = this.workbook.getSheetAt(sheetIndex);
		generateTablename();
		Row row = this.sheet.getRow(0);
		if (row == null) {
			error = true;
			System.out.println("First row should not be empty for " + getTableName());
			return;
		}
		generateColumnNames(row);
	}

	public void load() {
		init();
		if (!error) {
			createTable();
			populate();
		}
	}

	protected void populate() {
		try {
			Connection con = Connections.MEMORYDB.newConnection();
			String sql = "insert into " + getTableName() + " values(?";
			int colSize = this.colNames.size();
			for (int i = 0; i < colSize; i++) {
				sql = sql + ", ? ";
			}
			sql = sql + ")";
			PreparedStatement pstmt = con.prepareStatement(sql);
			Iterator<Row> rows = this.sheet.iterator();
			Row row = null;
			int rowCount = 0;
			while (rows.hasNext()) {
				rowCount++;
				row = rows.next();
				List<String> data = readDataCells(row);
				pstmt.setInt(1, rowCount);
				int pos = 2;
				for (int i = 0; i < this.colNames.size(); i++) {
					pstmt.setString(pos++, StringUtils.substring(StringUtils.trimToEmpty(data.get(i)), 0, 1000));
				}
				pstmt.addBatch();
				if (rowCount % 100 == 0) {
					pstmt.executeBatch();
					pstmt.clearBatch();
				}
			}
			pstmt.executeBatch();
			System.out.println(getTableName() + " Total count - " + rowCount);
			DatabaseConnection.release(pstmt, con);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private List<String> readDataCells(Row row) {
		Cell cell = null;
		int size = this.colNames.size();
		ArrayList<String> data = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
			int cellType = cell.getCellType();
			switch (cellType) {
			case Cell.CELL_TYPE_BLANK:
				data.add("");
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				data.add(String.valueOf(cell.getBooleanCellValue()));
				break;
			case Cell.CELL_TYPE_ERROR:
				data.add("");
				break;
			case Cell.CELL_TYPE_FORMULA:
				data.add("");
				break;
			case Cell.CELL_TYPE_NUMERIC:
				double numericCellValue = cell.getNumericCellValue();
				if (numericCellValue % 1 == 0) {
					data.add(String.valueOf((int) numericCellValue));
				} else {
					data.add(String.valueOf(numericCellValue));
				}
				break;
			case Cell.CELL_TYPE_STRING:
				data.add(cell.getStringCellValue());
				break;
			default:
				data.add("");
				break;
			}
		}
		return data;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	public Sheet getSheet() {
		return sheet;
	}

	public void setSheet(Sheet sheet) {
		this.sheet = sheet;
	}

	public List<String> getColNames() {
		return colNames;
	}

	public void setColNames(List<String> colNames) {
		this.colNames = colNames;
	}

	public int getSheetIndex() {
		return sheetIndex;
	}

	public void setSheetIndex(int sheetIndex) {
		this.sheetIndex = sheetIndex;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

}
