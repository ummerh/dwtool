package com.lndb.dwtool.erm.dml.data;


public class ResponsibilityAttributeExcel {

	private String rspAttributeId;
	private String value;
	private String typeId;
	private String attributeDefnId;
	private String rspId;

	public ResponsibilityAttributeExcel() {
		setTypeId("7");
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId
	 *            the typeId to set
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the attributeDefnId
	 */
	public String getAttributeDefnId() {
		return attributeDefnId;
	}

	/**
	 * @param attributeDefnId
	 *            the attributeDefnId to set
	 */
	public void setAttributeDefnId(String attributeDefnId) {
		this.attributeDefnId = attributeDefnId;
	}

	/**
	 * @return the rspAttributeId
	 */
	public String getRspAttributeId() {
		return rspAttributeId;
	}

	/**
	 * @param rspAttributeId the rspAttributeId to set
	 */
	public void setRspAttributeId(String rspAttributeId) {
		this.rspAttributeId = rspAttributeId;
	}

	/**
	 * @return the rspId
	 */
	public String getRspId() {
		return rspId;
	}

	/**
	 * @param rspId the rspId to set
	 */
	public void setRspId(String rspId) {
		this.rspId = rspId;
	}

}
