package com.lndb.dwtool.erm.dml.data;

import com.lndb.dwtool.erm.dml.OracleSqlConstant;

public class RoleMemberExcel {
	private String roleMemberId;
	private String roleId;
	private String memberId;
	private String memberTypeCode;
	private String activeFromDate;
	private String activeToDate;
	private String lastUpdateDate;
	
	public RoleMemberExcel() {
		this.setLastUpdateDate( OracleSqlConstant.SYSDATE);
	}
	
	/**
	 * @return the roleMemberId
	 */
	public String getRoleMemberId() {
		return roleMemberId;
	}
	/**
	 * @param roleMemberId the roleMemberId to set
	 */
	public void setRoleMemberId(String roleMemberId) {
		this.roleMemberId = roleMemberId;
	}
	/**
	 * @return the roleId
	 */
	public String getRoleId() {
		return roleId;
	}
	/**
	 * @param roleId the roleId to set
	 */
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	/**
	 * @return the memberId
	 */
	public String getMemberId() {
		return memberId;
	}
	/**
	 * @param memberId the memberId to set
	 */
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	/**
	 * @return the memberTypeCode
	 */
	public String getMemberTypeCode() {
		return memberTypeCode;
	}
	/**
	 * @param memberTypeCode the memberTypeCode to set
	 */
	public void setMemberTypeCode(String memberTypeCode) {
		this.memberTypeCode = memberTypeCode;
	}
	/**
	 * @return the activeFromDate
	 */
	public String getActiveFromDate() {
		return activeFromDate;
	}
	/**
	 * @param activeFromDate the activeFromDate to set
	 */
	public void setActiveFromDate(String activeFromDate) {
		this.activeFromDate = activeFromDate;
	}
	/**
	 * @return the activeToDate
	 */
	public String getActiveToDate() {
		return activeToDate;
	}
	/**
	 * @param activeToDate the activeToDate to set
	 */
	public void setActiveToDate(String activeToDate) {
		this.activeToDate = activeToDate;
	}

	/**
	 * @return the lastUpdateDate
	 */
	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	/**
	 * @param lastUpdateDate the lastUpdateDate to set
	 */
	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
}
