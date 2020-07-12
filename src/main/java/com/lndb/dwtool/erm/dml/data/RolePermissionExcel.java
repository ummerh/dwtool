package com.lndb.dwtool.erm.dml.data;


public class RolePermissionExcel {
	private String rolePermId;
	private String activeInd;
	private String permId;
	private String roleId;

	public RolePermissionExcel() {
		setActiveInd("Y");
	}
	/**
	 * @return the rolePermId
	 */
	public String getRolePermId() {
		return rolePermId;
	}

	/**
	 * @param rolePermId
	 *            the rolePermId to set
	 */
	public void setRolePermId(String rolePermId) {
		this.rolePermId = rolePermId;
	}


	/**
	 * @return the activeInd
	 */
	public String getActiveInd() {
		return activeInd;
	}

	/**
	 * @param activeInd
	 *            the activeInd to set
	 */
	public void setActiveInd(String activeInd) {
		this.activeInd = activeInd;
	}

	/**
	 * @return the permId
	 */
	public String getPermId() {
		return permId;
	}

	/**
	 * @param permId
	 *            the permId to set
	 */
	public void setPermId(String permId) {
		this.permId = permId;
	}

	/**
	 * @return the roleId
	 */
	public String getRoleId() {
		return roleId;
	}

	/**
	 * @param roleId
	 *            the roleId to set
	 */
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

}
