package com.lndb.dwtool.erm.dml;

import java.io.BufferedWriter;
import java.io.FileInputStream;

import com.lndb.dwtool.erm.db.DBMap;

/**
 * This class defines a common interface for Data to SQL script converter
 * 
 * @author ZHANGMA
 * 
 */
public interface DMLConverterInterface {
	
	public static String ONE_ON_ONE = "One on One Mapping";
	public static String ROLES = "Roles";
	public static String PERMISSIONS = "Permissions";
	public static String RESPONSIBILITIES = "Responsibilities";
	public static String ROLE_PERMISSIONS = "Role Permissions";
	public static String ROLE_RESPONSIBILITIES = "Role Responsibilities";
	
	public boolean convertToSQL(FileInputStream dataInputStream, DBMap targetDb, String outputDir, BufferedWriter report, BufferedWriter errorReport);
}
