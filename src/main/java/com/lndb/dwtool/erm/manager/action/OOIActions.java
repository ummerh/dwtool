package com.lndb.dwtool.erm.manager.action;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lndb.dwtool.erm.kom.TreeBuilder;
import com.opensymphony.xwork2.ActionSupport;

public class OOIActions extends ActionSupport {
	private static final long serialVersionUID = 13434L;

	public String rootOrgCode;

	public String getRootOrgCode() {
		return rootOrgCode;
	}

	public void setRootOrgCode(String rootOrgCode) {
		this.rootOrgCode = rootOrgCode;
	}

	public String getTreeJson() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getOutputStream(), new TreeBuilder().buildOrgTree(rootOrgCode));
		return null;
	}
}
