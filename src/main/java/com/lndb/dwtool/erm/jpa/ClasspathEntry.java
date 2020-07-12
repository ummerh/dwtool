package com.lndb.dwtool.erm.jpa;

public class ClasspathEntry {
    private String pathFileLocation;
    private String kind;
    private String path;
    private Boolean exported;

    /**
     * @return the kind
     */
    public String getKind() {
	return kind;
    }

    /**
     * @param kind
     *            the kind to set
     */
    public void setKind(String kind) {
	this.kind = kind;
    }

    /**
     * @return the path
     */
    public String getPath() {
	return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
	this.path = path;
    }

    /**
     * @return the exported
     */
    public Boolean isExported() {
	return exported;
    }

    /**
     * @param exported
     *            the exported to set
     */
    public void setExported(Boolean exported) {
	this.exported = exported;
    }

    /**
     * @return the pathFileLocation
     */
    public String getPathFileLocation() {
	return pathFileLocation;
    }

    /**
     * @param pathFileLocation
     *            the pathFileLocation to set
     */
    public void setPathFileLocation(String pathFileLocation) {
	this.pathFileLocation = pathFileLocation;
    }

}
