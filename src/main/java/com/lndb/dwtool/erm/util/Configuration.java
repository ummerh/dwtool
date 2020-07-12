/**
 * 
 */
package com.lndb.dwtool.erm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public final class Configuration {
    private static Properties properties = new Properties();

    public static String getProperty(String key) {
	if (properties.isEmpty()) {
	    try {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		properties.load(contextClassLoader.getResourceAsStream("settings.properties"));
		String configLoc = System.getProperty("dwtool.config.file");
		if (StringUtils.isNotBlank(configLoc)) {
		    File configFile = new File(System.getProperty("dwtool.config.file"));
		    if (configFile.exists()) {
			properties.load(new FileInputStream(configFile));
		    }
		} else {
		    File userHomeConfig = new File(System.getProperty("user.home"), "dwtool-configuration.properties");
		    if (userHomeConfig.exists()) {
			properties.load(new FileInputStream(userHomeConfig));
		    }
		}
	    } catch (IOException e) {
		System.err.println("Failed to load settings file");
	    }
	}
	return properties.getProperty(key);
    }
}
