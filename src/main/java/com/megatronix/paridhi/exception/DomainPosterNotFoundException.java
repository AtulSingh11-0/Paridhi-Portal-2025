package com.megatronix.paridhi.exception;

public class DomainPosterNotFoundException extends RuntimeException {
	public DomainPosterNotFoundException(String message) {
		super(message);
	}
	
	public DomainPosterNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
