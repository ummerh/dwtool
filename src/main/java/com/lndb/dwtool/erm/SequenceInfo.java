package com.lndb.dwtool.erm;

public class SequenceInfo {
    private String sequenceName;
    private double minValue;
    private double maxValue;
    private double incrementBy;
    private String cycleFlag;
    private String orderFlag;
    private int cacheSize;
    private double lastNumber;

    public String getSequenceName() {
	return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
	this.sequenceName = sequenceName;
    }

    public double getMinValue() {
	return minValue;
    }

    public void setMinValue(double minValue) {
	this.minValue = minValue;
    }

    public double getMaxValue() {
	return maxValue;
    }

    public void setMaxValue(double maxValue) {
	this.maxValue = maxValue;
    }

    public double getIncrementBy() {
	return incrementBy;
    }

    public void setIncrementBy(double incrementBy) {
	this.incrementBy = incrementBy;
    }

    public String getCycleFlag() {
	return cycleFlag;
    }

    public void setCycleFlag(String cycleFlag) {
	this.cycleFlag = cycleFlag;
    }

    public String getOrderFlag() {
	return orderFlag;
    }

    public void setOrderFlag(String orderFlag) {
	this.orderFlag = orderFlag;
    }

    public int getCacheSize() {
	return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
	this.cacheSize = cacheSize;
    }

    public double getLastNumber() {
	return lastNumber;
    }

    public void setLastNumber(double lastNumber) {
	this.lastNumber = lastNumber;
    }

    public boolean isSame(SequenceInfo other) {
	if (this == other)
	    return true;
	if (other == null)
	    return false;
	if (sequenceName == null) {
	    if (other.sequenceName != null)
		return false;
	} else if (!sequenceName.equals(other.sequenceName))
	    return false;
	return true;
    }

}
