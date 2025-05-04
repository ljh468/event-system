package com.event.core.exception;

import lombok.Getter;

@Getter
public class DataNotFoundException extends RuntimeException {

  public DataNotFoundException(String message) {
    super(message);
  }

  public DataNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
