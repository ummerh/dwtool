package com.lndb.dwtool.erm.manager.action;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.lndb.dwtool.erm.DependencyLevelMap;
import com.lndb.dwtool.erm.RelationalMap;
import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.code.generator.JunitFixtureGenerator;
import com.lndb.dwtool.erm.code.generator.PojoGenerator;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.ojb.ClassDescriptor;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.util.FileUtil;
import com.lndb.dwtool.erm.util.JavaSourceUtil;
import com.lndb.dwtool.erm.util.StatusMonitor;
import com.lndb.dwtool.erm.util.StringUtil;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action class that support all DB + OJB Entity Relationship views/reports
 * 
 * @author harsha07
 * 
 */
public class DbErActions extends ActionSupport {

	private static final String AUTO_UPDATE_REFERENCES = "autoUpdateReferences";
	private static final String ALL_TABLES2 = "allTables2";
	private static final String ALL_TABLES = "allTables";
	private static final String DEPENDENCY_MAP = "dependencyMap";
	private static final String LOAD_ORDER = "loadOrder";
	private static final long serialVersionUID = 3342405811908769871L;
	private String connectionName;
	private String tableName;
	private List<TableDescriptor> allTables;
	private List<String> orderedTables;
	private DependencyLevelMap dependencyMap;
	private Map<String, String> dataStatus;
	private String warningMessage;
	private List<String> autoUpdateableReferences;

