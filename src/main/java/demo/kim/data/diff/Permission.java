package demo.kim.data.diff;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Permission {
	private String permId;
	private String objId;
	private Integer verNbr;
	private String permTmplId;
	private String nmspcCd;
	private String nm;
	private String descTxt;
	private String actvInd;
	private String message;
	private Map<String, String> attrs = new TreeMap<String, String>();

	public String getPermId() {
		return this.permId;
	}

	public void setPermId(String rspId) {

		this.permId = rspId;
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

	public String getPermTmplId() {
		return this.permTmplId;
	}

	public void setPermTmplId(String rspTmplId) {

		this.permTmplId = rspTmplId;
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

	public boolean matches(Permission obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Permission other = (Permission) obj;
		if (actvInd == null) {
			if (other.actvInd != null)
				return false;
		} else if (!actvInd.equals(other.actvInd))
			return false;
		Set<String> attrKeys = this.attrs.keySet();
		for (String k : attrKeys) {
			if (!this.attrs.get(k).equalsIgnoreCase(obj.attrs.get(k))) {
				return false;
			}
		}
		if (nmspcCd == null) {
			if (other.nmspcCd != null)
				return false;
		} else if (!nmspcCd.equals(other.nmspcCd))
			return false;
		if (permTmplId == null) {
			if (other.permTmplId != null)
				return false;
		} else if (!permTmplId.equals(other.permTmplId))
			return false;
		return true;
	}
	
	public boolean matchBasic(Permission other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (nmspcCd == null) {
			if (other.nmspcCd != null)
				return false;
		} else if (!nmspcCd.equals(other.nmspcCd))
			return false;
		if (permTmplId == null) {
			if (other.permTmplId != null)
				return false;
		} else if (!permTmplId.equals(other.permTmplId))
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
		return "Permission [permId=" + permId + ", permTmplId=" + permTmplId + ", nmspcCd=" + nmspcCd + ", nm=" + nm + ", descTxt=" + descTxt + ", actvInd=" + actvInd + ", attrs=" + attrs + "]";
	}

	public String[] toStrings() {
		return new String[] { nmspcCd, permTmplId, permId, nm, descTxt, actvInd, attrs.isEmpty() ? "" : attrs.toString(), message };
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
