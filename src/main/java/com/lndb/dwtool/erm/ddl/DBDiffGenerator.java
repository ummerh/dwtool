package com.lndb.dwtool.erm.ddl;

import static com.lndb.dwtool.erm.ddl.Constraint.buildConstraintName;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.IndexInfo;
import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.SequenceInfo;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.util.Configuration;

/**
 * 
 * @author harsha07
 * 
 *         This class should be able to compare a base schema to new target schema and then compare against latest OJB definition
 * 
 *         Report should list the following
 * 
 *         <li>Tables to be removed from base</li> <li>Tables to be added to base</li> <li>Tables with same name but unmatched definition</li> <li>
 *         Foreign Keys to be dropped</li> <li>Foreign keys to be added</li> <li>
 *         Constraints to be dropped</li> <li>Constraints to be added</li>
 * 
 */
public class DBDiffGenerator {
	protected DBMap baseDb;

	protected DBMap targetDb;

	protected OJBMap ojbMap;

	protected List<String> tablesToBeRemoved = new ArrayList<String>();

	protected List<String> tablesToBeAdded = new ArrayList<String>();

	protected List<String> mismatchedTables = new ArrayList<String>();

	protected List<String> allTables = new ArrayList<String>();

	protected List<ForeignKey> dbKeysToBeAdded = new ArrayList<ForeignKey>();

	protected List<ForeignKey> dbKeysToBeRemoved = new ArrayList<ForeignKey>();

	protected List<ForeignKey> changeOjbToDbKeys = new ArrayList<ForeignKey>();

	protected List<ForeignKey> ojbKeysToBeAdded = new ArrayList<ForeignKey>();

	protected List<ForeignKey> ojbKeysToBeRemoved = new ArrayList<ForeignKey>();

	protected SchemaJoinMetaData baseToTargetDBJoin;

	protected SchemaJoinMetaData targetToBaseDBJoin;

	protected SchemaJoinMetaData baseDBToOJBJoin;

	protected SchemaJoinMetaData ojbToBaseDBJoin;

	protected boolean initialized;

	private List<IndexInfo> indexesToBeAdded = new ArrayList<IndexInfo>();
	private List<IndexInfo> indexesToBeRemoved = new ArrayList<IndexInfo>();
	private List<SequenceInfo> sequencesToBeAdded = new ArrayList<SequenceInfo>();

	protected static final Pattern SIZE_DECR_EXCLUDE_PATTERN = Pattern.compile(Configuration.getProperty("report.col.size.decrease.exclude.pattern"));
	protected static final Pattern TYPE_CHANGED_EXCLUDE_PATTERN = Pattern.compile(Configuration.getProperty("report.col.type.changed.exclude.pattern"));
	protected static final Pattern DEFAULT_CHANGED_EXCLUDE_PATTERN = Pattern.compile(Configuration.getProperty("report.col.default.changed.exclude.pattern"));
	protected static final Pattern TECH_COLS_EXCLUDE_PATTERN = Pattern.compile(Configuration.getProperty("report.technical.columns.exclude.pattern"));

	public void init(DBMap baseDb, DBMap targetDb, OJBMap ojbMap) {
		this.baseDb = baseDb;
		this.targetDb = targetDb;
		this.ojbMap = ojbMap;
		this.baseToTargetDBJoin = new SchemaJoinMetaData(baseDb, targetDb);
		this.targetToBaseDBJoin = new SchemaJoinMetaData(targetDb, baseDb);
		this.baseDBToOJBJoin = new SchemaJoinMetaData(baseDb, ojbMap);
		this.ojbToBaseDBJoin = new SchemaJoinMetaData(ojbMap, baseDb);
		this.allTables.addAll(this.baseDb.defineOrder());
		findUpdateNewTables();
		findUpdateMismatchedTables();
		findUpdateKeysToBeAdded();
		findUpdateKeysToBeRemoved();
		findUpdateOjbKeysToBeAdded();
		findUpdateOjbKeysToBeRemoved();
		findIndexesToBeAdded();
		findIndexesToBeRemoved();
		findSequencesToBeAdded();
		this.initialized = true;
	}

