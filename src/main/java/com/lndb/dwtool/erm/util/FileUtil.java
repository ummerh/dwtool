package com.lndb.dwtool.erm.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public class FileUtil {
	public static File prepareFilePath(String name, String... paths) {
		StringBuilder pathBuf = new StringBuilder();
		for (String path : paths) {
			pathBuf.append(path);
			File dir = new File(pathBuf.toString());
			if (!dir.exists()) {
				dir.mkdir();
			}
			pathBuf.append(File.separator);
		}
		pathBuf.append(name);
		return new File(pathBuf.toString());
	}

	public static void writeOut(InputStream is, File output) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.write(line);
			writer.newLine();
		}
		reader.close();
		writer.flush();
		writer.close();

	}

	public static void streamOut(InputStream is, OutputStream output, boolean close) throws IOException {
		BufferedInputStream bfis = new BufferedInputStream(is);
		BufferedOutputStream bfos = new BufferedOutputStream(output);
		byte[] b = new byte[65536];
		int len = 0;
		while ((len = bfis.read(b)) != -1) {
			bfos.write(b, 0, len);
		}
		if (close) {
			bfis.close();
			bfos.flush();
			bfos.close();
		}
	}

	public static void appendToFile(File file, String[] lines) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		writer.newLine();
		for (String line : lines) {
			writer.write(line);
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}

	public static void deleteContents(File dir) {
		if (dir != null && dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (File child : children) {
				if (child.isFile()) {
					child.delete();
				} else if (child.isDirectory()) {
					deleteContents(child);
					child.delete();
				}
			}
		}
	}

	public static void writeLines(List<String> lines, File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (String line : lines) {
			writer.write(line);
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}

	public static void write(Reader readerIn, Writer writerOut) throws IOException {
		BufferedWriter writer = new BufferedWriter(writerOut);
		BufferedReader reader = new BufferedReader(readerIn);
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.write(line);
			writer.newLine();
		}
		reader.close();
		writer.flush();
		writer.close();
	}

	public static void write(InputStream is, OutputStream os) throws IOException {
		BufferedInputStream bfis = new BufferedInputStream(is);
		BufferedOutputStream bfos = new BufferedOutputStream(os);
		byte[] b = new byte[10 * 1024];
		int read = 0;
		while ((read = bfis.read(b)) > 0) {
			os.write(b, 0, read);
		}
		bfos.flush();
		bfos.close();
		bfis.close();
	}

	public static void htmlStream(Reader readerIn, Writer writerOut) throws IOException {
		BufferedWriter writer = new BufferedWriter(writerOut);
		BufferedReader reader = new BufferedReader(readerIn);
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.write(line.replaceAll("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
			writer.newLine();
		}
		reader.close();
		writer.flush();
	}
}
