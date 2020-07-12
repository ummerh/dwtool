package com.lndb.dwtool.erm.manager.web.jquery;

import java.util.ArrayList;
import java.util.List;

public class EasyUiTreeNode {
    private int id;
    private String text;
    private String iconCls;
    private boolean checked;
    private String state;
    private List<EasyUiTreeNode> children = new ArrayList<EasyUiTreeNode>();

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public String getText() {
	return text;
    }

    public void setText(String text) {
	this.text = text;
    }

    public String getIconCls() {
	return iconCls;
    }

    public void setIconCls(String iconCls) {
	this.iconCls = iconCls;
    }

    public boolean isChecked() {
	return checked;
    }

    public void setChecked(boolean checked) {
	this.checked = checked;
    }

    public String getState() {
	return state;
    }

    public void setState(String state) {
	this.state = state;
    }

    public List<EasyUiTreeNode> getChildren() {
	return children;
    }

    public void setChildren(List<EasyUiTreeNode> children) {
	this.children = children;
    }
}