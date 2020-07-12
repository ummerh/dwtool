/**
 * 
 */
package com.lndb.dwtool.erm.manager.action;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.lndb.dwtool.erm.util.ExcelConverter;
import com.lndb.dwtool.erm.util.Pager;
import com.lndb.dwtool.erm.util.ExcelConverter.ExcelDataSource;
import com.opensymphony.xwork2.ActionSupport;

import demo.release.control.data.RevisionDataIndexManager;
import demo.release.control.data.web.AerDetail;
import demo.release.control.data.web.AsrDetail;
import demo.release.control.data.web.DttDetail;
import demo.release.control.data.web.FisheyeDetail;
import demo.release.control.data.web.RevisionDataSearcher;

/**
 * 
 */
public class RevisionDataAction extends ActionSupport {
	private static final int FISHEYE_DEFAULT_PAGE_SIZE = 50;
	private static Comparator<AerDetail> aerIdComparator = new Comparator<AerDetail>() {
		public int compare(AerDetail o1, AerDetail o2) {
			return Integer.valueOf(o1.getAerid()).compareTo(Integer.valueOf(o2.getAerid()));
		}
	};
	private static Comparator<AsrDetail> asrIdComparator = new Comparator<AsrDetail>() {
		public int compare(AsrDetail o1, AsrDetail o2) {
			return Integer.valueOf(o1.getItemid()).compareTo(Integer.valueOf(o2.getItemid()));
		}
	};
	private static Comparator<DttDetail> dttIdComparator = new Comparator<DttDetail>() {
		public int compare(DttDetail o1, DttDetail o2) {
			return Integer.valueOf(o1.getItemid()).compareTo(Integer.valueOf(o2.getItemid()));
		}
	};
	private static final Comparator<FisheyeDetail> fisheyeComparator = new Comparator<FisheyeDetail>() {
		public int compare(FisheyeDetail o1, FisheyeDetail o2) {
			return o1.getId().compareTo(o2.getId());
		}
	};
	private static Integer pageIdGenerator = 0;
	private static final HashMap<String, String> prjOrderMap = new HashMap<String, String>();
	private static final String REPORT = "report";

	private static final long serialVersionUID = -2483549934797643795L;

	private static final int SUGGEST_MAX_SIZE = 75;
	static {
		prjOrderMap.put("rice", "1");
		prjOrderMap.put("kfs", "2");
		prjOrderMap.put("kmm", "3");
		prjOrderMap.put("ooi", "4");
	}
	private List<AerDetail> aerDetails;
	private String aerResult;
	private List<AsrDetail> asrDetails;
	private String asrResult;
	private List<DttDetail> dttDetails;
	private String dttResult;

	private List<FisheyeDetail> fisheyeDetails;

	private Pager<FisheyeDetail> fisheyePages;

	private String fisheyeResult;
	private String pageId;
	private int pageNumber = 1;
	private String project;

	private String sortBy;

	private String term;

	public String displayInput() throws Exception {
		return INPUT;
	}

	public List<AerDetail> getAerDetails() {
		// add sorting by aer id
		Collections.sort(aerDetails, aerIdComparator);
		return aerDetails;
	}

	public String getAerResult() {
		return aerResult;
	}

	public List<AsrDetail> getAsrDetails() {
		Collections.sort(asrDetails, asrIdComparator);
		return asrDetails;
	}

	public String getAsrResult() {
		return asrResult;
	}

	public List<DttDetail> getDttDetails() {
		Collections.sort(dttDetails, dttIdComparator);
		return dttDetails;
	}

	public String getDttResult() {
		return dttResult;
	}

	public List<FisheyeDetail> getFisheyeDetails() {
		Collections.sort(fisheyeDetails, fisheyeComparator);
		return fisheyeDetails;
	}

	public Pager<FisheyeDetail> getFisheyePages() {
		return fisheyePages;
	}

	public String getFisheyeResult() {
		return fisheyeResult;
	}

