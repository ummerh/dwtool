package com.lndb.dwtool.erm.manager.action;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.lndb.dwtool.erm.DifferenceMap;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.ddl.DDLGenerator;
import com.lndb.dwtool.erm.ddl.Dialect;
import com.lndb.dwtool.erm.ddl.OracleDialect;
import com.lndb.dwtool.erm.hive.HiveDDLGenerator;
import com.lndb.dwtool.erm.ojb.ClassDescriptor;
import com.lndb.dwtool.erm.ojb.FieldDescriptor;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.util.ConnectionRepository;
import com.opensymphony.xwork2.ActionSupport;

public class ERActions extends ActionSupport {
	private String connectionName;
	private List<ConnectionDetail> connections = new ArrayList<ConnectionDetail>();
	private DifferenceMap differenceMap;
	private static final OracleDialect oracleDialect = new OracleDialect();

	private static final long serialVersionUID = 2439743718758492237L;

	public String displayMain() throws Exception {
		this.connections = ConnectionRepository.getAllConnections();
		return SUCCESS;
	}

	public String downloadOJBRelationships() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/notepad");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getConnectionName() + "_ojb.txt" + "\"");
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		ojbMap.writeMap(response.getWriter());
		return null;
	}

	public String downloadDBRelationships() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/notepad");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getConnectionName() + "_db.txt" + "\"");
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		dbMap.writeMap(response.getWriter());
		return null;
	}

	public String downloadDDL() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/notepad");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getConnectionName() + "_ddl.sql" + "\"");
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DDLGenerator ddlGenerator = new DDLGenerator(ojbMap, dbMap);
		ddlGenerator.writeSchemaDDL(response.getWriter());
		return null;
	}

	public String dropSchemaDDL() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/notepad");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getConnectionName() + "_drop_ddl.sql" + "\"");
		ConnectionDetail detail = ConnectionDetail.configure(getConnectionName());
		Dialect dialect = detail.getDialect();
		String sql = dialect.buildDropUserObjectsSql(DatabaseConnection.newConnection(detail));
		response.getWriter().write(sql);
		return null;

	}

	public String ojbMappingErrors() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/xls");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getConnectionName() + "_mapping_errors.csv" + "\"");
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		PrintWriter writer = response.getWriter();
		List<String> errors = ojbMap.validateAgainstDBSchema(dbMap, null);
		for (String string : errors) {
			writer.println(string);
		}
		writer.flush();
		writer.close();
		return null;

	}

	@SuppressWarnings("unused")
	private void tempPrintFullMap() {
		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\temp\\db-ojb-details.txt"));
			DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
			OJBMap ojbMap = OJBMapCache.getOJBMap();
			List<String> allTables = dbMap.getAllTables();
			for (String tableName : allTables) {
				TableDescriptor tableDescriptor = dbMap.getTableDescriptor(tableName);
				ClassDescriptor classDescriptor = ojbMap.getClassDescriptor(tableName);
				if (tableDescriptor != null && classDescriptor != null) {
					List<FieldDescriptor> fieldDescriptors = classDescriptor.getFieldDescriptors();
					writer.write("Table:" + tableName + " BO:" + classDescriptor.getClassName());
					writer.newLine();
					for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
						ColumnDescriptor columnDescriptor = tableDescriptor.getColumn(fieldDescriptor.getColumn());
						String sqlDataType = null;
						if (columnDescriptor == null)
							sqlDataType = "";
						else
							sqlDataType = oracleDialect.getSqlDataType(columnDescriptor);
						writer.write(fieldDescriptor.getColumn() + " : [" + sqlDataType + "] :" + fieldDescriptor.getName());
						writer.newLine();
					}
					writer.write("------------------------------------------");
					writer.newLine();
					writer.newLine();
				}

			}
			writer.flush();
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String downloadOJBDDL() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/notepad");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getConnectionName() + "_ojb_ddl.sql" + "\"");
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		DDLGenerator ddlGenerator = new DDLGenerator(ojbMap, dbMap);
		ddlGenerator.writeOjbDDL(response.getWriter());
		return null;
	}

	public String compareDBToOJB() throws Exception {
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		OJBMap ojbMap = OJBMapCache.getOJBMap();
		this.differenceMap = new DifferenceMap();
		differenceMap.compare(dbMap, ojbMap);
		return "ojbDbDiff";
	}

	public String downloadHiveETLTableDDL() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/notepad");
		response.setHeader("Content-disposition", "attachment; filename=\"" + getConnectionName() + "_hive_ddl.sql" + "\"");
		PrintWriter out = response.getWriter();
		DBMap dbMap = DBMapCache.getDBMap(getConnectionName());
		List<TableDescriptor> tbls = dbMap.getAllTableDescriptors();
		for (TableDescriptor tableDescriptor : tbls) {
			out.println(HiveDDLGenerator.hiveETLTableDDL(tableDescriptor));
		}
		return null;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public List<ConnectionDetail> getConnections() {
		return connections;
	}

	public void setConnections(List<ConnectionDetail> connections) {
		this.connections = connections;
	}

	public DifferenceMap getDifferenceMap() {
		return differenceMap;
	}

	public void setDifferenceMap(DifferenceMap differenceMap) {
		this.differenceMap = differenceMap;
	}

}