	public String getWarningMessage() {
		return warningMessage;
	}

	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}

	/**
	 * Action method scans through all the tables and data within it, reports any violation of relationships (DB+OJB) is reported as error
	 * 
	 * @return
	 * @throws Exception
	 */
	public String checkDataIntegrity() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
		Connection newConnection = DatabaseConnection.newConnection(dbMap.getConnectionDetail());
		this.dataStatus = joinMetaData.checkDataIntegrity(newConnection);
		DatabaseConnection.release(newConnection);
		return "dataStatus";
	}

	/**
	 * Action method scans data within a specific table for any data ER violation
	 * 
	 * @return
	 * @throws Exception
	 */
	public String checkDataIntegrityByTable() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
		Connection newConnection = DatabaseConnection.newConnection(dbMap.getConnectionDetail());
		String tableDataStatus = joinMetaData.checkDataIntegrity(newConnection, getTableName());
		this.dataStatus = new TreeMap<String, String>();
		this.dataStatus.put(getTableName(), tableDataStatus);
		DatabaseConnection.release(newConnection);
		return "dataStatus";
	}

	/**
	 * Action method displays the table list ordered by their happy load order path
	 * 
	 * @return
	 * @throws Exception
	 */
	public String defineDBLoadOrder() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
		this.orderedTables = joinMetaData.defineOrder();
		return LOAD_ORDER;
	}

	@SuppressWarnings("unused")
	private void tempCreateFISToKFS() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("/Temp/fis-kfs-template.sql"));
			for (String tableName : this.orderedTables) {
				if (tableName.startsWith("CM_")) {
					Connection con = DatabaseConnection.newConnection(ConnectionDetail.configure("kuldemo"));
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery("select column_name from user_tab_cols where table_name='" + tableName + "'");
					StringBuffer to = new StringBuffer();
					StringBuffer from = new StringBuffer();
					to.append("insert into kfs." + tableName + " (");
					from.append("select ");
					if (rs != null && rs.next()) {
						to.append(rs.getString(1));
						from.append(rs.getString(1));
					}
					while (rs != null && rs.next()) {
						to.append(", " + rs.getString(1));
						from.append(", " + rs.getString(1));
					}
					to.append(") ");
					from.append(" from fis." + tableName + " ;");
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					writer.append(to.toString());
					writer.newLine();
					writer.append(from.toString());
					writer.newLine();
				}
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {

		}
	}

	@SuppressWarnings("unused")
	private void tempPrintDbOrder() throws ClassNotFoundException, SQLException {
		Connection con = DatabaseConnection.newConnection(ConnectionDetail.configure("kuldbx"));
		List<String> tables = new ArrayList<String>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select table_name from user_tables");
		while (rs != null && rs.next()) {
			tables.add(rs.getString(1));
		}
		System.out.println("BEGIN");
		for (String tableName : this.orderedTables) {
			if (tables.contains(tableName)) {
				System.out.println(tableName);
			}
		}
		System.out.println("END");
	}

	/**
	 * Action method displays table dependency information for a specific table
	 * 
	 * @return
	 * @throws Exception
	 */
	public String defineTableDependency() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		RelationalMap dbMap = DBMapCache.getDBMap(getConnectionName());
		SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
		dependencyMap = new DependencyLevelMap();
		dependencyMap.load(joinMetaData, tableName);
		return DEPENDENCY_MAP;
	}

	/**
	 * Action method display happy load order path for a specific table
	 * 
	 * @return
	 * @throws Exception
	 */
	public String defineTableLoadOrder() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
		this.orderedTables = joinMetaData.defineOrder(getTableName());
		return LOAD_ORDER;
	}

	/**
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String downloadPojo() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/java");
		response.setHeader("Content-disposition", "attachment; filename=\"" + JavaSourceUtil.buildJavaClassName(getTableName(), "_") + ".java" + "\"");
		PrintWriter out = response.getWriter();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		TableDescriptor tableDescriptor = dbMap.getTableDescriptor(getTableName());
		PojoGenerator.toPojo(tableDescriptor, out);
		return LOAD_ORDER;
	}

	/**
	 * Action method display happy load order path for a specific table
	 * 
	 * @return
	 * @throws Exception
	 */
	public String displayAutoUpdateableReferences() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		this.autoUpdateableReferences = new ArrayList<String>(ojbMap.getAutoUpdateableReferences(getTableName()));
		return AUTO_UPDATE_REFERENCES;
	}

	/**
	 * Action method displays the header page within which all tables are displayed. This page is split so that loading delay message can be displayed and a scrollable IFRAME can be provided to the
	 * user
	 * 
	 * @return
	 * @throws Exception
	 */
	public String displayAllTables() throws Exception {
		if (!DBMapCache.isLoaded(getConnectionName())) {
			StatusMonitor.removeStatus(getConnectionName());
			StatusMonitor.removeProgressInd(getConnectionName());
			setWarningMessage("Page loading may take several minutes while loading the DB definition for the first time. Please wait....");

		}
		return ALL_TABLES;
	}

	/**
	 * Action method that displays the list of tables within the IFRAME
	 * 
	 * @return
	 * @throws Exception
	 */
	public String displayAllTables2() throws Exception {
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		this.allTables = dbMap.getAllTableDescriptors();
		// writeOneByOne();
		return ALL_TABLES2;
	}

	@Override
	public String execute() throws Exception {
		return super.execute();
	}

	public List<TableDescriptor> getAllTables() {
		return allTables;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public Map<String, String> getDataStatus() {
		return dataStatus;
	}

	public DependencyLevelMap getDependencyMap() {
		return dependencyMap;
	}

	public List<String> getOrderedTables() {
		return orderedTables;
	}

	public String getTableName() {
		return tableName;
	}

	/**
	 * Action method that prints data integrity error records found within a specific table
	 * 
	 * @return
	 * @throws Exception
	 */
	public String printDataIntegrityErrors() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/txt");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getTableName() + "_errors.txt" + "\"");
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
		PrintWriter writer = response.getWriter();
		Connection newConnection = DatabaseConnection.newConnection(dbMap.getConnectionDetail());
		joinMetaData.printDataIntegrityErrors(newConnection, new BufferedWriter(writer), getTableName());
		writer.flush();
		writer.close();
		DatabaseConnection.release(newConnection);
		return null;
	}

	public void setAllTables(List<TableDescriptor> allTables) {
		this.allTables = allTables;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public void setDataStatus(Map<String, String> dataStatus) {
		this.dataStatus = dataStatus;
	}

	public void setDependencyMap(DependencyLevelMap dependencyMap) {
		this.dependencyMap = dependencyMap;
	}

	public void setOrderedTables(List<String> orderedTables) {
		this.orderedTables = orderedTables;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the autoUpdateableReferences
	 */
	public List<String> getAutoUpdateableReferences() {
		return autoUpdateableReferences;
	}

	/**
	 * @param autoUpdateableReferences
	 *            the autoUpdateableReferences to set
	 */
	public void setAutoUpdateableReferences(List<String> autoUpdateableReferences) {
		this.autoUpdateableReferences = autoUpdateableReferences;
	}

	/**
	 * Action method that supports junit test data fixture generator
	 * 
	 * @return
	 * @throws Exception
	 */
	public String downloadJunitFixture() throws Exception {
		ClassDescriptor classDescriptor = OJBMapCache.getOJBMap().getClassDescriptor(getTableName());
		if (classDescriptor != null) {
			String className = classDescriptor.getClassName().substring(classDescriptor.getClassName().lastIndexOf('.') + 1);
			InputStreamReader fixtureCode = JunitFixtureGenerator.generateFixtureCode(getConnectionName(), getTableName());
			HttpServletResponse response = ServletActionContext.getResponse();
			response.setContentType("text/java");
			response.setHeader("Content-disposition", "attachment; filename=\"" + className + "Fixture.java" + "\"");
			PrintWriter out = response.getWriter();
			FileUtil.write(fixtureCode, out);
		}
		return null;
	}

	public String downloadOrderSql() throws Exception {
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		SchemaJoinMetaData joinMetaData = new SchemaJoinMetaData(dbMap, ojbMap);
		this.orderedTables = joinMetaData.defineOrder();

		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/java");
		response.setHeader("Content-disposition", "attachment; filename=\"table_order.sql\"");
		PrintWriter out = response.getWriter();
		int pos = 1;
		out.write("create table tbl_data_load_order(load_pos NUMBER, table_name VARCHAR2(100));");
		out.write(StringUtil.LINE_BREAK);
		for (String tblName : this.orderedTables) {
			out.write("insert into tbl_data_load_order(load_pos, table_name) values(" + pos++ + ",'" + tblName + "');");
			out.write(StringUtil.LINE_BREAK);
		}
		out.write("commit;");
		out.write(StringUtil.LINE_BREAK);
		return null;
	}

}
