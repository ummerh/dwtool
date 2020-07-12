package com.lndb.dwtool.code.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiffOutputRunner {
    public static void main(String[] args) {
	try {
	    DiffOutPreparer.main(null);
	    // Always remember to use the file separator
	    String vanillaLoc = "/java/projects/kfs3-lndb-vanilla/";
	    String currentLoc = "/java/projects/kfs3-lndb-trunk/";
	    String fileListLoc = "/Projects/kfs-upgrade/automation/kfs-file-locations.txt";
	    String outputLoc = "/Projects/kfs-upgrade/automation/kfs/lhs/";
	    String commandScript = "/Projects/kfs-upgrade/automation/kfs/command-lhs.bat";

	    createAndExecuteDiffCommands(vanillaLoc, currentLoc, fileListLoc, outputLoc, commandScript);

	    vanillaLoc = "/java/projects/kfs5/";
	    currentLoc = "/git/kfs/kfs/";
	    fileListLoc = "/Projects/kfs-upgrade/automation/kfs-file-locations.txt";
	    outputLoc = "/Projects/kfs-upgrade/automation/kfs/rhs/";
	    commandScript = "/Projects/kfs-upgrade/automation/kfs/command-rhs.bat";
	    createAndExecuteDiffCommands(vanillaLoc, currentLoc, fileListLoc, outputLoc, commandScript);

	    vanillaLoc = "/java/projects/rice-1.0.1-lndb-vanilla/";
	    currentLoc = "/java/projects/rice-1.0.1-kfs-trunk/";
	    fileListLoc = "/Projects/kfs-upgrade/automation/rice-file-locations.txt";
	    outputLoc = "/Projects/kfs-upgrade/automation/rice/lhs/";
	    commandScript = "/Projects/kfs-upgrade/automation/rice/command-lhs.bat";
	    createAndExecuteDiffCommands(vanillaLoc, currentLoc, fileListLoc, outputLoc, commandScript);

	    vanillaLoc = "/java/projects/rice-1.0.3.3-vanilla/";
	    currentLoc = "/git-repos/rice/";
	    fileListLoc = "/Projects/kfs-upgrade/automation/rice-file-locations.txt";
	    outputLoc = "/Projects/kfs-upgrade/automation/rice/rhs/";
	    commandScript = "/Projects/kfs-upgrade/automation/rice/command-rhs.bat";
	    createAndExecuteDiffCommands(vanillaLoc, currentLoc, fileListLoc, outputLoc, commandScript);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static void createAndExecuteDiffCommands(String vanillaLoc, String currentLoc, String fileListLoc, String outputLoc, String commandScript) throws FileNotFoundException, IOException {
	List<String> list = new ArrayList<String>();
	BufferedReader reader = new BufferedReader(new FileReader(fileListLoc));
	String line = null;
	while ((line = reader.readLine()) != null) {
	    String command = "diff -a --text -b --ignore-space-change -B --ignore-blank-lines --normal " + vanillaLoc + line + " " + currentLoc + line + " > " + outputLoc + line;
	    list.add(command.replace('\\', '/'));
	}
	reader.close();
	File commandFile = new File(commandScript);
	FileUtil.writeLines(list, commandFile);
	// Runtime.getRuntime().exec("cmd start C/ " + commandFile.getName());
    }
}
