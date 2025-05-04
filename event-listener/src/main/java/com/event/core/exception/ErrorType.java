package com.event.core.exception;

import lombok.Getter;

@Getter
public enum ErrorType {
  UNKNOWN("E000", "알 수 없는 에러입니다."),
  INVALID_DATA("E001", "잘못된 데이터입니다."),
  INVALID_PARAMETER("E002", "잘못된 요청 값 입니다."),
  NO_RESOURCE("E003", "존재하지 않는 리소스입니다."),
  DATA_NOT_FOUND("E004", "데이터를 찾을 수 없습니다."),
  DATA_ALREADY_EXISTS("E005", "데이터가 이미 존재합니다.");


  private final String code;
  private final String description;

  ErrorType(String code, String description) {
    this.code = code;
    this.description = description;
  }
}