	protected void findUpdateOjbKeysToBeAdded() {
		for (String baseTblName : this.allTables) {
			List<ForeignKey> ojbKeys = ojbMap.getForeignKeys(baseTblName);
			if (ojbKeys != null) {
				for (ForeignKey ojbKey : ojbKeys) {
					if (!baseDBToOJBJoin.isDuplicateFK(ojbKey)) {
						this.ojbKeysToBeAdded.add(ojbKey);
					}
				}
			}
		}
	}

	protected void findUpdateMismatchedTables() {
		TableDescriptor baseTbl;
		TableDescriptor targetTbl;
		for (String baseTblName : this.allTables) {
			baseTbl = baseDb.getTableDescriptor(baseTblName);
			targetTbl = targetDb.getTableDescriptor(baseTblName);
			if (baseTbl != null && targetTbl != null) {
				if (!baseTbl.isColsMatch(targetTbl)) {
					this.mismatchedTables.add(baseTblName);
				}
			} else if (targetTbl == null) {
				tablesToBeRemoved.add(baseTblName);
			}
		}
	}

	protected void findUpdateKeysToBeRemoved() {
		TableDescriptor baseTbl;
		for (String baseTblName : this.allTables) {
			baseTbl = baseDb.getTableDescriptor(baseTblName);
			if (baseTbl != null) {
				List<ForeignKey> baseKeys = baseTbl.getForeignKeys();
				for (ForeignKey baseKey : baseKeys) {
					if (!targetToBaseDBJoin.isDuplicateFK(baseKey) && !baseKey.getName().startsWith("O_")) {
						this.dbKeysToBeRemoved.add(baseKey);
						baseTbl.removeImportKey(baseKey);
					}
				}
			}
		}
	}

	protected void findUpdateOjbKeysToBeRemoved() {
		TableDescriptor baseTbl;
		for (String baseTblName : this.allTables) {
			baseTbl = baseDb.getTableDescriptor(baseTblName);
			if (baseTbl != null) {
				List<ForeignKey> baseKeys = baseTbl.getForeignKeys();
				for (ForeignKey baseKey : baseKeys) {
					if (!ojbToBaseDBJoin.isDuplicateFK(baseKey) && baseKey.getName().startsWith("O_")) {
						this.ojbKeysToBeRemoved.add(baseKey);
					}
				}
			}
		}
	}

	protected void findUpdateKeysToBeAdded() {
		TableDescriptor targetTbl;
		TableDescriptor baseTbl;
		for (String baseTblName : this.allTables) {
			baseTbl = baseDb.getTableDescriptor(baseTblName);
			targetTbl = targetDb.getTableDescriptor(baseTblName);
			if (baseTbl != null && targetTbl != null) {
				List<ForeignKey> targetKeys = targetTbl.getForeignKeys();
				if (targetKeys != null) {
					for (ForeignKey targetKey : targetKeys) {
						ForeignKey identical = baseToTargetDBJoin.findIdentical(targetKey);
						if (identical == null) {
							this.dbKeysToBeAdded.add(targetKey);
						} else if (identical.getName().startsWith("O_")) {
							this.changeOjbToDbKeys.add(identical);
						}
					}
				}
			}
		}
	}

	protected void findIndexesToBeAdded() {
		TableDescriptor targetTbl;
		TableDescriptor baseTbl;
		for (String baseTblName : this.allTables) {
			baseTbl = baseDb.getTableDescriptor(baseTblName);
			targetTbl = targetDb.getTableDescriptor(baseTblName);
			if (baseTbl != null && targetTbl != null) {
				List<IndexInfo> tgtIndexes = targetTbl.getIndexes();
				List<IndexInfo> baseIdxs = baseTbl.getIndexes();

				for (IndexInfo tgt : tgtIndexes) {
					boolean matched = false;
					for (IndexInfo src : baseIdxs) {
						if (src.isSame(tgt)) {
							matched = true;
							break;
						}
					}
					if (!matched) {
						this.indexesToBeAdded.add(tgt);
					}
				}
			}
		}
	}

	protected void findIndexesToBeRemoved() {
		TableDescriptor targetTbl;
		TableDescriptor baseTbl;
		for (String baseTblName : this.allTables) {
			baseTbl = baseDb.getTableDescriptor(baseTblName);
			targetTbl = targetDb.getTableDescriptor(baseTblName);
			if (baseTbl != null && targetTbl != null) {
				List<IndexInfo> baseIdxs = baseTbl.getIndexes();
				List<IndexInfo> tgtIndexes = targetTbl.getIndexes();

				for (IndexInfo src : baseIdxs) {
					boolean matched = false;
					for (IndexInfo tgt : tgtIndexes) {
						if (tgt.isSame(src)) {
							matched = true;
							break;
						}
					}
					if (!matched) {
						this.indexesToBeRemoved.add(src);
					}
				}
			}
		}
	}

