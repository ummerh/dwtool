package com.lndb.dwtool.erm.manager.web.jquery;

import java.util.ArrayList;
import java.util.List;

public class QueryResultView {
    private String title;
    private List<String> headers = new ArrayList<String>();
    private List<List<String>> data = new ArrayList<List<String>>();
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public List<String> getHeaders() {
        return headers;
    }
    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
    public List<List<String>> getData() {
        return data;
    }
    public void setData(List<List<String>> data) {
        this.data = data;
    }
}
