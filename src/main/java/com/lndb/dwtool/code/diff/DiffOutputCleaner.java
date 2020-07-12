package com.lndb.dwtool.code.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiffOutputCleaner {
    public static void main(String[] args) {
	try {
	    File loc = new File("/Projects/kfs-upgrade/automation/kfs/lhs");
	    cleanDiffFiles(loc);
	    loc = new File("/Projects/kfs-upgrade/automation/kfs/rhs");
	    cleanDiffFiles(loc);
	    loc = new File("/Projects/kfs-upgrade/automation/rice/lhs");
	    cleanDiffFiles(loc);
	    loc = new File("/Projects/kfs-upgrade/automation/rice/rhs");
	    cleanDiffFiles(loc);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static void cleanDiffFiles(File loc) throws FileNotFoundException, IOException {
	List<File> files = new ArrayList<File>();
	listContents(loc, files);

	for (File file : files) {
	    List<String> lines = new ArrayList<String>();
	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		line = line.trim();
		if (line.length() > 1 && line.startsWith(">")) {
		    lines.add(line);
		}
	    }
	    reader.close();

	    if (file.delete()) {
		file.createNewFile();
		FileUtil.writeLines(lines, file);
	    }
	}
    }

    public static void listContents(File dir, List<File> files) {
	if (dir != null && dir.isDirectory()) {
	    File[] children = dir.listFiles();
	    for (File child : children) {
		if (child.isFile()) {
		    files.add(child);
		} else if (child.isDirectory()) {
		    listContents(child, files);
		}
	    }
	}
    }
}
