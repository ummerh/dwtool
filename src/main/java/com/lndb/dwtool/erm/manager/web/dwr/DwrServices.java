package com.lndb.dwtool.erm.manager.web.dwr;

import com.lndb.dwtool.erm.util.StatusMonitor;

public class DwrServices {

    public DwrServices() {
    }

    public String currentStatus(String eventId) {
	String currentStatus = StatusMonitor.currentStatus(eventId);
	if (currentStatus == null) {
	    return "UNKNOWN";
	}
	return currentStatus;
    }

    public Integer currentProgressInd(String eventId) {
	Integer progressInd = StatusMonitor.currentProgressInd(eventId);
	if (progressInd == null) {
	    return 0;
	}
	return progressInd;
    }

}
