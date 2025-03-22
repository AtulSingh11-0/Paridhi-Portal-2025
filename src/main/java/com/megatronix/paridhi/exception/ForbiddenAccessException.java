package com.megatronix.paridhi.exception;

public class ForbiddenAccessException extends RuntimeException {
  public ForbiddenAccessException(String message) {
    super(message);
  }
}
