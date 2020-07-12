package com.lndb.dwtool.code.search;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class SourceCodeFile {
	private String name;
	private String path;
	private String content;
	private boolean loaded;

	public void load(File file) {
		this.name = file.getName();
		this.path = file.getAbsolutePath().replace("\\", "/").replace("C:", "");
		try {
			content = FileUtils.readFileToString(file, "UTF-8");
			loaded = true;
		} catch (IOException e) {
			content = null;
		}
	}

	public Document toDocument() {
		Document doc = new Document();
		doc.add(new Field("name", name, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("path", path, Field.Store.YES, Field.Index.ANALYZED));
		if (loaded && content != null) {
			doc.add(new Field("content", content, Field.Store.YES, Field.Index.ANALYZED));
		}
		return doc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

}
