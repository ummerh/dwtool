/**
 * 
 */
package demo.release.control.data.web;

import org.apache.commons.lang.StringUtils;

/**
 * 
 */
public class FisheyeDetail {
	private Integer id;

	private String csid, comments, path, project, displayPath, author;

	public String getCsid() {
		return StringUtils.substring(csid, 0, 7);
	}

	public void setCsid(String csid) {
		this.csid = csid;
	}

	public String getDisplayComments() {
		if (this.comments != null && this.comments.length() > 75) {
			return this.comments.substring(0, 75) + "...";
		}
		return comments;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getDisplayPath() {
		return displayPath;
	}

	public void setDisplayPath(String displayPath) {
		this.displayPath = displayPath;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
}