	protected void findUpdateNewTables() {
		TableDescriptor baseTbl;
		List<String> targetTables = targetDb.getAllTables();
		for (String targetTblName : targetTables) {
			baseTbl = baseDb.getTableDescriptor(targetTblName);
			if (baseTbl == null) {
				this.tablesToBeAdded.add(targetTblName);
				TableDescriptor tgtTbl = targetDb.getTableDescriptor(targetTblName);
				// this.dbKeysToBeAdded.addAll(tgtTbl.getForeignKeys());
				this.indexesToBeAdded.addAll(tgtTbl.getIndexes());
				allTables.add(targetTblName);
			}
		}
	}

	public void writeChangeSummary(OutputStream os) throws IOException {
		if (!initialized) {
			throw new RuntimeException("Data mapping not initialized....");
		}
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
		writeTableList(writer, "TABLES TO BE REMOVED", this.tablesToBeRemoved);
		writeTableList(writer, "TABLES TO BE ADDED", this.tablesToBeAdded);
		writeTableList(writer, "TABLES MISMATCHED", this.mismatchedTables);
		writeFKList(writer, "DB RELATIONSHIPS TO BE ADDED", this.dbKeysToBeAdded);
		writeFKList(writer, "DB RELATIONSHIPS TO BE REMOVED", this.dbKeysToBeRemoved);
		writeFKList(writer, "OJB RELATIONS CHANGING TO DB (red to black)", this.changeOjbToDbKeys);
		writeFKList(writer, "OJB RELATIONSHIPS TO BE ADDED", this.ojbKeysToBeAdded);
		writeFKList(writer, "OJB RELATIONSHIPS TO BE REMOVED", this.ojbKeysToBeRemoved);
		writer.flush();
	}

	public void writeDiffReport(OutputStream os) {
		try {
			if (!initialized) {
				throw new RuntimeException("Data mapping not initialized....");
			}
			HSSFWorkbook report = new HSSFWorkbook();
			HSSFSheet sheet = report.createSheet("NewTables");
			for (int i = 0; i < this.tablesToBeAdded.size(); i++) {
				Row newRow = sheet.createRow(i);
				Cell createCell = newRow.createCell((short) 0);
				createCell.setCellValue(new HSSFRichTextString(this.tablesToBeAdded.get(i)));
			}

			sheet = report.createSheet("DeletedTables");
			for (int i = 0; i < this.tablesToBeRemoved.size(); i++) {
				Row newRow = sheet.createRow(i);
				Cell createCell = newRow.createCell((short) 0);
				createCell.setCellValue(new HSSFRichTextString(this.tablesToBeRemoved.get(i)));
			}

			CellStyle boldStyle = createBoldStyle(report);
			sheet = report.createSheet("ChangedTables");
			int rowNum = -1;
			rowNum = addMismatchesheader(sheet, rowNum, boldStyle);

			for (String mismatchTbl : this.mismatchedTables) {
				rowNum = generateTableDiffReport(mismatchTbl, rowNum, sheet);
			}
			report.write(os);
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("error", e);
		}
	}

	/**
	 * @param report
	 * @return
	 */
	protected CellStyle createBoldStyle(HSSFWorkbook report) {
		Font bold = report.createFont();
		bold.setBoldweight(Font.BOLDWEIGHT_BOLD);
		CellStyle boldStyle = report.createCellStyle();
		boldStyle.setFont(bold);
		boldStyle.setAlignment(CellStyle.ALIGN_CENTER);
		return boldStyle;
	}

