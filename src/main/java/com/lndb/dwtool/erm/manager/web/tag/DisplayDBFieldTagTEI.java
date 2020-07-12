package com.lndb.dwtool.erm.manager.web.tag;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

public class DisplayDBFieldTagTEI extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData data) {
	return new VariableInfo[] { new VariableInfo("columnName", "java.lang.String", true, VariableInfo.NESTED),
		new VariableInfo("tableDescriptor", "com.lndb.dwtool.erm.db.TableDescriptor", true, VariableInfo.NESTED), new VariableInfo("data", "java.lang.Object", true, VariableInfo.NESTED),
		new VariableInfo("property", "java.lang.String", true, VariableInfo.NESTED), new VariableInfo("id", "java.lang.String", true, VariableInfo.NESTED),
		new VariableInfo("styleClass", "java.lang.String", true, VariableInfo.NESTED), new VariableInfo("readOnly", "java.lang.Boolean", true, VariableInfo.NESTED) };

    }
}
