package com.lndb.dwtool.erm.ddl;

import java.util.List;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.IndexInfo;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;

public class DDL {
    private static final OracleDialect oracleDialect = new OracleDialect();

    public static String addConstraintDDL(String tblName, String fkName, ForeignKey foreignKey) {
	return oracleDialect.addConstraintDDL(tblName, fkName, foreignKey);
    }
    
    public static String dropConstraintDDL(String tblName, String name) {
	return oracleDialect.dropConstraintDDL(tblName,name);
    }

    public static String inlinePKConstraint(String constraintName, List<String> primaryKeys) {
	return oracleDialect.inlinePKConstraint(constraintName, primaryKeys);
    }

    public static String columnSpec(TableDescriptor tableDescriptor) {
	return oracleDialect.columnSpec(tableDescriptor);
    }

    public static String getSqlDataType(ColumnDescriptor columnDescriptor) {
	return oracleDialect.getSqlDataType(columnDescriptor);
    }

    public static String addIndexDDL(String tblName, String indexName, IndexInfo index) {
	return oracleDialect.addIndexDDL(tblName, indexName, index);
    }

    public static String addUniqueDDL(String tblName, String indexName, IndexInfo index) {
	return oracleDialect.addUniqueDDL(tblName, indexName, index);
    }

}
