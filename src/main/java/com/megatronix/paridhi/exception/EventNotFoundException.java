package com.megatronix.paridhi.exception;

public class EventNotFoundException extends RuntimeException {
  
	private static final long serialVersionUID = 1L;
	
	public EventNotFoundException(String message) {
    super(message);
  }
  
  public EventNotFoundException(Long eventId) {
    super("Event not found with ID: " + eventId);
  }

}
