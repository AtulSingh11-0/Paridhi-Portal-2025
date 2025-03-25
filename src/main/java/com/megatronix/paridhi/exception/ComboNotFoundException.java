package com.megatronix.paridhi.exception;

public class ComboNotFoundException extends RuntimeException {
  public ComboNotFoundException(String message) {
    super(message);
  }

  public ComboNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
