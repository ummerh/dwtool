package com.lndb.dwtool.erm.manager.standalone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.ojb.ClassDescriptor;
import com.lndb.dwtool.erm.ojb.FieldDescriptor;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;

public class PrintORColumns {
    public static void main(String[] args) {
	try {
	    BufferedWriter writer = new BufferedWriter(new FileWriter("/temp/db-pojo-size.txt"));
	    System.out.println("Loading DB Map");
	    DBMap dbMap = DBMapCache.getDBMap("mmdemo");
	    System.out.println("Loading OJB Map");
	    OJBMap ojbMap = OJBMapCache.getOJBMap(new File("C:\\java\\projects\\mm-dev\\src\\java\\org\\kuali\\ext\\mm\\ojb-mm.xml"));
	    Set<String> allClasses = ojbMap.getAllClasses();
	    ArrayList<String> allClassNames = new ArrayList<String>(allClasses);
	    Collections.sort(allClassNames);
	    for (String classNm : allClassNames) {
		// for each class, column, size
		String tblName = ojbMap.getTableNameForClass(classNm);
		TableDescriptor tableDescriptor = dbMap.getTableDescriptor(tblName);
		if (tableDescriptor == null) {
		    continue;
		}
		ClassDescriptor classDescriptor = ojbMap.getClassDescriptor(tblName);
		List<FieldDescriptor> fieldDescriptors = classDescriptor.getFieldDescriptors();
		for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
		    ColumnDescriptor column = tableDescriptor.getColumn(fieldDescriptor.getColumn());
		    String fieldNm = fieldDescriptor.getName();
		    if (column == null || "active".equals(fieldNm) || "objectId".equals(fieldNm) || "versionNumber".equals(fieldNm) || "lastUpdateDate".equals(fieldNm)) {
			continue;
		    }
		    writer.write(classNm + ", " + fieldNm + ", " + column.getSize());
		    writer.newLine();
		}
	    }
	    writer.flush();
	    writer.close();
	    System.out.println("DONE check out the file " + "/temp/db-pojo-size.txt");

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
