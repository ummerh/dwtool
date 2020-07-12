/*
 * ProjectCode Abbrev - Description
 *
 * Copyright 2006 Michigan State University
 * East Lansing, Michigan 48824, U.S.A.
 * All rights reserved.
 */
package com.lndb.dwtool.erm.manager.web.tag;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * DOCUMENT ME!
 * 
 * @version 1.0
 */
public abstract class ErTagSupport extends TagSupport {
    protected boolean editable;
    protected String property;
    protected String value;
    protected boolean disabled;
    protected String id;
    protected String styleClass;
    protected boolean readOnly;

    /**
     * Creates a new ErTagSupport object.
     */
    public ErTagSupport() {
	super();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String determineStyleClass() {
	String styleText = "";

	if (getStyleClass() != null) {
	    styleText = " class=\"" + getStyleClass() + "\" ";
	}

	return styleText;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isEditable() {
	return editable;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param editable
     *            DOCUMENT ME!
     */
    public void setEditable(boolean editable) {
	this.editable = editable;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getProperty() {
	return property;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param property
     *            DOCUMENT ME!
     */
    public void setProperty(String property) {
	this.property = property;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getValue() {
	return value;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param value
     *            DOCUMENT ME!
     */
    public void setValue(String value) {
	this.value = value;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isDisabled() {
	return disabled;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param disabled
     *            DOCUMENT ME!
     */
    public void setDisabled(boolean disabled) {
	this.disabled = disabled;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getId() {
	return id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id
     *            DOCUMENT ME!
     */
    public void setId(String id) {
	this.id = id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getStyleClass() {
	return styleClass;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param styleClass
     *            DOCUMENT ME!
     */
    public void setStyleClass(String styleClass) {
	this.styleClass = styleClass;
    }

    public boolean isReadOnly() {
	return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
	this.readOnly = readOnly;
    }

}
