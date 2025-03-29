package com.megatronix.paridhi.exception;

public class MemberProfileNotFoundException extends RuntimeException {
	public MemberProfileNotFoundException(String message) {
		super(message);
	}

	public MemberProfileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
