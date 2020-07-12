package com.lndb.dwtool.erm.manager.web;

import java.io.File;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.lndb.dwtool.erm.util.FileUtil;

/**
 * Session listener that cleans up after each session
 * 
 * @author harsha07
 * 
 */
public class SessionListener implements HttpSessionListener {

    public void sessionCreated(HttpSessionEvent event) {
	File sessionDir = SessionRepository.getSessionDirectory(event.getSession());
	if (!sessionDir.exists()) {
	    sessionDir.mkdirs();
	}
    }

    public void sessionDestroyed(HttpSessionEvent event) {
	File sessionDir = SessionRepository.getSessionDirectory(event.getSession());
	if (sessionDir != null && sessionDir.isDirectory()) {
	    FileUtil.deleteContents(sessionDir);
	    sessionDir.delete();
	}
    }

}
