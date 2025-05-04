package com.event.core.exception.handler;

import com.event.core.exception.DataNotFoundException;
import com.event.core.exception.InvalidDataException;
import com.event.core.exception.ResourceAlreadyExistsException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class HttpStatusMapper {

  private static final Map<Class<? extends Throwable>, HttpStatus> EXCEPTION_STATUS_MAP = new HashMap<>();

  static {
    EXCEPTION_STATUS_MAP.put(InvalidDataException.class, HttpStatus.BAD_REQUEST); // 400
    EXCEPTION_STATUS_MAP.put(DataNotFoundException.class, HttpStatus.NOT_FOUND); // 404
    EXCEPTION_STATUS_MAP.put(ResourceAlreadyExistsException.class, HttpStatus.CONFLICT); // 409
    EXCEPTION_STATUS_MAP.put(Exception.class, HttpStatus.INTERNAL_SERVER_ERROR); // 500
  }

  public static HttpStatus getHttpStatus(Class<? extends Throwable> exceptionClass) {
    return EXCEPTION_STATUS_MAP.getOrDefault(exceptionClass, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}