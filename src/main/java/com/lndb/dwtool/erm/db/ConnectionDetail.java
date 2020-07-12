package com.lndb.dwtool.erm.db;

import java.util.Properties;

import com.lndb.dwtool.erm.ddl.Dialect;
import com.lndb.dwtool.erm.ddl.HsqlDialect;
import com.lndb.dwtool.erm.ddl.MySqlDialect;
import com.lndb.dwtool.erm.ddl.OracleDialect;
import com.lndb.dwtool.erm.ddl.SqlServerDialect;
import com.lndb.dwtool.erm.util.ConnectionRepository;
import com.lndb.dwtool.erm.util.DESEncryptor;

public class ConnectionDetail {
	private String url;
	private String userId;
	private String password;
	private String schema;
	private String catalog;
	private String driver;
	private String name;
	private boolean valid = true;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrlDisplay() {
		if (getUrl() != null && getUrl().length() > 40) {
			return getUrl().substring(0, 40).concat("...");
		}
		return getUrl();
	}

	public static ConnectionDetail configure(String con) {
		Properties props = ConnectionRepository.loadProperties();
		ConnectionDetail connectionDetail = new ConnectionDetail();
		connectionDetail.setName(con);
		connectionDetail.setDriver(props.getProperty(con + ".driver"));
		connectionDetail.setUrl(props.getProperty(con + ".url"));
		connectionDetail.setSchema(props.getProperty(con + ".schema"));
		connectionDetail.setUserId(props.getProperty(con + ".user"));
		connectionDetail.setPassword(DESEncryptor.decrypt(props.getProperty(con
				+ ".pwd")));
		return connectionDetail;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getHighlight() {
		Integer integer = ConnectionRepository.FAILED_ATTEMPTS.get(getName());
		if (integer == null || integer == 0) {
			return "";
		}
		return "error";
	}

	public Dialect getDialect() {
		if (getDriver().toLowerCase().contains("oracle")) {
			return new OracleDialect();
		}
		if (getDriver().toLowerCase().contains("jtds")) {
			return new SqlServerDialect();
		}
		if (getDriver().toLowerCase().contains("hsql")) {
			return new HsqlDialect();
		}
		if (getDriver().toLowerCase().contains("mysql")) {
			return new MySqlDialect();
		}
		return new OracleDialect();
	}
}
