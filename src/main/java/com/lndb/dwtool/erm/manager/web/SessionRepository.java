package com.lndb.dwtool.erm.manager.web;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.lndb.dwtool.erm.util.Configuration;

public class SessionRepository {
    public static final String SESSION__REPOSITORY = Configuration.getProperty("sessionDir");

    /**
     * Private method to get the session directory
     * 
     * @return
     */
    public static File getSessionDirectory() {
	HttpServletRequest request = ServletActionContext.getRequest();
	HttpSession session = request.getSession(true);
	return getSessionDirectory(session);
    }

    public static File getSessionDirectory(HttpSession session) {
	if (session == null) {
	    return null;
	}
	String sessionId = session.getId();
	File sessionDir = new File((SESSION__REPOSITORY + File.separator + sessionId));
	if (!sessionDir.isDirectory()) {
	    sessionDir.mkdirs();
	}
	return sessionDir;
    }
}
