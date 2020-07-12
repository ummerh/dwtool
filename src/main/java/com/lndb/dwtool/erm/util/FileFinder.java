package com.lndb.dwtool.erm.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * This class provides methods to find files from class path and specific file locations <li>Uses specified file filter</li> <li>Can find input streams from class path including from within Jar files</li>
 * <li>Can find input stream from source directory and jar files placed within it</li>
 * 
 * 
 * @author harsha07
 * 
 */
public class FileFinder {
	private static final FileFinder INSTANCE = new FileFinder();

	private static class JarFilter implements FileFilter {
		public boolean accept(File pathname) {
			if (pathname.getName().toLowerCase().endsWith(".jar")) {
				return true;
			}
			return false;
		}
	}

	private static final JarFilter JAR_FILTER = new JarFilter();

	private FileFinder() {
	}

	public static final FileFinder getInstance() {
		return INSTANCE;
	}

	public List<FileInputStreamWrapper> findFromSystemClasspath(FileFilter filter) throws IOException {
		List<FileInputStreamWrapper> files = new ArrayList<FileInputStreamWrapper>();
		String classpath = System.getProperty("java.class.path");
		String[] paths = classpath.split(System.getProperty("path.separator"));
		for (String path : paths) {
			File filePath = new File(path);
			listFiles(filePath, files, filter);
		}

		return files;
	}

	public List<FileInputStreamWrapper> findFromOjbRepositoryDir(FileFilter filter) throws IOException {
		List<FileInputStreamWrapper> files = new ArrayList<FileInputStreamWrapper>();
		listFiles(new File(Configuration.getProperty("ojbRepositoryDir")), files, filter);
		return files;
	}

	public List<FileInputStreamWrapper> findFromJpaRepositoryDir(FileFilter filter) throws IOException {
		List<FileInputStreamWrapper> jarFiles = new ArrayList<FileInputStreamWrapper>();
		listFiles(new File(Configuration.getProperty("jpaRepositoryDir")), jarFiles, filter);
		List<FileInputStreamWrapper> classesFromJar = null;
		for (FileInputStreamWrapper fileInputStreamWrapper : jarFiles) {
			classesFromJar = findFromJarFile(fileInputStreamWrapper.getFile(), new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".class");
				}
			});
		}
		return classesFromJar;
	}

	public List<FileInputStreamWrapper> findFromWebClasspath(FileFilter filter, String packageName) throws IOException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		String ojbXmlPath = contextClassLoader.getResource(packageName).getPath();
		List<FileInputStreamWrapper> files = new ArrayList<FileInputStreamWrapper>();
		listFiles(new File(ojbXmlPath), files, filter);
		return files;
	}

	public List<FileInputStreamWrapper> findFromDir(File sourceDir, FileFilter filter) throws IOException {
		List<FileInputStreamWrapper> files = new ArrayList<FileInputStreamWrapper>();
		if (sourceDir.isDirectory()) {
			File[] contents = sourceDir.listFiles();
			for (File source : contents) {
				listFiles(source, files, filter);
			}
		}
		return files;
	}

	private void listFiles(File filePath, List<FileInputStreamWrapper> files, FileFilter filter) throws IOException {
		if (filePath.isDirectory()) {
			File[] contents = filePath.listFiles(filter);
			for (File file : contents) {
				try {
					files.add(new FileInputStreamWrapper(new FileInputStream(file), file, file.getName()));
				} catch (FileNotFoundException fne) {
					// ignore the exception
				}
			}
			iterateDirectories(filePath, files, filter);
		} else if (filter.accept(filePath)) {
			files.add(new FileInputStreamWrapper(new FileInputStream(filePath), filePath, filePath.getName()));

		} else if (JAR_FILTER.accept(filePath)) {
			addJarEntries(filePath, files, filter);

		}
	}

	private void addJarEntries(File filePath, List<FileInputStreamWrapper> files, FileFilter filter) throws IOException {
		JarInputStream jarIs = new JarInputStream(new FileInputStream(filePath));
		ZipEntry jarItem = null;
		while ((jarItem = jarIs.getNextEntry()) != null) {
			String fileName = jarItem.getName();
			File jarFile = new File(fileName);
			if (filter.accept(jarFile)) {
				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
				if (is == null) {
					is = readJarFileContent(jarIs);
				}
				files.add(new FileInputStreamWrapper(is, jarFile, fileName));
			}
		}
	}

	public List<FileInputStreamWrapper> findFromJarFile(File jarFile, FileFilter filter) throws IOException {
		List<FileInputStreamWrapper> files = new ArrayList<FileInputStreamWrapper>();
		addJarEntries(jarFile, files, filter);
		return files;
	}

	private InputStream readJarFileContent(JarInputStream jarIs) throws IOException {
		InputStream is;
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(jarIs));
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append(StringUtil.LINE_BREAK);
		}
		is = new ByteArrayInputStream(sb.toString().getBytes());
		return is;
	}

	private void iterateDirectories(File filePath, List<FileInputStreamWrapper> files, FileFilter filter) throws IOException {
		File[] directories = filePath.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		for (File dir : directories) {
			listFiles(dir, files, filter);
		}
	}
}