	public String getPageId() {
		return pageId;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public String getProject() {
		return project;
	}

	public String getSortBy() {
		return sortBy;
	}

	public String getTerm() {
		return term;
	}

	public String indexData() throws Exception {
		RevisionDataIndexManager.index();
		RevisionDataSearcher.reset();
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.write("Indexing finished successfully");
		pw.flush();
		return null;
	}

	@SuppressWarnings("unchecked")
	public String page() throws Exception {
		fisheyePages = (Pager<FisheyeDetail>) ServletActionContext.getRequest().getSession().getAttribute(pageId);
		if (fisheyePages == null || pageNumber == 0) {
			return displayInput();
		}
		fisheyeDetails = fisheyePages.getPage(pageNumber);
		return "fisheye_page";
	}

	public String reportByProject() throws Exception {
		if (project != null) {
			RevisionDataSearcher.init();
			aerDetails = RevisionDataSearcher.getAersByProject(project);
			dttDetails = RevisionDataSearcher.getDttsByProject(project);
			asrDetails = RevisionDataSearcher.getAsrsByProject(project);
		}
		return REPORT;
	}

	public String search() throws Exception {
		HashSet<String> aerIds = new HashSet<String>();
		HashSet<String> dttIds = new HashSet<String>();
		HashSet<String> asrIds = new HashSet<String>();
		HashSet<String> fisheyeIds = new HashSet<String>();
		RevisionDataSearcher.init();
		RevisionDataSearcher.buildSearchResultKeys(term, aerIds, dttIds, asrIds, fisheyeIds);
		aerDetails = RevisionDataSearcher.buildAERDetails(aerIds);
		dttDetails = RevisionDataSearcher.buildDTTDetails(dttIds);
		asrDetails = RevisionDataSearcher.buildASRDetails(asrIds);
		fisheyePages = new Pager<FisheyeDetail>(RevisionDataSearcher.buildFisheyeDetails(fisheyeIds), 50);
		pageId = "" + pageIdGenerator++;
		fisheyePages.setPageId(pageId);
		ServletActionContext.getRequest().getSession().setAttribute(pageId, fisheyePages);
		fisheyeDetails = fisheyePages.getPage(pageNumber);

		if (aerDetails.isEmpty()) {
			aerResult = "No AER match found.";
		}
		if (dttDetails.isEmpty()) {
			dttResult = "No DTT match found.";
		}
		if (asrDetails.isEmpty()) {
			asrResult = "No ASR match found.";
		}
		if (fisheyeDetails.isEmpty()) {
			fisheyeResult = "No fisheye match found.";
		}
		return SUCCESS;
	}

	public void setAerDetails(List<AerDetail> aerDetails) {
		this.aerDetails = aerDetails;
	}

	public void setAerResult(String aerResult) {
		this.aerResult = aerResult;
	}

	public void setAsrDetails(List<AsrDetail> asrDetails) {
		this.asrDetails = asrDetails;
	}

	public void setAsrResult(String asrResult) {
		this.asrResult = asrResult;
	}

	public void setDttDetails(List<DttDetail> dttDetails) {
		this.dttDetails = dttDetails;
	}

	public void setDttResult(String dttResult) {
		this.dttResult = dttResult;
	}

	public void setFisheyeDetails(List<FisheyeDetail> fisheyeDetails) {
		this.fisheyeDetails = fisheyeDetails;
	}

	public void setFisheyePages(Pager<FisheyeDetail> fisheyePages) {
		this.fisheyePages = fisheyePages;
	}

	public void setFisheyeResult(String fisheyeResult) {
		this.fisheyeResult = fisheyeResult;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String sortRevisions() throws Exception {
		fisheyePages = (Pager<FisheyeDetail>) ServletActionContext.getRequest().getSession().getAttribute(pageId);
		List<FisheyeDetail> fullList = fisheyePages.getList();
		if ("csid".equals(sortBy)) {
			if ("csid".equals(fisheyePages.getSortBy())) {
				Collections.reverse(fullList);
			} else {
				Collections.sort(fullList, new Comparator<FisheyeDetail>() {
					public int compare(FisheyeDetail o1, FisheyeDetail o2) {
						return o1.getCsid().compareTo(o2.getCsid());
					};
				});
			}
		} else if ("path".equals(sortBy)) {
			if ("path".equals(fisheyePages.getSortBy())) {
				Collections.reverse(fullList);
			} else {
				Collections.sort(fullList, new Comparator<FisheyeDetail>() {
					public int compare(FisheyeDetail o1, FisheyeDetail o2) {
						return o1.getPath().compareTo(o2.getPath());
					};
				});
			}
		}
		fisheyePages = new Pager<FisheyeDetail>(fullList, FISHEYE_DEFAULT_PAGE_SIZE);
		fisheyePages.setSortBy(sortBy);
		pageId = "" + pageIdGenerator++;
		fisheyePages.setPageId(pageId);
		ServletActionContext.getRequest().getSession().setAttribute(pageId, fisheyePages);
		pageNumber = 1;
		fisheyeDetails = fisheyePages.getPage(pageNumber);
		return "fisheye_page";
	}

	public String exportRevisions() throws Exception {
		fisheyePages = (Pager<FisheyeDetail>) ServletActionContext.getRequest().getSession().getAttribute(pageId);
		final List<FisheyeDetail> fullList = fisheyePages.getList();
		if (fullList == null || fullList.isEmpty()) {
			return null;
		}
		HttpServletResponse response = ServletActionContext.getResponse();
		String fileName = "fisheye_revs.xls";
		response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
		ExcelConverter converter = new ExcelConverter();
		converter.writeOut(response.getOutputStream(), new ExcelDataSource() {
			private Iterator<FisheyeDetail> details;

			public String[] nextRow() {
				if (details == null) {
					details = fullList.iterator();
				}
				FisheyeDetail next = details.next();
				String[] data = new String[5];
				data[0] = next.getProject();
				data[1] = next.getCsid();
				data[2] = next.getComments();
				data[3] = next.getPath();
				data[4] = "http://fisheye.lndb.com/changelog/" + next.getProject() + "?cs=" + next.getCsid();
				return data;
			}

			public boolean hasNext() {
				if (details == null) {
					details = fullList.iterator();
				}
				return details.hasNext();
			}

			public String[] getHeaders() {
				return new String[] { "Project", "CSID", "Comments", "Path", "Link" };
			}
		});
		return null;
	}

	public String suggestInput() throws Exception {
		ArrayList<String> suggestions = new ArrayList<String>();
		search();
		if (!getAerDetails().isEmpty()) {
			for (AerDetail aerDtl : aerDetails) {
				String title = aerDtl.getTitle();
				if (title.length() > SUGGEST_MAX_SIZE) {
					title = title.substring(0, SUGGEST_MAX_SIZE);
				}
				suggestions.add("{\"id\":\"" + aerDtl.getAerid() + "\", \"label\":\"(AER) " + title + "\", \"value\":\""
						+ RevisionDataIndexManager.extractAERKey(aerDtl.getTitle(), aerDtl.getItemnumber()) + "\"}");
			}
		}
		if (!getDttDetails().isEmpty()) {
			for (DttDetail dttDtl : dttDetails) {
				String defectname = dttDtl.getDefectname();
				if (defectname.length() > SUGGEST_MAX_SIZE) {
					defectname = defectname.substring(0, SUGGEST_MAX_SIZE);
				}
				suggestions.add("{\"id\":\"" + dttDtl.getItemid() + "\", \"label\":\"(DTT-" + dttDtl.getItemid() + ") " + defectname + "\", \"value\":\"DTT " + dttDtl.getItemid() + "\"}");
			}
		}
		if (!getAsrDetails().isEmpty()) {
			for (AsrDetail detail : asrDetails) {
				String title = detail.getTitle();
				if (title.length() > SUGGEST_MAX_SIZE) {
					title = title.substring(0, SUGGEST_MAX_SIZE);
				}
				suggestions.add("{\"id\":\"" + detail.getItemid() + "\", \"label\":\"(ASR-" + detail.getItemid() + ") " + title + "\", \"value\":\"" + detail.getItemid() + "\"}");
			}
		}
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.write(suggestions.toString());
		pw.flush();
		return null;
	}

}
