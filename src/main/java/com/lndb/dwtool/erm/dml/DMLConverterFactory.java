package com.lndb.dwtool.erm.dml;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.manager.action.ExcelImportAction;

public class DMLConverterFactory {

	
	public static DMLConverterInterface getConverter(String fileName) {
		if (StringUtils.isNotBlank(fileName)) {
			if (fileName.startsWith("kim-role-diff")) {
				return new RoleConverter();
			}
			else if (fileName.startsWith("kim-perm-diff")) {
				return new PermissionConverter();
			}
			else if (fileName.startsWith("kim-roleperm-diff")) {
				return new ComplexConverter();
			}
			else if (fileName.startsWith("kim-rsp-diff")) {
				return new ResponsibilityConverter();
			}
			else if (fileName.startsWith("kim-rolersp-diff")) {
				return new RoleResponsibilityConverter();
			}
		}
//			return new ComplexConverter();
		return new SimpleConverter();
	}
}
