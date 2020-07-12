/**
 * 
 */
package com.lndb.dwtool.erm.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Pager<T> {
	private int current;
	private List<T> list;
	private String pageId;
	private int pageSize;
	private String sortBy;

	public Pager(List<T> list) {
		this.list = list;
		this.pageSize = 10;
	}

	public Pager(List<T> list, int pageSize) {
		this.list = list;
		this.pageSize = pageSize;
	}

	public int getCurrent() {
		return current;
	}

	public List<T> getList() {
		return list;
	}

	public int getNext() {
		return current + 1;
	}

	public List<T> getPage(int page) {
		int size = this.list.size();
		int endIndex = page * pageSize;
		int beginIndex = (page - 1) * pageSize;
		if (beginIndex > size - 1 || beginIndex < 0) {
			return new ArrayList<T>();
		}
		if (endIndex > size) {
			endIndex = size;
		}
		this.current = page;
		return this.list.subList(beginIndex, endIndex);
	}

	public String getPageId() {
		return pageId;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPrev() {
		return current - 1;
	}

	public String getSortBy() {
		return sortBy;
	}

	public int getTotalCount() {
		return this.list.size() / pageSize + (this.list.size() % pageSize > 0 ? 1 : 0);
	}

	public boolean isEmpty(int page) {
		int size = this.list.size();
		int beginIndex = (page - 1) * pageSize;
		if (beginIndex > size - 1 || beginIndex < 0) {
			return true;
		}
		return false;
	}

	public boolean isNextPage() {
		return !isEmpty(current + 1);
	}

	public boolean isPrevPage() {
		return !isEmpty(current - 1);
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

}
