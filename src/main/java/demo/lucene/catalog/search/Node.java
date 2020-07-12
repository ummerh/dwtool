/**
 * 
 */
package demo.lucene.catalog.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {
    private static final long serialVersionUID = 368973793836924305L;
    private Node parent;
    private String id;
    private List<Node> children = new ArrayList<Node>();
    private List<String> childrenIds = new ArrayList<String>();
    private String name;
    private String group;
    private String groupCode;

    public Node(Node parent, String id, String name) {
	this.parent = parent;
	this.id = id;
	this.name = name;
    }

    public Node getParent() {
	return parent;
    }

    public void setParent(Node parent) {
	this.parent = parent;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public List<Node> getChildren() {
	return children;
    }

    public void setChildren(List<Node> children) {
	this.children = children;
    }

    public void addChild(Node child) {
	this.children.add(child);
	this.childrenIds.add(child.getId());
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    private void loadAncestors(List<Node> ancestors) {
	if (this.parent != null) {
	    ancestors.add(this.parent);
	    parent.loadAncestors(ancestors);
	}
    }

    private void loadDescendants(List<Node> descendants) {
	if (!this.children.isEmpty()) {
	    for (Node node : children) {
		descendants.add(node);
		node.loadDescendants(descendants);
	    }
	}
    }

    public List<Node> getDescendants() {
	ArrayList<Node> descendants = new ArrayList<Node>();
	loadDescendants(descendants);
	return descendants;
    }

    public List<Node> getAncestors() {
	ArrayList<Node> ancestors = new ArrayList<Node>();
	loadAncestors(ancestors);
	return ancestors;
    }

    public String getGroup() {
	return group;
    }

    public void setGroup(String group) {
	this.group = group;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final Node other = (Node) obj;
	if (id == null) {
	    if (other.id != null)
		return false;
	} else if (!id.equals(other.id))
	    return false;
	return true;
    }

    public String getGroupCode() {
	return groupCode;
    }

    public void setGroupCode(String groupCode) {
	this.groupCode = groupCode;
    }
}