	/**
	 * @param sheet
	 * @param rowNum
	 * @return
	 */
	protected int addMismatchesheader(HSSFSheet sheet, int rowCnt, CellStyle cellStyle) {
		int rowNum = rowCnt;
		Row newRow = sheet.createRow(++rowNum);
		createCell(newRow, (short) 0, "Table", cellStyle);
		createCell(newRow, (short) 1, "Column", cellStyle);
		createCell(newRow, (short) 2, "New?", cellStyle);
		createCell(newRow, (short) 3, "Size Increased?", cellStyle);
		createCell(newRow, (short) 4, "Size Decreased?", cellStyle);
		createCell(newRow, (short) 5, "Type Changed?", cellStyle);
		createCell(newRow, (short) 6, "Nullable?", cellStyle);
		createCell(newRow, (short) 7, "Not Nullable?", cellStyle);
		createCell(newRow, (short) 8, "Default Changed?", cellStyle);
		createCell(newRow, (short) 9, "Dropped?", cellStyle);
		createCell(newRow, (short) 10, "Current Spec", cellStyle);
		createCell(newRow, (short) 11, "New Spec", cellStyle);
		return rowNum;
	}

	/**
	 * @param newRow
	 * @param pos
	 *            TODO
	 * @param text
	 *            TODO
	 */
	protected void createCell(Row newRow, short pos, String text, CellStyle style) {
		Cell cell = newRow.createCell(pos);
		cell.setCellValue(new HSSFRichTextString(text));
		if (style != null) {
			cell.setCellStyle(style);
		}
	}

	public int generateTableDiffReport(String mismatchTbl, int rowCnt, HSSFSheet sheet) {
		TableDescriptor src = baseDb.getTableDescriptor(mismatchTbl);
		TableDescriptor tgt = targetDb.getTableDescriptor(mismatchTbl);
		int rowNum = rowCnt;
		if (src == null || tgt == null) {
			return rowNum;
		}
		List<ColumnDescriptor> toCols = tgt.getColumns();
		for (ColumnDescriptor tgtCol : toCols) {
			boolean addCol = false;
			boolean colSizeIncr = false;
			boolean colSizeDecr = false;
			boolean dataTypChanged = false;
			boolean changeToNullable = false;
			boolean changeToNonNullable = false;
			boolean defaultChanged = false;

			if (TECH_COLS_EXCLUDE_PATTERN.matcher(tgtCol.getName()).matches()) {
				// ignore technical columns
				continue;
			}

			ColumnDescriptor srcCol = src.getColumn(tgtCol.getName());
			if (srcCol == null) {
				addCol = true;
			} else {
				if (!tgtCol.isSizeEqual(srcCol)) {
					if (tgtCol.isSizeIncreased(srcCol)) {
						colSizeIncr = true;
					} else {
						if (!SIZE_DECR_EXCLUDE_PATTERN.matcher(tgtCol.getName()).matches()) {
							colSizeDecr = true;
						}
					}
				}
				if (!tgtCol.isJdbcTypeEqual(srcCol)) {
					if (TYPE_CHANGED_EXCLUDE_PATTERN.matcher(tgtCol.getName()).matches()) {
						continue;
					}
					dataTypChanged = true;
				}
				if (!tgtCol.isNullableEqual(srcCol)) {
					if (tgtCol.isNullable()) {
						changeToNullable = true;
					} else {
						changeToNonNullable = true;
					}
				}
				if (!tgtCol.isDefualtEqual(srcCol) && !DEFAULT_CHANGED_EXCLUDE_PATTERN.matcher(tgtCol.getName()).matches()) {
					defaultChanged = true;
				}

			}
			if (addCol || colSizeIncr || colSizeDecr || dataTypChanged || changeToNullable || changeToNonNullable || defaultChanged) {
				Row newRow = sheet.createRow(++rowNum);
				newRow.createCell((short) 0).setCellValue(new HSSFRichTextString(mismatchTbl));
				newRow.createCell((short) 1).setCellValue(new HSSFRichTextString(tgtCol.getName()));
				newRow.createCell((short) 2).setCellValue(new HSSFRichTextString(addCol ? "Y" : ""));
				newRow.createCell((short) 3).setCellValue(new HSSFRichTextString(colSizeIncr ? "Y" : ""));
				newRow.createCell((short) 4).setCellValue(new HSSFRichTextString(colSizeDecr ? "Y" : ""));
				newRow.createCell((short) 5).setCellValue(new HSSFRichTextString(dataTypChanged ? "Y" : ""));
				newRow.createCell((short) 6).setCellValue(new HSSFRichTextString(changeToNullable ? "Y" : ""));
				newRow.createCell((short) 7).setCellValue(new HSSFRichTextString(changeToNonNullable ? "Y" : ""));
				newRow.createCell((short) 8).setCellValue(new HSSFRichTextString(defaultChanged ? "Y" : ""));
				if (srcCol != null) {
					newRow.createCell((short) 10).setCellValue(new HSSFRichTextString(srcCol.toSpecString()));
				}
				newRow.createCell((short) 11).setCellValue(new HSSFRichTextString(tgtCol.toSpecString()));
			}
		}
		// find coumns to be dropped
		List<ColumnDescriptor> srcCols = src.getColumns();
		for (ColumnDescriptor col : srcCols) {
			if (tgt.getColumn(col.getName()) == null) {
				Row newRow = sheet.createRow(++rowNum);
				newRow.createCell((short) 0).setCellValue(new HSSFRichTextString(mismatchTbl));
				newRow.createCell((short) 1).setCellValue(new HSSFRichTextString(col.getName()));
				newRow.createCell((short) 9).setCellValue(new HSSFRichTextString("Y"));
			}
		}
		// find if PK is same
		List<String> tgtKeys = tgt.getPrimaryKeys();
		List<String> srcKeys = src.getPrimaryKeys();
		boolean pkMatched = true;
		if (tgtKeys.size() != srcKeys.size()) {
			pkMatched = false;

		} else {
			for (String pk : tgtKeys) {
				if (!srcKeys.contains(pk)) {
					pkMatched = false;
					break;
				}
			}
		}
		if (!pkMatched) {
			Row newRow = sheet.createRow(++rowNum);
			newRow.createCell((short) 0).setCellValue(new HSSFRichTextString(tgt.getTableName() + ": PK Mismatch"));
			newRow.createCell((short) 10).setCellValue(new HSSFRichTextString(srcKeys.toString()));
			newRow.createCell((short) 11).setCellValue(new HSSFRichTextString(tgtKeys.toString()));
		}
		return rowNum;
	}

