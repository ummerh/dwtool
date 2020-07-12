package demo.rice.upgrade;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class PathMapper {
	static class SourceInfo {
		private String fileName, fullPath;
		private long size;
		private double startRange, endRange;

		public SourceInfo(String pathKey, String fullPath, long size) {
			super();
			this.fileName = pathKey;
			this.fullPath = fullPath;
			if (size > 0) {
				this.size = size;
				this.startRange = size - (size * 0.10);
				this.endRange = size + (size * 0.10);
			}
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String pathKey) {
			this.fileName = pathKey;
		}

		public String getFullPath() {
			return fullPath;
		}

		public void setFullPath(String fullPath) {
			this.fullPath = fullPath;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public double getStartRange() {
			return startRange;
		}

		public void setStartRange(double startRange) {
			this.startRange = startRange;
		}

		public double getEndRange() {
			return endRange;
		}

		public void setEndRange(double endRange) {
			this.endRange = endRange;
		}

	}

	public static void main(String[] args) {
		try {
			BufferedWriter result = new BufferedWriter(new FileWriter(new File("/TEMP/rice-2-3-6-list.csv")));
			File root = new File("C:\\java\\kepler\\workspace\\rice-2-3-6");
			ArrayList<SourceInfo> paths = new ArrayList<SourceInfo>();
			listPathInfo(root, paths);
			Collections.sort(paths, new Comparator<SourceInfo>() {
				public int compare(SourceInfo o1, SourceInfo o2) {
					return o1.getFileName().compareTo(o2.getFileName());
				}
			});
			for (SourceInfo path : paths) {
				result.write("\"" + path.getFileName() + "\",\"" + path.getFullPath() + "\",\"" + path.getSize() + "\",\""
						+ path.getStartRange() + "\",\"" + path.getEndRange() + "\"");
				result.newLine();
			}
			result.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void listPathInfo(File root, List<SourceInfo> paths) throws Exception {
		File[] list = root.listFiles();
		for (File d : list) {
			if (d.isDirectory()) {
				listPathInfo(d, paths);
			} else {
				if (!d.getName().startsWith(".")) {
					File parent1 = new File(d.getParent());
					if (d.getName().endsWith("pom.xml")) {
						paths.add(new SourceInfo(parent1.getName() + "/" + d.getName(), d.getAbsolutePath(), FileUtils.sizeOf(d)));
					} else if (d.getName().endsWith(".java") || d.getName().endsWith(".jsp") || d.getName().endsWith(".tag")
							|| (d.getName().endsWith(".xml") && d.getPath().contains("src"))) {
						File parent2 = new File(parent1.getParent());
						paths.add(new SourceInfo(parent2.getName() + "/" + parent1.getName() + "/" + d.getName(), d.getAbsolutePath(),
								FileUtils.sizeOf(d)));
					}
				}
			}
		}
	}
}
