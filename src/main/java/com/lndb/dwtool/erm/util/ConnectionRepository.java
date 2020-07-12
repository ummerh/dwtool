package com.lndb.dwtool.erm.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;

public class ConnectionRepository {
    private static final String PROP_PWD = "pwd";
    private static final String PROP_USER = "user";
    private static final String PROP_SCHEMA = "schema";
    private static final String PROP_URL = "url";
    private static final String PROP_DRIVER = "driver";
    private static final String PROPERTY_CONNECTIONS = "connections";
    private static String REPOSITORY_PATH = Configuration.getProperty("connectionRepositoryFile");
    private static Properties properties;
    public static Hashtable<String, Integer> FAILED_ATTEMPTS = new Hashtable<String, Integer>();
    public static HashSet<String> IN_PROGRESS = new HashSet<String>();

    static {
	loadProperties();
    }

    public static synchronized Properties loadProperties() {
	properties = new Properties();
	try {
	    if (!new File(REPOSITORY_PATH).exists()) {
		new File(new File(REPOSITORY_PATH).getParent()).mkdirs();
		new File(REPOSITORY_PATH).createNewFile();
	    }
	    properties.load(new FileInputStream(REPOSITORY_PATH));
	} catch (Exception e) {
	    throw new RuntimeException();
	}
	return properties;
    }

    public static List<ConnectionDetail> getAllConnections() {
	String conList = properties.getProperty(PROPERTY_CONNECTIONS);
	if (conList == null) {
	    return new ArrayList<ConnectionDetail>();
	}
	String[] connections = conList.split(",", -1);
	List<ConnectionDetail> allCons = new ArrayList<ConnectionDetail>();
	for (String con : connections) {
	    if (con != null && con.trim().length() > 0) {
		allCons.add(ConnectionDetail.configure(con));
	    }
	}
	return allCons;
    }

    public static synchronized void saveConnection(String conName, String[] entries) throws IOException {
	String conNameList = properties.getProperty(PROPERTY_CONNECTIONS);
	if (conNameList == null) {
	    conNameList = conName;
	} else {
	    String[] split = conNameList.split(",");
	    if (!Arrays.asList(split).contains(conName)) {
		conNameList = conNameList.concat((conNameList.endsWith(",") ? "" : ",") + conName);
	    }
	}
	properties.put(PROPERTY_CONNECTIONS, conNameList);
	properties.put(conName + "." + PROP_DRIVER, entries[0]);
	properties.put(conName + "." + PROP_URL, entries[1]);
	properties.put(conName + "." + PROP_SCHEMA, entries[2]);
	properties.put(conName + "." + PROP_USER, entries[3]);
	properties.put(conName + "." + PROP_PWD, DESEncryptor.encrypt(entries[4]));

	BufferedWriter writer = new BufferedWriter(new FileWriter(REPOSITORY_PATH));
	writer.write(PROPERTY_CONNECTIONS + " = " + conNameList);
	writer.newLine();
	writer.newLine();
	rewritePropertiesFile(writer);
	writer.flush();
	writer.close();
    }

    private static void rewritePropertiesFile(BufferedWriter writer) throws IOException {
	String[] connections = properties.getProperty(PROPERTY_CONNECTIONS).split(",", -1);
	for (String con : connections) {
	    if (con != null && con.trim().length() > 0) {
		writer.write(con + "." + PROP_DRIVER + " = " + properties.getProperty(con + "." + PROP_DRIVER));
		writer.newLine();
		writer.write(con + "." + PROP_URL + " = " + properties.getProperty(con + "." + PROP_URL));
		writer.newLine();
		writer.write(con + "." + PROP_SCHEMA + " = " + properties.getProperty(con + "." + PROP_SCHEMA));
		writer.newLine();
		writer.write(con + "." + PROP_USER + " = " + properties.getProperty(con + "." + PROP_USER));
		writer.newLine();
		writer.write(con + "." + PROP_PWD + " = " + properties.getProperty(con + "." + PROP_PWD));
		writer.newLine();
		writer.newLine();
	    }
	}
    }

    public static synchronized void deleteConnection(String conName) throws IOException {
	String[] cons = properties.getProperty(PROPERTY_CONNECTIONS).split(",", -1);
	String conNameList = removeConnectionName(conName, cons);
	if (conName != null && conName.trim().length() > 0) {
	    BufferedWriter writer = new BufferedWriter(new FileWriter(REPOSITORY_PATH));
	    if (conNameList != null && conNameList.length() > 0) {
		writer.write("connections = " + conNameList);
		writer.newLine();
		writer.newLine();
	    }
	    properties.put(PROPERTY_CONNECTIONS, conNameList);
	    properties.remove(conName + "." + PROP_DRIVER);
	    properties.remove(conName + "." + PROP_URL);
	    properties.remove(conName + "." + PROP_SCHEMA);
	    properties.remove(conName + "." + PROP_USER);
	    properties.remove(conName + "." + PROP_PWD);
	    rewritePropertiesFile(writer);
	    writer.flush();
	    writer.close();
	}
    }

    private static String removeConnectionName(String conName, String[] cons) {
	String conNameList = "";
	int pos = 0;
	for (String string : cons) {
	    if (!string.equals(conName) && string.length() > 0) {
		conNameList += string;
		if (pos < cons.length - 1) {
		    conNameList += ",";
		}
	    }
	    pos++;
	}
	return conNameList;
    }

    public static void validateConnections(List<ConnectionDetail> connections) {
	for (final ConnectionDetail conDetail : connections) {
	    try {
		Integer attempt = FAILED_ATTEMPTS.get(conDetail.getName());
		if (attempt == null || attempt.intValue() < 2) {
		    Thread newThread = new Thread() {
			@Override
			public void run() {
			    try {
				validateConnection(conDetail);
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			}
		    };
		    newThread.start();
		}
	    } catch (Exception e) {
		// do nothing
	    }
	}
    }

    public static Connection validateConnection(ConnectionDetail conDetail) {
	Connection newConnection = null;
	try {
	    if (IN_PROGRESS.add(conDetail.getName())) {
		newConnection = DatabaseConnection.newConnection(conDetail);
		if (newConnection != null && !newConnection.isClosed()) {
		    Statement stmt = newConnection.createStatement();
		    if (conDetail.getDriver().toLowerCase().contains("oracle")) {
			stmt.execute("select 1 from dual");
			conDetail.setValid(true);
		    } else {
			// stmt.execute("select 1 from dual");
			conDetail.setValid(true);
		    }
		    newConnection.close();
		    FAILED_ATTEMPTS.remove(conDetail.getName());
		} else {
		    conDetail.setValid(false);
		}
	    }
	} catch (Exception e) {
	    Integer attempt = FAILED_ATTEMPTS.get(conDetail.getName());
	    if (attempt == null) {
		attempt = 0;
	    }
	    FAILED_ATTEMPTS.put(conDetail.getName(), ++attempt);
	    System.out.println("CONNECTION FAILED ATTEMPT FOR " + conDetail.getName() + " = " + attempt);
	    conDetail.setValid(false);
	    throw new RuntimeException(e);
	} finally {
	    try {
		IN_PROGRESS.remove(conDetail.getName());
		DatabaseConnection.release(newConnection);
	    } catch (SQLException e) {
		throw new RuntimeException(e);
	    }
	}
	return newConnection;

    }
}