	protected void writeTableList(BufferedWriter writer, String header, List<String> list) throws IOException {
		writer.write(header);
		writer.newLine();
		writer.write("--------------------------------------------");
		writer.newLine();
		for (String tbl : list) {
			writer.write(tbl);
			writer.newLine();
		}
		writer.write("----------------------------------------------------------------------------------------");
		writer.newLine();
		writer.newLine();
	}

	protected void writeFKList(BufferedWriter writer, String header, List<ForeignKey> list) throws IOException {
		writer.write(header);
		writer.newLine();
		writer.write("--------------------------------------------");
		writer.newLine();
		for (ForeignKey key : list) {
			writer.write(key.getReferByTable() + "  " + key.getName() + "  " + key.getReferByCols().toString() + "   >>   " + key.getReferToTable() + "  " + key.getReferToCols());
			writer.newLine();
		}
		writer.write("----------------------------------------------------------------------------------------");
		writer.newLine();
		writer.newLine();
	}

	public void writeDiffDDL(OutputStream os) throws InvalidMapException, IOException {

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
		// mismatched tables
		writeMismatchedTableColSQL(out);
		// tables to be removed
		writeTablesRemovedSQL(out);
		// tables to be added
		writeTablesAddedSQL(out);
		// db constraints to be dropped
		writeRelationsRemovedSQL(out);
		// db constraints to be added
		writeRelationsAddedSQL(out);
		// ojb constraints to be dropped
		// writeOJBRelationsRemovedSQL(out);
		// ojb constraints to be added
		// writeOJBRelationsAddedSQL(out);
		writeIndexesAddedSQL(out);
		writeSequencesAddedSQL(out);
		out.flush();

	}

	protected void writeOJBRelationsAddedSQL(BufferedWriter out) throws IOException {
		Integer counter = new Integer(0);
		addComment(out, "OJB RELATIONSHIPS TO BE ADDED");
		for (ForeignKey constraint : this.ojbKeysToBeAdded) {
			if (constraint.isValid()) {
				counter++;
				out.write(DDL.addConstraintDDL(constraint.getReferByTable(), buildConstraintName("O", constraint.getReferByTable(), "FK", counter), constraint));
				out.newLine();
			}
		}
	}

	protected void writeOJBRelationsRemovedSQL(BufferedWriter out) throws IOException {
		addComment(out, "OJB RELATIONSHIPS TO BE REMOVED");
		for (ForeignKey constraint : this.ojbKeysToBeRemoved) {
			out.write(DDL.dropConstraintDDL(constraint.getReferByTable(), constraint.getName()));
			out.newLine();
		}
	}

