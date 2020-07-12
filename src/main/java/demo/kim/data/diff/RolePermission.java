package demo.kim.data.diff;

public class RolePermission {
	private String id;
	private Role role;
	private Permission permission;

	public Permission getPermission() {
		return permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	private String message = "-Only";

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String[] toStringArray() {
		return new String[] { role.getNmspcCd(), id, role.getRoleId(), role.getRoleNm(), permission.getNmspcCd(), permission.getPermTmplId(), permission.getPermId(), permission.getNm(),
				permission.getAttrs().toString().replace("{", "").replace("}", ""), message };
	}

	public boolean matches(RolePermission other) {
		if (permission == null) {
			if (other.permission != null)
				return false;
		} else if (!permission.matchBasic(other.permission))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.matchBasic(other.role))
			return false;

		return true;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
