package com.lndb.dwtool.erm.jpa;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

import javax.persistence.Column;
import javax.persistence.Table;

import com.lndb.dwtool.erm.ForeignKey;
import com.lndb.dwtool.erm.RelationalMap;
import com.lndb.dwtool.erm.util.Configuration;
import com.lndb.dwtool.erm.util.FileFinder;
import com.lndb.dwtool.erm.util.FileInputStreamWrapper;

/**
 * This is under construction.....
 * 
 * Uses javassist to parse classes from jar files using class byte codes
 * Annotations are first collected from each class, fields and methods Next step
 * will interpret and create ClassDescriptors and Reference Descriptors
 * 
 * @author harsha07
 */
public class JpaMap extends RelationalMap {
    private Map<String, EntityDescriptor> classJpaMap = new HashMap<String, EntityDescriptor>();
    private Map<String, EntityDescriptor> tableJpaMap = new HashMap<String, EntityDescriptor>();
    private Map<String, String> classToTableMap = new HashMap<String, String>();
    private Map<String, String> tableToClassMap = new HashMap<String, String>();
    private boolean loaded;
    private static String jpaDirPath = Configuration.getProperty("jpaRepositoryDir");
    private static FileFilter jarFilter = new FileFilter() {
	public boolean accept(File pathname) {
	    return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".jar");
	}
    };
    private static Method ADD_URL_METHOD = null;
    static {
	try {
	    ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
	    ADD_URL_METHOD.setAccessible(true);
	} catch (Exception e) {
	}
    }

    @Override
    public List<String> getAllTables() {
	return null;
    }

    @Override
    public List<ForeignKey> getExportedKeys(String tableName) {
	return null;
    }

    @Override
    public List<ForeignKey> getForeignKeys(String tableName) {
	return null;
    }

    public void loadMap() {
	try {
	    if (!loaded) {
		// load url to classpath
		addUrlToClassLoader(jpaDirPath);
		ClassPool classPool = ClassPool.getDefault();
		classPool.insertClassPath(jpaDirPath + "/*");
		List<FileInputStreamWrapper> allClasses = FileFinder.getInstance().findFromJpaRepositoryDir(jarFilter);
		String className = null;

		for (FileInputStreamWrapper wrapper : allClasses) {
		    Map<String, List<Object>> fullList = new HashMap<String, List<Object>>();
		    try {
			className = wrapper.getFile().getPath().replace(File.separatorChar, '.').replace(".class", "");
			CtClass ctClass = classPool.get(className);
			fullList.put(className, new ArrayList<Object>());
			Object[] classAnnotations = ctClass.getAnnotations();
			if (classAnnotations.length > 0) {
			    addToAnnotationDetails(fullList, classAnnotations, className);
			    inspectDetails(className, fullList, ctClass);
			}
			EntityDescriptor entityDescriptor = new EntityDescriptor();
			entityDescriptor.setClassName(className);
			Set<String> keySet = fullList.keySet();
			for (String string : keySet) {
			    List<Object> list = fullList.get(string);
			    for (Object object : list) {
				if (Table.class.isAssignableFrom(object.getClass())) {
				    entityDescriptor.setTableName(((Table) object).name());
				}
				if (Column.class.isAssignableFrom(object.getClass())) {
				    String field = string.substring(string.indexOf('-') + 1);
				    System.out.println(field + "-" + ((Column) object).name());
				    FieldDescriptor fieldDescriptor = new FieldDescriptor((Column) object);
				    entityDescriptor.add(fieldDescriptor);
				}
			    }
			}
		    } catch (Exception e) {
			System.out.println("Could not inspect  " + className + " due to " + e.toString());
		    }
		}
		this.loaded = true;
	    }

	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private void inspectDetails(String className, Map<String, List<Object>> fullList, CtClass ctClass) throws ClassNotFoundException {
	CtField[] fields = ctClass.getDeclaredFields();
	for (CtField ctField : fields) {
	    addToAnnotationDetails(fullList, ctField.getAnnotations(), className, ctField.getName());
	}
	CtMethod[] methods = ctClass.getDeclaredMethods();
	for (CtMethod ctMethod : methods) {
	    addToAnnotationDetails(fullList, ctMethod.getAnnotations(), className, ctMethod.getName());
	}
	try {
	    CtClass superclass = ctClass.getSuperclass();
	    if (superclass != null && !"java.lang.Object".equals(superclass.getName())) {
		inspectDetails(className, fullList, superclass);
	    }
	} catch (NotFoundException e) {
	    System.out.println("Super could not be loaded");
	}
    }

    private void addToAnnotationDetails(Map<String, List<Object>> fullList, Object[] annotations, String... params) {
	for (Object object : annotations) {
	    List<Object> list = fullList.get(params[0] + (params.length == 2 ? "-" + params[1] : ""));
	    if (list == null) {
		list = new ArrayList<Object>();
		fullList.put(params[0] + (params.length == 2 ? "-" + params[1] : ""), list);
	    }
	    list.add(object);
	}
    }

    private URLClassLoader addUrlToClassLoader(String jpaPath) throws MalformedURLException {
	String[] supportedTypes = new String[] { ".jar" };
	ArrayList<URL> urls = new ArrayList<URL>();
	findResources(new File(jpaPath), supportedTypes, urls);
	URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	for (int i = 0; i < urls.size(); i++) {
	    try {
		ADD_URL_METHOD.invoke(sysloader, new Object[] { urls.get(i) });
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
	return sysloader;
    }

    private void findResources(File rootPath, String[] supportedTypes, List<URL> urls) throws MalformedURLException {
	File[] all = rootPath.listFiles();
	for (File content : all) {
	    if (content.isDirectory()) {
		urls.add(new URL("file:" + content.getPath() + File.separator));
		findResources(content, supportedTypes, urls);
	    } else {
		for (String mime : supportedTypes) {
		    if (content.getName().toLowerCase().endsWith(mime)) {
			urls.add(new URL("file:" + content.getPath()));
			break;
		    }
		}
	    }
	}
    }

    public void addEntityDescriptor(EntityDescriptor entityDescriptor) {
	if (!this.classJpaMap.containsKey(entityDescriptor.getClassName())) {
	    this.classJpaMap.put(entityDescriptor.getClassName(), entityDescriptor);
	    if (entityDescriptor.getTableName() != null) {
		if (this.tableJpaMap.get(entityDescriptor.getTableName().toUpperCase()) == null) {
		    this.tableJpaMap.put(entityDescriptor.getTableName().toUpperCase(), entityDescriptor);
		    this.tableToClassMap.put(entityDescriptor.getTableName().toUpperCase(), entityDescriptor.getClassName());
		} else {
		    System.err.println("Duplicate classes for the same table " + entityDescriptor.getTableName().toUpperCase() + " : " + entityDescriptor.getClassName());
		}
		this.classToTableMap.put(entityDescriptor.getClassName(), entityDescriptor.getTableName().toUpperCase());
	    } else {
		System.out.println("WARNING! Table mapping not found " + entityDescriptor.getClassName());
	    }

	    // do inverse reference
	    // collectInverseReferences(entityDescriptor);
	}

    }

    public static void main(String[] args) {
	JpaMap map = new JpaMap();
	map.loadMap();

    }
}
