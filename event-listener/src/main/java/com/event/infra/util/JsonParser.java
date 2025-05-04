package com.event.infra.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class JsonParser {

  private final ObjectMapper camelCaseObjectMapper;
  private final ObjectMapper snakeCaseObjectMapper;

  @Autowired
  public JsonParser(
      @Qualifier("objectMapper") ObjectMapper camelCaseObjectMapper,
      @Qualifier("snakeCaseObjectMapper") ObjectMapper snakeCaseObjectMapper
  ) {
    this.camelCaseObjectMapper = camelCaseObjectMapper;
    this.snakeCaseObjectMapper = snakeCaseObjectMapper;
  }

  // 일반 JSON 파싱 - camelCase 사용
  public <T> T parseCamel(Map<String, String> map, Class<T> clazz) {
    try {
      String json = camelCaseObjectMapper.writeValueAsString(map);
      return camelCaseObjectMapper.readValue(json, clazz);
    } catch (Exception e) {
      throw new JsonParsingException("CamelCase JSON 파싱 실패: " + clazz.getSimpleName(), e);
    }
  }

  // Redis 등 snake_case 사용
  public <T> T parseSnake(Map<String, String> map, Class<T> clazz) {
    try {
      return snakeCaseObjectMapper.convertValue(map, clazz);
    } catch (Exception e) {
      throw new JsonParsingException("SnakeCase Map 파싱 실패: " + clazz.getSimpleName(), e);
    }
  }

  public String convertObjectToJson(Object obj) {
    try {
      if(isNull(obj)) {
        return null;
      }
      return camelCaseObjectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("Result JSON 변환 중 오류 발생: {}", jsonProcessingException.getMessage(), jsonProcessingException);
    }
    return null;
  }
}
