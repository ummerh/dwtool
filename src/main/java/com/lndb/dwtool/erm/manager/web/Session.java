package com.lndb.dwtool.erm.manager.web;

import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

public final class Session {
    public static final void set(String key, Object val) {
	HttpSession session = ServletActionContext.getRequest().getSession();
	session.setAttribute(key, val);
    }

    public static final Object get(String key) {
	HttpSession session = ServletActionContext.getRequest().getSession();
	return session.getAttribute(key);
    }
}
