package com.lndb.dwtool.erm.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DBMetadataDefinitionPrinter {
	public static void printMetadataDefinition(ConnectionDetail connectionDetail) throws ClassNotFoundException, SQLException {
		Connection dbConnection = DatabaseConnection.newConnection(connectionDetail);
		DatabaseMetaData metaData = dbConnection.getMetaData();
		ResultSet rs = metaData.getTables(connectionDetail.getSchema(), connectionDetail.getSchema(), "KRIM_", new String[] { "TABLE" });
		printMetadata("TABLE", rs);
		rs = metaData.getColumns(connectionDetail.getCatalog(), connectionDetail.getSchema(), "KRIM_", null);
		while (rs.next()) {
			System.out.println(rs.getString("DATA_TYPE") + " - " + rs.getString("DATA_TYPE") + "-" + rs.getString("SQL_DATA_TYPE") + "-"
					+ rs.getString("SQL_DATETIME_SUB"));
		}
		printMetadata("COLUMN", rs);
		rs = metaData.getPrimaryKeys(connectionDetail.getCatalog(), connectionDetail.getSchema(), "KRIM_");
		printMetadata("PK", rs);
		rs = metaData.getImportedKeys(connectionDetail.getCatalog(), connectionDetail.getSchema(), "KRIM_");
		printMetadata("FK", rs);

		rs = metaData.getExportedKeys(connectionDetail.getCatalog(), connectionDetail.getSchema(), "KRIM_");
		printMetadata("EXPORT", rs);

		rs = metaData.getIndexInfo(connectionDetail.getCatalog(), connectionDetail.getSchema(), "KRIM_PERM_T", true, false);
		printMetadata("INDEX", rs);
		DatabaseConnection.release(dbConnection);
	}

	static void printMetadata(String header, ResultSet resultSet) throws SQLException {
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		System.out.println("BEGIN -------" + header);
		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			System.out.println(resultSetMetaData.getColumnName(i) + " - " + resultSetMetaData.getColumnTypeName(i));
		}
		System.out.println("END -------" + header);
		System.out.println();
	}

	static void printResultData(String header, ResultSet resultSet) throws SQLException {
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		System.out.println("BEGIN -------" + header);
		while (resultSet.next()) {
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				System.out.print(resultSetMetaData.getColumnName(i) + " = " + resultSet.getString(resultSetMetaData.getColumnName(i))
						+ " ,");
			}
			System.out.println();
		}
		System.out.println("END -------" + header);
		System.out.println();
	}

	public static void main(String[] args) {
		try {

			ConnectionDetail connectionDetail = prepareConnectionDetails();
			Connection dbConnection = DatabaseConnection.newConnection(connectionDetail);
			DatabaseMetaData metaData = dbConnection.getMetaData();
			ResultSet rs = metaData.getIndexInfo(connectionDetail.getCatalog(), connectionDetail.getSchema(), "KRIM_PERM_T", false, false);
			printResultData("INDEX", rs);
			DatabaseConnection.release(dbConnection);
			// printMetadataDefinition(prepareConnectionDetails());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ConnectionDetail prepareConnectionDetails() {
		ConnectionDetail connectionDetail = new ConnectionDetail();
		connectionDetail.setDriver("oracle.jdbc.OracleDriver");
		connectionDetail.setSchema("KFS5R2");
		connectionDetail.setUrl("jdbc:oracle:thin:@localhost:1521:ORCL");
		connectionDetail.setUserId("kfs5r2");
		return connectionDetail;
	}
}
