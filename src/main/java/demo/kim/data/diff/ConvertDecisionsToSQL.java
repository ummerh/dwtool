package demo.kim.data.diff;

import java.io.FileInputStream;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.util.ExcelReaderBase;

public class ConvertDecisionsToSQL extends ExcelReaderBase {
	public static void main(String[] args) {
		try {
			DBMap lndbDb = DBMapCache.getDBMap("FINDEV");
			DBMap fndnDb = DBMapCache.getDBMap("kfs5r2");

			FileInputStream dataInputStream = new FileInputStream("/TEMP/KFS-DB-decision-matrix.xls");
			Workbook wb = new HSSFWorkbook(new POIFSFileSystem(dataInputStream));
			Iterator<Row> rows = wb.getSheet("LNDBTables").iterator();
			Row headerRow = (rows != null && rows.hasNext()) ? rows.next() : null;
			Map<String, Integer> headerMap = readHeaderMap(headerRow);
			while (rows.hasNext()) {
				List<String> dataCols = readDataCells(rows.next());
				// Sheet Name: LNDB Tables; Technical Decision � Accept ; Create script following pattern �drop table <tablename>�
				String tableName = readColValue("Table", headerMap, dataCols);
				String decision = readColValue("Technical Decision", headerMap, dataCols);
				if ("Accept".equals(decision)) {
					System.out.println("drop table " + tableName + ";");
				}
			}

			System.out.println("-------------------");

			rows = wb.getSheet("ChangedTables").iterator();
			headerRow = (rows != null && rows.hasNext()) ? rows.next() : null;
			headerMap = readHeaderMap(headerRow);
			while (rows.hasNext()) {
				List<String> dataCols = readDataCells(rows.next());
				String tableName = readColValue("Table", headerMap, dataCols);
				String decision = readColValue("Technical Decision", headerMap, dataCols);
				String column = readColValue("Column", headerMap, dataCols);

				// Sheet: Changed Tables; Column: New; Technical Decision: No Change; Create script following pattern �alter table <tablename> drop column <columnname>�
				if ("No Change".equals(decision) && "Y".equals(readColValue("New?", headerMap, dataCols))) {
					System.out.println("--revert add decision");
					System.out.println("alter table " + tableName + " drop column " + column + " ;");
				}
				// Sheet: Changed Tables; Column: Nullable; Technical Decision: No Change; Create script following pattern �alter table <tablename> modify column <column name> not null �
				if ("No Change".equals(decision) && "Y".equals(readColValue("Nullable?", headerMap, dataCols))) {
					System.out.println("--revert column null decision");
					System.out.println("alter table " + tableName + " modify column not null;");
				}

				// Sheet: Changed Tables; Column: Dropped; Technical Decision: Accept; Create script following pattern �alter table drop column <column name>�
				if ("Accept".equals(decision) && "Y".equals(readColValue("Dropped?", headerMap, dataCols))) {
					System.out.println("--revert accept column decision");
					System.out.println("alter table " + tableName + " drop column " + column + " ;");
				}

				// Sheet: Changed Tables; Column: Size Increased; Technical Decision: No Change; Create script following pattern �alter table <tablename> modify column <reduce size back as per old
				// spec>�
				if ("No Change".equals(decision) && "Y".equals(readColValue("Size Increased?", headerMap, dataCols))) {
					System.out.println("--revert size increase decision");
					TableDescriptor srcTbl = lndbDb.getTableDescriptor(tableName);
					ColumnDescriptor srcCol = srcTbl.getColumn(column);
					String colDefault = srcCol.getColDefault();
					System.out.println("alter table " + tableName + " modify (" + column + " " + getSqlDataType(srcCol, null, null, null)
							+ (StringUtils.isNotBlank(colDefault) ? " default " + colDefault : "") + ");");
				}

				// Sheet: Changed Tables; Column: Size Decreased; Technical Decision: Accept; Create script following pattern �alter table <tablename> modify column <reduce size back as per new spec>�
				if ("Accept".equals(decision) && "Y".equals(readColValue("Size Decreased?", headerMap, dataCols))) {
					TableDescriptor srcTbl = lndbDb.getTableDescriptor(tableName);
					ColumnDescriptor srcCol = srcTbl.getColumn(column);
					String colDefault = srcCol.getColDefault();
					TableDescriptor tgtTbl = fndnDb.getTableDescriptor(tableName);
					ColumnDescriptor tgtCol = tgtTbl.getColumn(column);
					System.out.println("--revert size decrease decision");
					System.out.println("alter table " + tableName + " modify (" + column + " " + getSqlDataType(srcCol, null, tgtCol.getSize(), tgtCol.getDecimalDigits())
							+ (StringUtils.isNotBlank(colDefault) ? " default " + colDefault : "") + ");");
				}
				// Sheet: Changed Tables; Column: Type Changed; Technical Decision: No Change; Create script following pattern �alter table <tablename> modify column <change type back as per oldspec>�
				if ("No Change".equals(decision) && "Y".equals(readColValue("Type Changed?", headerMap, dataCols))) {
					TableDescriptor srcTbl = lndbDb.getTableDescriptor(tableName);
					ColumnDescriptor srcCol = srcTbl.getColumn(column);
					String colDefault = srcCol.getColDefault();
					System.out.println("--revert type change decision");
					System.out.println("alter table " + tableName + " modify (" + column + " " + getSqlDataType(srcCol, null, null, null)
							+ (StringUtils.isNotBlank(colDefault) ? " default " + colDefault : "") + ");");
				}
				// Sheet: Changed Tables; Column: Default Changed; Technical Decision: No Change; Create script following pattern �alter table <tablename> modify column <change default back as per old
				// spec>�
				if ("No Change".equals(decision) && "Y".equals(readColValue("Default Changed?", headerMap, dataCols))) {
					TableDescriptor srcTbl = lndbDb.getTableDescriptor(tableName);
					ColumnDescriptor srcCol = srcTbl.getColumn(column);
					String colDefault = srcCol.getColDefault();
					System.out.println("--revert default change decision");
					System.out.println("alter table " + tableName + " modify (" + column + " " + getSqlDataType(srcCol, null, null, null)
							+ (StringUtils.isNotBlank(colDefault) ? " default " + colDefault : "") + ");");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getSqlDataType(ColumnDescriptor columnDescriptor, Integer type, Integer size, Integer dec) {
		String sqlDataType = null;
		int jdbcType = columnDescriptor.getJdbcType();
		if (type != null) {
			jdbcType = type;
		}
		int sizeVal = columnDescriptor.getSize();
		if (size != null) {
			sizeVal = size;
		}
		int decVal = columnDescriptor.getDecimalDigits();
		if (dec != null) {
			decVal = dec;
		}
		switch (jdbcType) {
		case Types.BIGINT:
			sqlDataType = "BIGINT";
			break;
		case Types.BINARY:
			sqlDataType = "BINARY";
			break;
		case Types.BIT:
			sqlDataType = "BIT";
			break;
		case Types.BLOB:
			sqlDataType = "BLOB";
			break;
		case Types.BOOLEAN:
			sqlDataType = "BOOLEAN";
			break;
		case Types.CHAR:
			sqlDataType = "CHAR" + "(" + sizeVal + ")";
			break;
		case Types.CLOB:
			sqlDataType = "CLOB";
			break;
		case Types.DATE:
			sqlDataType = "DATE";
			break;
		case Types.DECIMAL:
			if (sizeVal > 0 || decVal > 0) {
				sqlDataType = "NUMBER" + "(" + sizeVal + (decVal > 0 ? "," + decVal : "") + ")";
			} else {
				sqlDataType = "INTEGER";
			}
			break;
		case Types.DOUBLE:
			sqlDataType = "DOUBLE" + "(" + sizeVal + (decVal > 0 ? "," + decVal : "") + ")";
			break;
		case Types.FLOAT:
			sqlDataType = "FLOAT" + "(" + sizeVal + (decVal > 0 ? "," + decVal : "") + ")";
			break;
		case Types.INTEGER:
			sqlDataType = "INTEGER";
			break;
		case Types.NUMERIC:
			sqlDataType = "NUMERIC" + "(" + sizeVal + (decVal > 0 ? "," + decVal : "") + ")";
			break;

		case Types.SMALLINT:
			sqlDataType = "SMALLINT";
			break;
		case Types.TIME:
			sqlDataType = "TIME";
			break;
		case Types.TIMESTAMP:
			sqlDataType = "TIMESTAMP";
			break;

		case Types.TINYINT:
			sqlDataType = "SMALLINT";
			break;
		case Types.VARBINARY:
			sqlDataType = "VARBINARY";
			break;
		case Types.VARCHAR:
			sqlDataType = "VARCHAR2" + "(" + sizeVal + ")";
			break;
		case Types.SQLXML:
			sqlDataType = "XMLTYPE";
			break;
		default:
			sqlDataType = "NULL";
			break;
		}
		return sqlDataType;
	}
}
