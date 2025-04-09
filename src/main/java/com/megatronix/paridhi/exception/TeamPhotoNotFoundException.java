package com.megatronix.paridhi.exception;

public class TeamPhotoNotFoundException extends RuntimeException {
	public TeamPhotoNotFoundException(String message) {
		super(message);
	}

	public TeamPhotoNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
