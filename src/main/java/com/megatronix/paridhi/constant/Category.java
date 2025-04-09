package com.megatronix.paridhi.constant;

public enum Category {
	MEGATRONS("megatrons"),
	DEVELOPERS("developers");

	private final String role;

	Category(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}
}