	protected void writeRelationsAddedSQL(BufferedWriter out) throws IOException {
		addComment(out, "DB RELATIONSHIPS TO BE ADDED");
		for (ForeignKey constraint : this.dbKeysToBeAdded) {
			String name = constraint.getName();
			TableDescriptor baseTbl = this.baseDb.getTableDescriptor(constraint.getReferByTable());
			if (baseTbl != null) {
				if (baseDb.isContraintNameExists(name)) {
					// avoid duplicate name
					name = name + "A";
				}
			}
			out.write(DDL.addConstraintDDL(constraint.getReferByTable(), name, constraint));
			out.newLine();
		}
	}

	protected void writeRelationsRemovedSQL(BufferedWriter out) throws IOException {
		addComment(out, "DB RELATIONSHIPS TO BE REMOVED");
		for (ForeignKey constraint : this.dbKeysToBeRemoved) {
			out.write("--" + DDL.dropConstraintDDL(constraint.getReferByTable(), constraint.getName()));
			out.newLine();
		}
	}

	protected void writeIndexesAddedSQL(BufferedWriter out) throws IOException {
		addComment(out, "DB UNIQUE CONSTRAINTS TO BE ADDED");
		for (IndexInfo index : this.indexesToBeAdded) {
			if (index.isUnique()) {
				String name = index.getName();
				out.write(DDL.addUniqueDDL(index.getTableName(), name, index));
				out.newLine();
			}
		}
		addComment(out, "DB INDEXES TO BE ADDED");
		for (IndexInfo index : this.indexesToBeAdded) {
			if (!index.isUnique()) {
				out.write(DDL.addIndexDDL(index.getTableName(), index.getName(), index));
				out.newLine();
			}
		}
	}

	protected void writeSequencesAddedSQL(BufferedWriter out) throws IOException {
		addComment(out, "DB SEQUENCES TO BE ADDED");
		for (SequenceInfo sequence : this.sequencesToBeAdded) {
			out.write(getBaseDBDialect().createSequence(sequence));
			out.newLine();
		}
	}

	protected void writeIndexesRemovedSQL(BufferedWriter out) throws IOException {
		addComment(out, "DB INDEXES TO BE REMVOED");
		for (IndexInfo index : this.indexesToBeRemoved) {
			if (index.isUnique()) {
				out.write(DDL.dropConstraintDDL(index.getTableName(), index.getName()));
				out.newLine();
			} else {
				// FIXME DROP index
			}
		}
	}

	protected void writeTablesAddedSQL(BufferedWriter out) throws IOException, InvalidMapException {
		DDLGenerator ddlGenerator = new DDLGenerator(ojbMap, this.targetDb);
		addComment(out, "TABLES TO BE ADDED");
		for (String tableName : this.tablesToBeAdded) {
			out.write(ddlGenerator.prepareDDL(tableName));
			out.newLine();
		}
		out.write(ddlGenerator.getConstraintsDDL());
		out.newLine();
	}

	protected void writeTablesRemovedSQL(BufferedWriter out) throws IOException {
		addComment(out, "TABLES TO BE REMOVED");
		for (String tableName : this.tablesToBeRemoved) {
			out.write("--drop table " + tableName + " cascade constraints;");
			out.newLine();
		}
	}

	protected void writeMismatchedTableColSQL(BufferedWriter out) throws IOException {
		addComment(out, "TABLES MODIFIED");
		for (String mismatchTblNm : this.mismatchedTables) {
			out.write(getBaseDBDialect().generateTableDiffDLL(this.baseDb.getTableDescriptor(mismatchTblNm), this.targetDb.getTableDescriptor(mismatchTblNm)));
		}
		out.newLine();
	}

	private Dialect getBaseDBDialect() {
		return this.baseDb.getConnectionDetail().getDialect();
	}

	protected void addComment(BufferedWriter out, String comment) throws IOException {
		out.newLine();
		out.write("--" + comment);
		out.newLine();
	}

	protected void findSequencesToBeAdded() {
		List<SequenceInfo> tgtIndexes = this.targetDb.getSequences();
		for (SequenceInfo tgt : tgtIndexes) {
			if (baseDb.getSequenceInfo(tgt.getSequenceName()) == null) {
				this.sequencesToBeAdded.add(tgt);
			}
		}
	}
}
