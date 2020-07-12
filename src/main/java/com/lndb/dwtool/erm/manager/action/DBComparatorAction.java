package com.lndb.dwtool.erm.manager.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.ddl.DBDiffGenerator;
import com.lndb.dwtool.erm.ddl.DataDiffGenerator;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.util.ConnectionRepository;
import com.opensymphony.xwork2.ActionSupport;

/**
 * 
 * @author harsha07
 * 
 */
public class DBComparatorAction extends ActionSupport {
	private String sourceDb;
	private String targetDb;

	/**
     * 
     */
	private static final long serialVersionUID = -4025402883486041044L;
	private List<ConnectionDetail> connections = new ArrayList<ConnectionDetail>();

	@SkipValidation
	public String displayInput() throws Exception {
		return INPUT;
	}

	private void prepareConnectionList() {
		this.connections.clear();
		List<ConnectionDetail> allConnections = ConnectionRepository.getAllConnections();
		ConnectionRepository.validateConnections(allConnections);
		for (ConnectionDetail connectionDetail : allConnections) {
			if (connectionDetail.isValid()) {
				this.connections.add(connectionDetail);
			}
		}
	}

	public String getSourceDb() {
		return sourceDb;
	}

	public void setSourceDb(String sourceDb) {
		this.sourceDb = sourceDb;
	}

	public String compareDb() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap target = DBMapCache.getDBMap(getTargetDb());
		DBMap source = DBMapCache.getDBMap(getSourceDb());
		DBDiffGenerator diffGenerator = new DBDiffGenerator();
		diffGenerator.init(source, target, ojbMap);
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/sql");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getSourceDb() + "_diff.sql\"");
		// diffGenerator.writeChangeSummary(ServletActionContext.getResponse().getOutputStream());
		diffGenerator.writeDiffDDL(ServletActionContext.getResponse().getOutputStream());
		ServletActionContext.getResponse().getOutputStream().flush();
		ServletActionContext.getResponse().getOutputStream().close();
		return null;
	}

	public String dbDiffReport() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap target = DBMapCache.getDBMap(getTargetDb());
		DBMap source = DBMapCache.getDBMap(getSourceDb());
		DBDiffGenerator diffGenerator = new DBDiffGenerator();
		diffGenerator.init(source, target, ojbMap);
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/xls");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getSourceDb() + "_diff.xls\"");
		diffGenerator.writeDiffReport(ServletActionContext.getResponse().getOutputStream());
		return null;
	}

	public String dataDiffReport() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap target = DBMapCache.getDBMap(getTargetDb());
		DBMap source = DBMapCache.getDBMap(getSourceDb());
		DataDiffGenerator diffGenerator = new DataDiffGenerator();
		diffGenerator.init(source, target, ojbMap);
		diffGenerator.compareData(ServletActionContext.getResponse().getOutputStream());
		return null;
	}

	/**
	 * @param source
	 * @param target
	 * @param stmt
	 * @param refTbl
	 * @param primaryKeys
	 * @param tgtTbl
	 * @param cnt
	 * @return
	 * @throws SQLException
	 */
	private int findRecordCount(DBMap source, DBMap target, Statement stmt, String refTbl, List<String> primaryKeys, TableDescriptor tgtTbl) throws SQLException {
		int cnt = 0;
		// only if new records found in target
		ResultSet rs = stmt.executeQuery(generateNewRecordsCountSql(source.getConnectionDetail().getSchema().toUpperCase(), target.getConnectionDetail().getSchema().toUpperCase(), refTbl,
				primaryKeys, tgtTbl.getColumns()));
		if (rs.next()) {
			cnt = rs.getInt(1);
		}
		rs.close();
		return cnt;
	}

	private String generateNewRecordsSql(String srcSchema, String tgtSchema, String tableName, List<String> pkCols, List<ColumnDescriptor> dataCols) {
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("insert into ");
		sqlBuf.append(srcSchema + "." + tableName);
		sqlBuf.append("(");
		for (int i = 0; i < dataCols.size() && i < dataCols.size(); i++) {
			sqlBuf.append(dataCols.get(i).getName());
			if (i < dataCols.size() - 1) {
				sqlBuf.append(", ");
			}
		}
		sqlBuf.append(") ");
		sqlBuf.append("select ");
		for (int i = 0; i < dataCols.size() && i < dataCols.size(); i++) {
			sqlBuf.append("A.");
			sqlBuf.append(dataCols.get(i).getName());
			if (i < dataCols.size() - 1) {
				sqlBuf.append(", ");
			}
		}
		sqlBuf.append(" from ");
		sqlBuf.append(tgtSchema + "." + tableName);
		sqlBuf.append(" A where not exists (select 1 from ");
		sqlBuf.append(srcSchema + "." + tableName);
		sqlBuf.append(" B where ");
		for (int i = 0; i < pkCols.size() && i < pkCols.size(); i++) {
			sqlBuf.append("B.");
			sqlBuf.append(pkCols.get(i));
			sqlBuf.append("= A.");
			sqlBuf.append(pkCols.get(i));
			if (i < pkCols.size() - 1) {
				sqlBuf.append(" and ");
			}
		}
		sqlBuf.append(");");
		return sqlBuf.toString();
	}

	private String generateNewRecordsCountSql(String srcSchema, String tgtSchema, String tableName, List<String> pkCols, List<ColumnDescriptor> dataCols) {
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("select count(1) from ");
		sqlBuf.append(tgtSchema + "." + tableName);
		sqlBuf.append(" A where not exists (select 1 from ");
		sqlBuf.append(srcSchema + "." + tableName);
		sqlBuf.append(" B where ");
		for (int i = 0; i < pkCols.size() && i < pkCols.size(); i++) {
			sqlBuf.append("B.");
			sqlBuf.append(pkCols.get(i));
			sqlBuf.append("= A.");
			sqlBuf.append(pkCols.get(i));
			if (i < pkCols.size() - 1) {
				sqlBuf.append(" and ");
			}
		}
		sqlBuf.append(")");
		return sqlBuf.toString();
	}

	/**
	 * @param ojbMap
	 * @param source
	 * @param allTables2
	 * @return
	 */
	private HashSet<String> getReferenceTables(OJBMap ojbMap, DBMap source, List<String> allTables2) {
		HashSet<String> excludes = new HashSet<String>();
		for (String td : allTables2) {
			List<String> ordr = new SchemaJoinMetaData(source, ojbMap).defineOrder(td);
			for (String string : ordr) {
				if (source.getTableDescriptor(string).excludeFromDataCompare()) {
					excludes.add(td);
				}
			}
		}
		HashSet<String> refTables = new HashSet<String>();
		for (String td : allTables2) {
			if (!excludes.contains(td)) {
				// add to reference table list
				refTables.add(td);
			}
		}
		return refTables;
	}

	public List<ConnectionDetail> getConnections() {
		prepareConnectionList();
		return connections;
	}

	public void setConnections(List<ConnectionDetail> connections) {
		this.connections = connections;
	}

	public String getTargetDb() {
		return targetDb;
	}

	public void setTargetDb(String stagingDb) {
		this.targetDb = stagingDb;
	}
}
