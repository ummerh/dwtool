package com.lndb.dwtool.erm.manager.standalone;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.lndb.dwtool.erm.db.DBMap;
import com.lndb.dwtool.erm.db.DBMapCache;
import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;

public class OjbToDBValidator {
    public static void main(String[] args) {
	try {
	    System.out.println("Loading DB Map");
	    DBMap dbMap = DBMapCache.getDBMap("mmdemo");
	    System.out.println("Loading OJB Map");
	    OJBMap ojbMap = OJBMapCache.getOJBMap(new File("C:\\java\\projects\\mm-dev\\src\\java\\org\\kuali\\ext\\mm\\ojb-mm.xml"));
	    System.out.println("Validating OJB Map");
	    List<String> errors = ojbMap.validateAgainstDBSchema(dbMap, "org.kuali.ext.mm.");
	    Collections.sort(errors);
	    for (String string : errors) {
		System.out.println(string);
	    }

	} catch (Exception e) {

	    e.printStackTrace();
	}
    }
}
