package com.lndb.dwtool.erm.util;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.FromXmlRuleSet;

import com.lndb.dwtool.erm.jpa.ClasspathEntry;

public class ClasspathUtils {
    public static void main(String[] args) {
	try {
	    Thread spin = new Thread() {
		@Override
		public void run() {
		    try {
			Digester digester = new Digester();
			digester.addRuleSet(new FromXmlRuleSet(Thread.currentThread().getContextClassLoader().getResource("classpath-parser-rules.xml")));
			File file = new File("/java/projects/kfs/.classpath");
			List<?> entries = (List<?>) digester.parse(file);
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			for (Object obj : entries) {
			    ClasspathEntry classpathEntry = (ClasspathEntry) obj;
			    String filePath = file.getParent() + File.separatorChar + classpathEntry.getPath();

			    String url = "file:" + filePath;
			    method.invoke(sysloader, new Object[] { new URL(url) });
			    if (new File(filePath).isDirectory()) {
				method.invoke(sysloader, new Object[] { new URL(url + "/") });
			    }
			}
			// add tomcat libraries needed
			File tomcatDir = new File("/java/servers/apache-tomcat-5.5.25");
			List<FileInputStreamWrapper> fromTomcat = FileFinder.getInstance().findFromDir(tomcatDir, new FileFilter() {
			    public boolean accept(File pathname) {
				return pathname.isDirectory() || (pathname.isFile() && pathname.getPath().endsWith(".jar"));
			    }
			});
			for (FileInputStreamWrapper fs : fromTomcat) {
			    String url = "file:" + fs.getFile().getPath();
			    if (fs.getFile().isDirectory()) {
				url = url + "/";
			    }
			    method.invoke(sysloader, new Object[] { new URL(url) });
			}
			Class<?> loadClass = sysloader.loadClass("org.kuali.kfs.sys.context.SpringContext");
			Method init = loadClass.getDeclaredMethod("initializeTestApplicationContext");
			init.setAccessible(true);
			init.invoke(loadClass.newInstance());
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    };
	    spin.start();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
