package com.lndb.dwtool.erm.main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lndb.dwtool.erm.ojb.OJBMap;
import com.lndb.dwtool.erm.ojb.OJBMapCache;

public class HiveMetadataGenerator {
	public static void main(String[] args) {
		try {
			OJBMap ojbMap = OJBMapCache.getOJBMap();
			List<String> allTables = ojbMap.getAllTables();
			HashSet<String> autoUpdated = new HashSet<String>();
			for (String tblName : allTables) {
				autoUpdated.addAll(ojbMap.getAutoUpdateableReferences(tblName));
			}
			Set<String> updateableReferences = new HashSet<String>();
			StringBuilder sb = new StringBuilder();
			for (String tblName : allTables) {
				// display top level table
				if (!autoUpdated.contains(tblName) && (tblName.startsWith("PUR") || tblName.startsWith("AP"))) {
					updateableReferences.clear();
					sb = new StringBuilder();
					ojbMap.printUpdateableReferences(tblName, updateableReferences, sb);
					System.out.println(tblName + ">>" + sb.toString());
					ojbMap.getClassDescriptor(tblName).getColumnName("last");
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
