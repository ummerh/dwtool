package com.lndb.dwtool.erm.db;

import java.util.ArrayList;
import java.util.Hashtable;

public class DBMapCache {
    private static ArrayList<String> loaded = new ArrayList<String>();
    private static Hashtable<String, DBMap> cacheMap = new Hashtable<String, DBMap>();

    // it is all synchronized, something is kinda screwed up but dont know what,
    // sometimes get wrong db map against the connection name
    public static synchronized DBMap getDBMap(String connectionName) throws Exception {
	if (cacheMap.get(connectionName) == null) {
	    synchronized (cacheMap) {
		ConnectionDetail conDetail = ConnectionDetail.configure(connectionName);
		DBMap dbMap = new DBMap();
		dbMap.loadMap(conDetail);
		cacheMap.put(connectionName, dbMap);
		loaded.add(connectionName);
	    }
	}

	return cacheMap.get(connectionName);
    }

    public static synchronized void removeDBMap(String connectionName) {
	cacheMap.remove(connectionName);
	loaded.remove(connectionName);
    }

    public static boolean isLoaded(String connectionName) {
	return loaded.contains(connectionName);
    }

    public static synchronized void clear() {
	cacheMap.clear();
	loaded.clear();
    }
}
