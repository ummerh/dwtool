package com.lndb.dwtool.code.diff;

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
		int _10kb = 1024 * 10;
		byte[] b = new byte[_10kb];
		int len = 0;
		while ((len = bfis.read(b)) > 0) {
			bfos.write(b, 0, len);
			b = new byte[_10kb];
		}
		bfos.flush();
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
					if (child.delete()) {
						System.out.println("Deleted " + child.getPath());
					}
				} else if (child.isDirectory()) {
					deleteContents(child);
					if (child.delete()) {
						System.out.println("Deleted " + child.getPath());
					}
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
