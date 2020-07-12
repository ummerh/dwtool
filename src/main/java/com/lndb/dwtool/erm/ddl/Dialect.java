package com.lndb.dwtool.erm.ddl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.SequenceInfo;
import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;

public interface Dialect {

    public String dropObjectDDL(String objectType, String objectName);

    public String simpleObjectsSql(String objectType);

    public String addConstraintDDL(String tblName, String fkName, ForeignKey foreignKey);

    public String inlinePKConstraint(String constraintName, List<String> primaryKeys);

    public String columnSpec(TableDescriptor tableDescriptor);

    public String getSqlDataType(ColumnDescriptor columnDescriptor);

    public String getJavaDataType(ColumnDescriptor columnDescriptor);

    public String getTableCountSql(String schemaName);

    public Object readData(ColumnDescriptor columnDescriptor, ResultSet rs) throws SQLException;

    public String generateTableDiffDLL(TableDescriptor src, TableDescriptor tgt);
    
    public String columnSpec(ColumnDescriptor columnDescriptor);
    
    public String createSequence(SequenceInfo sequence);

	public String buildDropUserObjectsSql(Connection con);

}