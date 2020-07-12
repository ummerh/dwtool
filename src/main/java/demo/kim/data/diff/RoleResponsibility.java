package demo.kim.data.diff;

public class RoleResponsibility {
	private String id;
	private Role role;
	private Responsibility responsibility;
	private String actionType, actionPolicy, actionForced, actionId;
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

	public Responsibility getResponsibility() {
		return responsibility;
	}

	public void setResponsibility(Responsibility responsibility) {
		this.responsibility = responsibility;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getActionPolicy() {
		return actionPolicy;
	}

	public void setActionPolicy(String actionPolicy) {
		this.actionPolicy = actionPolicy;
	}

	public String getActionForced() {
		return actionForced;
	}

	public void setActionForced(String actionForced) {
		this.actionForced = actionForced;
	}

	public String[] toStringArray() {
		return new String[] { role.getNmspcCd(), id, role.getRoleId(), role.getRoleNm(), responsibility.getNmspcCd(), responsibility.getRspTmplId(), responsibility.getRspId(),
				responsibility.getAttrs().toString().replace('{', (char) 0).replace('}', (char) 0), actionId, actionType, actionPolicy, actionForced, message };
	}

	public boolean matches(RoleResponsibility other) {
		if (responsibility == null) {
			if (other.responsibility != null)
				return false;
		} else if (!responsibility.matchBasic(other.responsibility))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.matchBasic(other.role))
			return false;
		if (actionForced == null) {
			if (other.actionForced != null)
				return false;
		} else if (!actionForced.equals(other.actionForced)) {
			setMessage("-Forced?");
			return false;
		}
		if (actionPolicy == null) {
			if (other.actionPolicy != null)
				return false;
		} else if (!actionPolicy.equals(other.actionPolicy)) {
			setMessage("-Policy?");
			return false;
		}
		if (actionType == null) {
			if (other.actionType != null)
				return false;
		} else if (!actionType.equals(other.actionType)) {
			setMessage("-ActionType?");
			return false;
		}
		return true;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
}
