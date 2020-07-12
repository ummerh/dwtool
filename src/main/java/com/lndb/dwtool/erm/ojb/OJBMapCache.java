package com.lndb.dwtool.erm.ojb;

import java.io.File;

public class OJBMapCache {

    private static OJBMap map;

    public static synchronized OJBMap getOJBMap() throws Exception {
	if (map == null) {
	    map = new OJBMap();
	    map.loadFromRepositoryDir();
	}
	return map;

    }

    public static synchronized OJBMap getOJBMap(File file) throws Exception {
	if (map == null) {
	    map = new OJBMap();
	    map.loadFromDirectory(file);
	}
	return map;

    }

    public static synchronized void refreshMap() throws Exception {
	map = null;
	getOJBMap();
    }
}
