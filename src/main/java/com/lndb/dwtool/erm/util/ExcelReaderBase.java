package com.lndb.dwtool.erm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public abstract class ExcelReaderBase {
	protected static Map<String, Integer> readHeaderMap(Row row) {
		List<String> data = readDataCells(row);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int index = 0;
		for (String string : data) {
			map.put(string, index++);
		}
		return map;
	}

	protected static String readColValue(String colName, Map<String, Integer> headerMap, List<String> dataCols) {
		Integer index = headerMap.get(colName);
		if (index != null) {
			return dataCols.get(index);
		}
		return null;
	}

	protected static List<String> readDataCells(Row row) {
		ArrayList<String> data = new ArrayList<String>();
		Iterator<Cell> cells = row.iterator();
		Cell cell = null;
		while (cells.hasNext()) {
			cell = cells.next();
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
				data.add(String.valueOf((int) cell.getNumericCellValue()));
				break;
			case Cell.CELL_TYPE_STRING:
				data.add(cell.getStringCellValue().trim());
				break;
			default:
				data.add(cell.getStringCellValue().trim());
				break;
			}
		}
		return data;
	}
}
