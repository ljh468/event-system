package com.event.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

// NOTE: 기본 camelCase용과 Redis용 snake_case ObjectMapper를 둘 다 등록
@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new ParameterNamesModule()) // 생성자 파라미터 이름으로 매핑하기 위해 등록 (records 사용 시 필요)
        .registerModule(new JavaTimeModule()) // LocalDateTime 등의 Java 8 날짜/시간 타입 지원
        .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE) // JSON 필드명을 camelCase 스타일로 직렬화/역직렬화
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // 정의되지 않은 필드가 있어도 무시하고 예외 발생하지 않도록 설정
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 날짜를 timestamp(숫자)가 아니라 문자열로 처리 (ISO 8601)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE); // 타임존 자동 보정 비활성화 (원본 시간값 그대로 유지)
  }
  
  @Bean("snakeCaseObjectMapper")
  public ObjectMapper snakeCaseObjectMapper() {
    return new ObjectMapper()
        .registerModule(new ParameterNamesModule())
        .registerModule(new JavaTimeModule())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
  }
}
