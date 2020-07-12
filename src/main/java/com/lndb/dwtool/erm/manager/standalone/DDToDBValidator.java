package com.lndb.dwtool.erm.manager.standalone;

import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;

public class DDToDBValidator {
	public static void main(String[] args) {
		try {
			OJBMap ojbMap = OJBMapCache.getOJBMap();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
