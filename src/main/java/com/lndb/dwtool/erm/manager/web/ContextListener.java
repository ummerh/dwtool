package com.lndb.dwtool.erm.manager.web;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.lndb.dwtool.erm.util.FileUtil;
import com.lndb.dwtool.erm.xldb.ExcelWorkbookDB;

/**
 * Servlet context listener that will clean up session data during startup and shutdown
 * 
 * @author harsha07
 * 
 */
public class ContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent arg0) {
		// clear session folders
		cleanUp();

	}

	public void contextInitialized(ServletContextEvent arg0) {
		try {
			//Log4jConfigurer.initLogging("classpath:log4j.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// clear session folders
		cleanUp();
		new ExcelWorkbookDB.WorkerThread().start();

	}

	private void cleanUp() {
		try {
			File dir = new File(SessionRepository.SESSION__REPOSITORY);
			FileUtil.deleteContents(dir);
		} catch (Exception e) {
			// do nothing
		}
	}

}
