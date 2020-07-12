package com.lndb.dwtool.erm.code.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.ddl.OracleDialect;
import com.lndb.dwtool.erm.util.JavaSourceUtil;
import com.lndb.dwtool.erm.util.StringUtil;

public class PojoGenerator {
    private static final OracleDialect oracleDialect = new OracleDialect();

    public static void toPojo(TableDescriptor tableDescriptor, Writer writer) throws IOException {
	BufferedWriter bufWriter = new BufferedWriter(writer);
	bufWriter.write(toPojo(tableDescriptor));
	bufWriter.flush();
	bufWriter.close();
    }

    public static String toPojo(TableDescriptor tableDescriptor) {
	StringBuilder builder = new StringBuilder();
	builder.append("public class ");
	builder.append(JavaSourceUtil.buildJavaClassName(tableDescriptor.getTableName(), "_"));
	builder.append("{");
	builder.append(StringUtil.LINE_BREAK);
	for (ColumnDescriptor column : tableDescriptor.getColumns()) {
	    builder.append(toPojoField(column));
	    builder.append(StringUtil.LINE_BREAK);
	}
	for (ColumnDescriptor column : tableDescriptor.getColumns()) {
	    builder.append(toPojoGetterSetters(column));
	    builder.append(StringUtil.LINE_BREAK);
	}
	builder.append(StringUtil.LINE_BREAK);
	builder.append("}");
	return builder.toString();
    }

    public static String toPojoField(ColumnDescriptor column) {
	StringBuilder builder = new StringBuilder();
	builder.append("private ");
	builder.append(oracleDialect.getJavaDataType(column));
	builder.append(" ");
	builder.append(JavaSourceUtil.buildJavaFieldName(column.getName(), "_"));
	builder.append(";");
	return builder.toString();
    }

    public static String toPojoGetterSetters(ColumnDescriptor column) {
	StringBuilder builder = new StringBuilder();
	builder.append("public ");
	String javaDataType = oracleDialect.getJavaDataType(column);
	builder.append(javaDataType);
	String pojoField = JavaSourceUtil.buildJavaFieldName(column.getName(), "_");
	builder.append(" get" + Character.toTitleCase(pojoField.charAt(0)) + pojoField.substring(1) + "(){");
	builder.append(StringUtil.LINE_BREAK);
	builder.append("return this.");
	builder.append(pojoField);
	builder.append(";");
	builder.append(StringUtil.LINE_BREAK);
	builder.append("}");
	builder.append(StringUtil.LINE_BREAK);
	builder.append("public void ");
	builder.append(" set" + Character.toTitleCase(pojoField.charAt(0)) + pojoField.substring(1));
	builder.append("(");
	builder.append(javaDataType);
	builder.append(" ");
	builder.append(pojoField);
	builder.append("){");
	builder.append(StringUtil.LINE_BREAK);
	builder.append(StringUtil.TAB);
	builder.append("this.");
	builder.append(pojoField);
	builder.append("=");
	builder.append(pojoField);
	builder.append(";");
	builder.append(StringUtil.LINE_BREAK);
	builder.append("}");
	return builder.toString();
    }
}
