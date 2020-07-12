package com.lndb.dwtool.erm.kom;

import java.util.ArrayList;
import java.util.List;

public class OrgNode {
	private String id, name;
	private Object data;
	private List<OrgNode> children = new ArrayList<OrgNode>(1);

	public OrgNode() {
		// TODO Auto-generated constructor stub
	}

	public OrgNode(String id, String name, Object data) {
		super();
		this.id = id;
		this.name = name;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public List<OrgNode> getChildren() {
		return children;
	}

	public void setChildren(List<OrgNode> children) {
		this.children = children;
	}

	public void addChild(OrgNode node) {
		this.children.add(node);
	}

}
