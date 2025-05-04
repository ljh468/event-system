package com.event.core.exception.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.event.core.exception.DataNotFoundException;
import com.event.core.exception.InvalidDataException;
import com.event.core.exception.ResourceAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

import static com.event.core.exception.ErrorType.*;
import static org.springframework.http.HttpStatus.*;
import static java.util.Objects.nonNull;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception exception) {
    log.error("Exception occurred. message={}, className={}", exception.getMessage(), exception.getClass().getName());
    return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                         .body(new ErrorResponse(UNKNOWN.getDescription(), UNKNOWN));

  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleApiException(RuntimeException runtimeException) {
    log.error("RuntimeException occurred. message={}, className={}",
              runtimeException.getMessage(), runtimeException.getClass().getName());
    return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                         .body(new ErrorResponse(runtimeException.getMessage(), UNKNOWN));
  }

  // NOTE: @Valid에서 발생하는 에러를 하기 위해서는 BindException 핸들링 해줘야 함
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleException(BindException bindException) {
    log.error("Bind Exception occurred. message={}, className={}",
              bindException.getMessage(), bindException.getClass().getName());
    return ResponseEntity.status(BAD_REQUEST)
                         .body(new ErrorResponse(createMessage(bindException), INVALID_PARAMETER));
  }

  // NOTE: URL 경로가 적절하지 않을 때 (없는 URL)
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
      NoResourceFoundException noResourceFoundException) {
    log.error("NoResourceFound Exception occurred. message={}, className={}",
              noResourceFoundException.getMessage(), noResourceFoundException.getClass().getName());
    return ResponseEntity.status(BAD_REQUEST)
                         .body(new ErrorResponse(NO_RESOURCE.getDescription(), NO_RESOURCE));
  }

  // NOTE: 파라미터가 비어있을 때
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException servletRequestParameterException) {
    log.error("MissingServletRequestParameter Exception occurred. parameterName={}, message={}, className={}",
              servletRequestParameterException.getParameterName(),
              servletRequestParameterException.getMessage(),
              servletRequestParameterException.getClass().getName());
    return ResponseEntity.status(BAD_REQUEST)
                         .body(new ErrorResponse(INVALID_PARAMETER.getDescription(), INVALID_PARAMETER));
  }

  // NOTE: 파라미터가 적절하지 않을 때
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
    log.error("MethodArgumentTypeMismatch Exception occurred. message={}, className={}",
              methodArgumentTypeMismatchException.getMessage(),
              methodArgumentTypeMismatchException.getClass().getName());
    return ResponseEntity.status(BAD_REQUEST)
                         .body(new ErrorResponse(INVALID_PARAMETER.getDescription(), INVALID_PARAMETER));
  }

  private String createMessage(BindException bindException) {
    // 디폴트 메시지가 있으면 그대로 내려주고,
    if (nonNull(bindException.getFieldError()) && nonNull(bindException.getFieldError().getDefaultMessage())) {
      return bindException.getFieldError().getDefaultMessage(); // .getFieldError는 Nullable
    }

    // 디폴트 메시지가 없으면 만들어야 함
    return bindException.getFieldErrors().stream()
                        .map(FieldError::getField)
                        .collect(Collectors.joining(", ")) + " 값들이 정확하지 않습니다.";
  }

  // NOTE: @RequestBody에서 발생하는 에러를 처리하기 위해서는 HttpMessageNotReadableException 핸들링 해줘야 함
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
    log.error("HttpMessageNotReadableException occurred. message={}, className={}",
              exception.getMessage(), exception.getClass().getName());

    // 내부 cause가 InvalidFormatException이면 처리 분기
    Throwable cause = exception.getCause();
    if (cause instanceof InvalidFormatException invalidFormatException) {
      String fieldName = invalidFormatException.getPath().stream()
                                               .map(JsonMappingException.Reference::getFieldName)
                                               .reduce((first, second) -> second) // 마지막 필드명 가져오기
                                               .orElse("알 수 없음");

      String value = invalidFormatException.getValue().toString();
      String targetType = invalidFormatException.getTargetType().getSimpleName();

      String message = String.format(
          "필드 [%s]에 잘못된 값 [%s]이(가) 들어왔습니다. 허용되는 타입: %s",
          fieldName, value, targetType
      );

      return ResponseEntity
          .status(BAD_REQUEST)
          .body(new ErrorResponse(message, INVALID_DATA));
    }

    return ResponseEntity
        .status(BAD_REQUEST)
        .body(new ErrorResponse(exception.getMessage(), INVALID_DATA));
  }

  @ExceptionHandler(InvalidDataException.class)
  public ResponseEntity<ErrorResponse> handleInvalidDataException(InvalidDataException exception) {
    log.error("InvalidDataException occurred. message={}, className={}",
              exception.getMessage(), exception.getClass().getName());
    return ResponseEntity.status(BAD_REQUEST)
                         .body(new ErrorResponse(exception.getMessage(), INVALID_DATA));
  }

  @ExceptionHandler(DataNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException exception) {
    log.error("DataNotFoundException occurred. message={}, className={}",
              exception.getMessage(), exception.getClass().getName());
    return ResponseEntity.status(NOT_FOUND)
                         .body(new ErrorResponse(exception.getMessage(), DATA_NOT_FOUND));
  }

  @ExceptionHandler(ResourceAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception) {
    log.error("ResourceAlreadyExistsException occurred. message={}, className={}",
              exception.getMessage(), exception.getClass().getName());
    return ResponseEntity.status(BAD_REQUEST)
                         .body(new ErrorResponse(exception.getMessage(), DATA_ALREADY_EXISTS));
  }
}
