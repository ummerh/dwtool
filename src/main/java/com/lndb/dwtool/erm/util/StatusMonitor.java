package com.lndb.dwtool.erm.util;

import java.util.HashMap;

public class StatusMonitor {
    private static HashMap<String, String> STATUS = new HashMap<String, String>();
    private static HashMap<String, Integer> PROGRESS_IND = new HashMap<String, Integer>();

    public static void putStatus(String eventId, String status) {
	STATUS.put(eventId, status);
    }

    public static void removeStatus(String eventId) {
	STATUS.remove(eventId);
    }

    public static String currentStatus(String eventId) {
	return STATUS.get(eventId);
    }

    public static void putProgressInd(String eventId, Integer progress) {
	PROGRESS_IND.put(eventId, progress);
    }

    public static void removeProgressInd(String eventId) {
	PROGRESS_IND.remove(eventId);
    }

    public static Integer currentProgressInd(String eventId) {
	return PROGRESS_IND.get(eventId);
    }
}
