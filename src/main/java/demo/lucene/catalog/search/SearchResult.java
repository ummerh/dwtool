/**
 * 
 */
package demo.lucene.catalog.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.lndb.dwtool.erm.util.Pager;

/**
 * 
 */
public class SearchResult implements Serializable {
	private static final long serialVersionUID = -4487981248478079638L;

	protected static class ItemResult implements Serializable {
		private static final long serialVersionUID = -2480550177309712285L;
		private int itemId;
		private double cost;
		private int relevance;
		private int orderCount;

		public ItemResult(int itemId, int defaultOrder, int orderCount, double cost) {
			super();
			this.itemId = itemId;
			this.relevance = defaultOrder;
			this.orderCount = orderCount;
			this.cost = cost;
		}

		public int getItemId() {
			return itemId;
		}

		public double getCost() {
			return cost;
		}

		public int getRelevance() {
			return relevance;
		}

		public int getOrderCount() {
			return orderCount;
		}

		public void setItemId(int catalogId) {
			this.itemId = catalogId;
		}

		public void setCost(double cost) {
			this.cost = cost;
		}

		public void setRelevance(int defaultOrder) {
			this.relevance = defaultOrder;
		}

		public void setOrderCount(int orderCount) {
			this.orderCount = orderCount;
		}
	}

	private static Comparator<ItemResult> byBestSeller = new Comparator<ItemResult>() {
		public int compare(ItemResult o1, ItemResult o2) {
			double c1 = o1.getOrderCount();
			double c2 = o2.getOrderCount();
			return c1 == c2 ? 0 : (c1 > c2 ? 1 : -1);
		}
	};
	private static Comparator<ItemResult> byCost = new Comparator<ItemResult>() {
		public int compare(ItemResult o1, ItemResult o2) {
			double c1 = o1.getCost();
			double c2 = o2.getCost();
			return c1 == c2 ? 0 : (c1 > c2 ? 1 : -1);
		}
	};

	private static Comparator<ItemResult> byRelevance = new Comparator<ItemResult>() {
		public int compare(ItemResult o1, ItemResult o2) {
			double c1 = o1.getRelevance();
			double c2 = o2.getRelevance();
			return c1 == c2 ? 0 : (c1 > c2 ? 1 : -1);
		}
	};

	private List<ItemResult> results = new ArrayList<ItemResult>();
	private Pager<ItemResult> pager = new Pager<ItemResult>(results);
	private List<String> catalogSubgrpIds = new ArrayList<String>();
	private HashMap<String, HashSet<String>> resultByGroup = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> resultBySubGroup = new HashMap<String, HashSet<String>>();
	private String menuTree;
	private String currentSort = "a";

	public void sortByBestSeller() {
		Collections.sort(this.results, byBestSeller);
	}

	public void sortByCost() {
		Collections.sort(this.results, byCost);
	}

	public void sortByRelevance() {
		Collections.sort(this.results, byRelevance);
	}

	public void reverseSort() {
		Collections.reverse(this.results);
	}

	public List<ItemResult> getResults() {
		return results;
	}

	public List<String> getCatalogSubgrpIds() {
		return catalogSubgrpIds;
	}

	public void setCatalogSubgrpIds(List<String> catalogSubgrpIds) {
		this.catalogSubgrpIds = catalogSubgrpIds;
	}

	public List<ItemResult> getPage(int page, int pageSize) {
		pager.setPageSize(pageSize);
		return pager.getPage(page);
	}

	public boolean nextPage(int page, int pageSize) {
		pager.setPageSize(pageSize);
		return pager.isNextPage();
	}

	public boolean prevPage(int page, int pageSize) {
		pager.setPageSize(pageSize);
		return pager.isPrevPage();
	}

	public String getMenuTree() {
		return menuTree;
	}

	public void setMenuTree(String menuTree) {
		this.menuTree = menuTree;
	}

	public String getCurrentSort() {
		return currentSort;
	}

	public void setCurrentSort(String currentSort) {
		this.currentSort = currentSort;
	}

	public void addToGroup(String grp, String itemId) {
		if (this.resultByGroup.get(grp) == null) {
			this.resultByGroup.put(grp, new HashSet<String>());
		}
		this.resultByGroup.get(grp).add(itemId);
	}

	public void addToGroup(String grp, HashSet<String> items) {
		if (this.resultByGroup.get(grp) == null) {
			this.resultByGroup.put(grp, new HashSet<String>());
		}
		this.resultByGroup.get(grp).addAll(items);
	}

	public void addToSubGroup(String subGgrp, String itemId) {
		if (this.resultBySubGroup.get(subGgrp) == null) {
			this.resultBySubGroup.put(subGgrp, new HashSet<String>());
		}
		this.resultBySubGroup.get(subGgrp).add(itemId);
	}

	public int getGroupCount(String grp) {
		if (this.resultByGroup.get(grp) != null) {
			return this.resultByGroup.get(grp).size();
		}
		return 0;
	}

	public int getSubGroupCount(String subGrp) {
		if (this.resultBySubGroup.get(subGrp) != null) {
			return this.resultBySubGroup.get(subGrp).size();
		}
		return 0;
	}

	public HashSet<String> getSubGroupItems(String subGrp) {
		if (this.resultBySubGroup.get(subGrp) != null) {
			return this.resultBySubGroup.get(subGrp);
		}
		return null;
	}

}
