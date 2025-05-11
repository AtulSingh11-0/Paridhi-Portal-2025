package com.megatronix.paridhi.constant;

public enum Category {
	MEGATRONS(AppConstant.MEGATRONS),
	DEVELOPERS(AppConstant.DEVELOPERS);

	private final String categoryValue;

	Category(String categoryValue) {
		this.categoryValue = categoryValue;
	}

	public String getCategoryValue() {
		return categoryValue;
	}
}
