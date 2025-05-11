package com.megatronix.paridhi.constant;

public enum Designation {
	MEGATRON(AppConstant.MEGATRON),
	MEMBER(AppConstant.MEMBER),

	APP_DEVELOPER(AppConstant.APP_DEVELOPER),

	FRONTEND_DEVELOPER(AppConstant.FRONTEND_DEVELOPER),
	BACKEND_DEVELOPER(AppConstant.BACKEND_DEVELOPER),
	BACKEND_DEVELOPER_AND_APP_DEVELOPER(AppConstant.BACKEND_DEVELOPER_AND_APP_DEVELOPER),
	FULL_STACK_DEVELOPER(AppConstant.FULL_STACK_DEVELOPER),

	BARA_BHATARI(AppConstant.BARA_BHATARI);

	private final String role;

	Designation(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}
}
