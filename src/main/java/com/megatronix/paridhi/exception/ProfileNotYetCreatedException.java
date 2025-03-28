package com.megatronix.paridhi.exception;

public class ProfileNotYetCreatedException extends RuntimeException {
	public ProfileNotYetCreatedException(String message) {
		super(message);
	}
	
	public ProfileNotYetCreatedException(String message, Throwable cause) {
		super(message, cause);
	}
}
