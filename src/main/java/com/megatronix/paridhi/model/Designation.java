package com.megatronix.paridhi.model;

public enum Designation {
	MEMBER("Member"),

	APP_DEV_CO_LEAD("App Development"),
	APP_DEV_LEAD("App Development"),

	FRONTEND_DEV_CO_LEAD("Web"),
	FRONTEND_DEV_LEAD("Web"),
	BACKEND_DEV_CO_LEAD("Web"),
	BACKEND_DEV_LEAD("Web"),
	FULL_STACK_DEV_CO_LEAD("Web"),
	FULL_STACK_DEV_LEAD("Web"),

	AI_ML_DEV_LEAD("AI/ML"),
	AI_ML_DEV_CO_LEAD("AI/ML");

	private final String category;

	Designation(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}
}
