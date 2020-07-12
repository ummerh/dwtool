package demo.kim.data.diff;

import java.util.ArrayList;
import java.util.List;

public class Role {
	private String roleId, roleNm, desc, nmspcCd, typeId, active, roleMbrId, message = "", typeNmspc, typeName;
	private List<Role> memberRoles = new ArrayList<Role>();

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleNm() {
		return roleNm;
	}

	public void setRoleNm(String roleNm) {
		this.roleNm = roleNm;
	}

	public String getNmspcCd() {
		return nmspcCd;
	}

	public void setNmspcCd(String nmspcCd) {
		this.nmspcCd = nmspcCd;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public boolean matches(Role other) {

		if (this == other)
			return true;
		if (other == null)
			return false;
		if (nmspcCd == null) {
			if (other.nmspcCd != null)
				return false;
		} else if (!nmspcCd.equals(other.nmspcCd))
			return false;
		if (roleNm == null) {
			if (other.roleNm != null)
				return false;
		} else if (!roleNm.equals(other.roleNm))
			return false;
		if (typeId == null) {
			if (other.typeId != null)
				return false;
		} else if (!typeId.equals(other.typeId))
			return false;

		if (!active.equals(other.active)) {
			setMessage("-Active?");
			return false;
		}

		if (memberRoles.size() != other.memberRoles.size()) {
			setMessage("-Members?");
			return false;
		}
		if (!memberRoles.isEmpty()) {
			for (Role mem : memberRoles) {
				boolean matched = false;
				for (Role omem : other.memberRoles) {
					if (mem.matches(omem)) {
						matched = true;
						break;
					}
				}
				if (!matched) {
					setMessage("-Members?");
					return false;
				}
			}
		}
		return true;
	}

	public boolean matchBasic(Role other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (nmspcCd == null) {
			if (other.nmspcCd != null)
				return false;
		} else if (!nmspcCd.equals(other.nmspcCd))
			return false;
		if (roleNm == null) {
			if (other.roleNm != null)
				return false;
		} else if (!roleNm.equals(other.roleNm))
			return false;
		if (typeId == null) {
			if (other.typeId != null)
				return false;
		} else if (!typeId.equals(other.typeId))
			return false;

		return true;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String[] toStrings() {
		return new String[] { nmspcCd, typeNmspc, typeName, roleId, roleNm, desc, active, memberRoles.isEmpty() ? "" : memberRoles.toString(), message };
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Role> getMemberRoles() {
		return memberRoles;
	}

	public void setMemberRoles(List<Role> memberRoles) {
		this.memberRoles = memberRoles;
	}

	@Override
	public String toString() {
		return "Role [nmspcCd=" + nmspcCd + ",roleId=" + roleId + ", roleNm=" + roleNm + (roleMbrId != null ? (", role_mbr_id=" + roleMbrId) : "") + "]";
	}

	public String getRoleMbrId() {
		return roleMbrId;
	}

	public void setRoleMbrId(String roleMbrId) {
		this.roleMbrId = roleMbrId;
	}

	public String getTypeNmspc() {
		return typeNmspc;
	}

	public void setTypeNmspc(String typeNmspc) {
		this.typeNmspc = typeNmspc;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

}
