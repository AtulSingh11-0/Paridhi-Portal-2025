package com.megatronix.paridhi.exception;

public class MemberProfileAlreadyExistsException extends RuntimeException {
	public MemberProfileAlreadyExistsException(String message) {
		super(message);
	}

	public MemberProfileAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
