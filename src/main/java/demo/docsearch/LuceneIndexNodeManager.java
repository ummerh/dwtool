package demo.docsearch;

import java.io.File;
import java.io.FilenameFilter;

public class LuceneIndexNodeManager {
	private static final String INDEX_PREFIX = "index";
	private static final String INDEX_LOCK = "index.lock";

	public final class HostNameFileFilter implements FilenameFilter {
		private final String hostName;

		private HostNameFileFilter(String hostName) {
			this.hostName = hostName;
		}

		public boolean accept(File dir, String name) {
			return name.contains(hostName);
		}
	}

	private static final String META_INFO_DIR = "meta-info";
	private static final String READY_SUFFIX = ".ready";
	private static final String RELOAD_SUFFIX = ".reload";
	private static final String LOCK_SUFFIX = ".lock";
	private static final String INDEX_READY = "index.ready";
	private String luceneDir;

	public LuceneIndexNodeManager(String luceneDir) {
		this.luceneDir = luceneDir;
	}

	public boolean isReady() {
		try {
			final String hostName = java.net.InetAddress.getLocalHost().getHostName();
			File rootDir = new File(luceneDir);
			if (rootDir.exists()) {
				File metaDir = new File(luceneDir, META_INFO_DIR);
				if (metaDir.exists()) {
					String[] list = metaDir.list(new HostNameFileFilter(hostName));
					if ((list.length == 0 && new File(metaDir, INDEX_READY).exists()) || (list != null && list.length > 0 && list[0].endsWith(READY_SUFFIX))) {
						if (!new File(metaDir, hostName + READY_SUFFIX).exists() && !new File(metaDir, hostName + READY_SUFFIX).createNewFile()) {
							throw new RuntimeException("Failed to create ready ");
						}
						return !isLocked();
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
		return false;
	}

	public boolean isReload() {
		try {
			final String hostName = java.net.InetAddress.getLocalHost().getHostName();
			File rootDir = new File(luceneDir);
			if (rootDir.exists()) {
				File metaDir = new File(luceneDir, META_INFO_DIR);
				if (metaDir.exists()) {
					String[] list = metaDir.list(new HostNameFileFilter(hostName));
					if (list != null && list.length == 1 && list[0].endsWith(RELOAD_SUFFIX)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
		return false;
	}

	public boolean isLocked() {
		try {
			final String hostName = java.net.InetAddress.getLocalHost().getHostName();
			File rootDir = new File(luceneDir);
			if (rootDir.exists()) {
				File metaDir = new File(luceneDir, META_INFO_DIR);
				if (metaDir.exists()) {
					String[] list = metaDir.list(new HostNameFileFilter(hostName));
					if (list != null && list.length == 1 && list[0].endsWith(LOCK_SUFFIX)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
		return false;
	}

	public void startIndexing() {
		try {
			File metaDir = new File(luceneDir, META_INFO_DIR);
			metaDir.mkdirs();
			File ready = new File(metaDir, INDEX_READY);

			if (ready.exists()) {
				if (!ready.renameTo(new File(metaDir, INDEX_LOCK))) {
					throw new RuntimeException("Failed to rename root ready " + ready.getAbsolutePath());
				}
			} else {
				if (!new File(metaDir, INDEX_LOCK).exists() && !new File(metaDir, INDEX_LOCK).createNewFile()) {
					throw new RuntimeException("Failed to create root lock " + ready.getAbsolutePath());
				}
			}
			// mark all ready and reload files to lock files
			File[] files = metaDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return !name.startsWith(INDEX_PREFIX) && (name.endsWith(READY_SUFFIX) || name.endsWith(RELOAD_SUFFIX));
				}
			});
			for (File file : files) {
				if (!file.renameTo(new File(metaDir, file.getName().replace(READY_SUFFIX, LOCK_SUFFIX).replace(RELOAD_SUFFIX, LOCK_SUFFIX)))) {
					throw new RuntimeException("Failed to convert to lock" + file.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	public void finishIndexing() {
		try {
			File metaDir = new File(luceneDir, META_INFO_DIR);
			metaDir.mkdirs();
			File lock = new File(metaDir, INDEX_LOCK);
			if (lock.exists()) {
				if (!lock.renameTo(new File(metaDir, INDEX_READY))) {
					throw new RuntimeException("Failed to rename root lock " + lock.getAbsolutePath());
				}
			} else {
				if (!new File(metaDir, INDEX_READY).exists() && !new File(metaDir, INDEX_READY).createNewFile()) {
					throw new RuntimeException("Failed to create root ready " + lock.getAbsolutePath());
				}
			}
			// mark all locked and ready files to reload files
			File[] files = metaDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return !name.startsWith(INDEX_PREFIX) && (name.endsWith(READY_SUFFIX) || name.endsWith(LOCK_SUFFIX));
				}
			});
			for (File file : files) {
				if (!file.renameTo(new File(metaDir, file.getName().replace(READY_SUFFIX, RELOAD_SUFFIX).replace(LOCK_SUFFIX, RELOAD_SUFFIX)))) {
					throw new RuntimeException("Failed to convert to reload " + file.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	public void finishReloading() {
		try {
			final String hostName = java.net.InetAddress.getLocalHost().getHostName();
			File metaDir = new File(luceneDir, META_INFO_DIR);
			metaDir.mkdirs();
			File reloadHost = new File(metaDir, hostName + RELOAD_SUFFIX);
			if (reloadHost.exists()) {
				if (!reloadHost.renameTo(new File(metaDir, hostName + READY_SUFFIX))) {
					throw new RuntimeException("Failed to rename reload to ready " + reloadHost.getAbsolutePath());
				}
			} else {
				if (!new File(metaDir, hostName + READY_SUFFIX).exists() && !new File(metaDir, hostName + READY_SUFFIX).createNewFile()) {
					throw new RuntimeException("Failed to create ready " + reloadHost.getAbsolutePath());
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	public String getLuceneDir() {
		return luceneDir;
	}

	public void setLuceneDir(String luceneDir) {
		this.luceneDir = luceneDir;
	}
}
