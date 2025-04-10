package com.megatronix.paridhi.exception;

public class ContactQueryNotFoundException extends RuntimeException {
	public ContactQueryNotFoundException(String message) {
		super(message);
	}

	public ContactQueryNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
