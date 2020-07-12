package com.lndb.dwtool.code.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DiffOutPreparer {
	public static void main(String[] args) {
		try {
			prepareDiffOutputPaths("/Projects/kfs-upgrade/automation/kfs/lhs/", "/Projects/kfs-upgrade/automation/kfs/rhs/", "/Projects/kfs-upgrade/automation/kfs-file-locations.txt");
			prepareDiffOutputPaths("/Projects/kfs-upgrade/automation/rice/lhs/", "/Projects/kfs-upgrade/automation/rice/rhs/", "/Projects/kfs-upgrade/automation/rice-file-locations.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void prepareDiffOutputPaths(String lhsParent, String rhsParent, String changeListFile) throws FileNotFoundException, IOException {
		File locs = new File(changeListFile);
		BufferedReader reader = new BufferedReader(new FileReader(locs));
		String line = null;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			File lhsFile = new File(lhsParent + line);
			if (!lhsFile.getParentFile().exists()) {
				lhsFile.getParentFile().mkdirs();
			}
			File rhsFile = new File(rhsParent + line);
			if (!rhsFile.getParentFile().exists()) {
				rhsFile.getParentFile().mkdirs();
			}
		}
		reader.close();
	}
}
