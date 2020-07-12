package com.lndb.dwtool.erm.ddl;

public class HsqlDialect extends SqlServerDialect {

    public String getTableCountSql(String schemaName) {
	return "select count(1) from information_schema.system_tables";
    }
}
