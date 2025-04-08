package com.megatronix.paridhi.exception;

public class DomainPosterAlreadyExistsExcpetion extends RuntimeException {
	public DomainPosterAlreadyExistsExcpetion(String message) {
		super(message);
	}
	
	public DomainPosterAlreadyExistsExcpetion(String message, Throwable cause) {
		super(message, cause);
	}
	
}
