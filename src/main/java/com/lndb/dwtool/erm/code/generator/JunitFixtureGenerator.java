package com.lndb.dwtool.erm.code.generator;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import com.lndb.dwtool.erm.db.ConnectionDetail;
import com.lndb.dwtool.erm.db.DatabaseConnection;
import com.lndb.dwtool.erm.ojb.ClassDescriptor;
import com.lndb.dwtool.erm.ojb.FieldDescriptor;
import com.lndb.dwtool.erm.ojb.OJBMapCache;
import com.lndb.dwtool.erm.util.StringUtil;

import oracle.sql.TIMESTAMP;

public class JunitFixtureGenerator {
    public static InputStreamReader generateFixtureCode(String connectionName, String tableName) {
	StringBuilder builder = new StringBuilder();
	try {
	    ClassDescriptor classDescriptor = OJBMapCache.getOJBMap().getClassDescriptor(tableName);
	    String className = classDescriptor.getClassName().substring(classDescriptor.getClassName().lastIndexOf('.') + 1);

	    List<FieldDescriptor> fieldDescriptors = classDescriptor.getFieldDescriptors();

	    // get first 50 records from the table
	    Connection con = DatabaseConnection.newConnection(ConnectionDetail.configure(connectionName));
	    Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("select * from " + tableName + " where rownum < 50");
	    // fdoc_nbr in
	    // ('11','12','13','21','22','23','31','32','33','34','35','36','41','51','52')
	    // RTE_NODE_ID in (21,22,23)
	    // RTE_NODE_INSTN_ID in (21,22,23)
	    int count = 0;
	    builder.append("import " + classDescriptor.getClassName() + ";");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("public enum " + className + "Fixture {");

	    while (rs.next()) {
		builder.append(StringUtil.LINE_BREAK);
		count++;
		if (count > 1) {
		    builder.append(",");
		    builder.append(StringUtil.LINE_BREAK);
		}
		builder.append("REC" + count + "{");
		builder.append(StringUtil.LINE_BREAK);
		builder.append("@Override");
		builder.append(StringUtil.LINE_BREAK);
		builder.append("public " + className + " newRecord() {");
		builder.append(StringUtil.LINE_BREAK);
		builder.append("" + className + " obj = new " + className + "();");
		builder.append(StringUtil.LINE_BREAK);
		for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
		    String column = fieldDescriptor.getColumn();
		    Object val = rs.getObject(column);
		    try {
			if (val != null && !"OBJ_ID".equalsIgnoreCase(column) && !"VER_NBR".equalsIgnoreCase(column)) {
			    builder.append("obj.set" + fieldDescriptor.getName().substring(0, 1).toUpperCase() + fieldDescriptor.getName().substring(1)
				    + getTypeSpecificStr(val, fieldDescriptor.getName()));
			    builder.append(StringUtil.LINE_BREAK);
			}
		    } catch (Exception e) {
		    }
		}
		builder.append("return obj;");
		builder.append(StringUtil.LINE_BREAK);
		builder.append("};");
		builder.append(StringUtil.LINE_BREAK);
		builder.append(" }");
	    }
	    builder.append(";");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("public abstract " + className + " newRecord();");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("public static void setUpData() {");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("businessObjectService.save(getAll());");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("}");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("private static List<" + className + "> getAll() {");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("List<" + className + "> recs = new ArrayList<" + className + ">();");
	    builder.append(StringUtil.LINE_BREAK);
	    for (int i = 1; i <= count; i++) {
		builder.append("recs.add(REC" + i + ".newRecord());");
		builder.append(StringUtil.LINE_BREAK);
	    }
	    builder.append("return recs;");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("}");
	    builder.append(StringUtil.LINE_BREAK);
	    builder.append("}");
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return new InputStreamReader(new ByteArrayInputStream(builder.toString().getBytes()));
    }

    private static String getTypeSpecificStr(Object val, String fieldName) {
	String field = fieldName.toLowerCase();
	if (val == null) {
	    return "(null);";
	} else if (Date.class.isAssignableFrom(val.getClass()) || TIMESTAMP.class.isAssignableFrom(val.getClass())) {
	    return "(new java.sql.Timestamp(new Date().getTime()));";
	} else if (Number.class.isAssignableFrom(val.getClass())) {
	    if (field.contains("amount") || field.contains("quantity"))
		return "(new KualiDecimal(" + val + "));";
	    else if (field.contains("price") || field.contains("percent"))
		return "(new BigDecimal(" + val + "));";
	} else if (String.class.isAssignableFrom(val.getClass())) {
	    if (field.contains("indicator") || field.contains("active")) {
		return "(" + ("Y".equals(val) ? "true" : "false") + ");";
	    }
	    return "(\"" + val + "\");";
	}
	return "(" + val + ");";
    }
}
