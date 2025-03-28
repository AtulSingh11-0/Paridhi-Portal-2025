package com.megatronix.paridhi.exception;

public class GalleryNotFoundException extends RuntimeException {
	public GalleryNotFoundException(String message) {
		super(message);
	}
	
	public GalleryNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
