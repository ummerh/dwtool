package demo.kim.data.diff;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Responsibility {
	private String rspId;
	private String objId;
	private Integer verNbr;
	private String rspTmplId;
	private String nmspcCd;
	private String nm;
	private String descTxt;
	private String actvInd;
	private String message;
	private Map<String, String> attrs = new TreeMap<String, String>();

	public String getRspId() {
		return this.rspId;
	}

	public void setRspId(String rspId) {

		this.rspId = rspId;
	}

	public String getObjId() {
		return this.objId;
	}

	public void setObjId(String objId) {

		this.objId = objId;
	}

	public Integer getVerNbr() {
		return this.verNbr;
	}

	public void setVerNbr(Integer verNbr) {

		this.verNbr = verNbr;
	}

	public String getRspTmplId() {
		return this.rspTmplId;
	}

	public void setRspTmplId(String rspTmplId) {

		this.rspTmplId = rspTmplId;
	}

	public String getNmspcCd() {
		return this.nmspcCd;
	}

	public void setNmspcCd(String nmspcCd) {

		this.nmspcCd = nmspcCd;
	}

	public String getNm() {
		return this.nm;
	}

	public void setNm(String nm) {

		this.nm = nm;
	}

	public String getDescTxt() {
		return this.descTxt;
	}

	public void setDescTxt(String descTxt) {

		this.descTxt = descTxt;
	}

	public String getActvInd() {
		return this.actvInd;
	}

	public void setActvInd(String actvInd) {
		this.actvInd = actvInd;
	}

	public boolean matches(Responsibility other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (actvInd == null) {
			if (other.actvInd != null)
				return false;
		} else if (!actvInd.equals(other.actvInd))
			return false;
		if (nmspcCd == null) {
			if (other.nmspcCd != null)
				return false;
		} else if (!nmspcCd.equals(other.nmspcCd))
			return false;
		if (rspTmplId == null) {
			if (other.rspTmplId != null)
				return false;
		} else if (!rspTmplId.equals(other.rspTmplId))
			return false;
		Set<String> attrKeys = this.attrs.keySet();
		for (String k : attrKeys) {
			if (!this.attrs.get(k).equalsIgnoreCase(other.attrs.get(k))) {
				return false;
			}
		}
		return true;
	}

	public boolean matchBasic(Responsibility other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (nmspcCd == null) {
			if (other.nmspcCd != null)
				return false;
		} else if (!nmspcCd.equals(other.nmspcCd))
			return false;
		if (rspTmplId == null) {
			if (other.rspTmplId != null)
				return false;
		} else if (!rspTmplId.equals(other.rspTmplId))
			return false;
		Set<String> attrKeys = this.attrs.keySet();
		for (String k : attrKeys) {
			if (!this.attrs.get(k).equalsIgnoreCase(other.attrs.get(k))) {
				return false;
			}
		}
		return true;
	}

	public Map<String, String> getAttrs() {
		return attrs;
	}

	public void setAttrs(Map<String, String> attrs) {
		this.attrs = attrs;
	}

	@Override
	public String toString() {
		return "Responsibility [rspId=" + rspId + ", rspTmplId=" + rspTmplId + ", nmspcCd=" + nmspcCd + ", nm=" + nm + ", descTxt=" + descTxt + ", actvInd=" + actvInd + ", attrs=" + attrs + "]";
	}

	public String[] toStrings() {
		return new String[] { nmspcCd, rspTmplId, rspId, nm, descTxt, actvInd, attrs.toString(), message };
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
