package com.event.infra.event.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RedisStreamEventProducer implements EventProducer {

  private static final String EVENT_STREAM_KEY = "EVENT_STREAM";


  private final StringRedisTemplate stringRedisTemplate;

  @Autowired
  public RedisStreamEventProducer(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * API 호출 시 이벤트 데이터를 Redis Stream에 저장합니다.
   *
   * @param type           로그 유형 (예: "INFO", "ERROR")
   * @param categoryCode   카테고리 코드 (예: "API")
   * @param userId         사용자 ID
   * @param endpoint       호출된 엔드포인트
   * @param httpMethod     HTTP 메서드 (예: "GET", "POST")
   * @param responseStatus HTTP 응답 상태 코드
   * @param responseTime   응답 시간 (ms)
   * @param ipAddress      클라이언트 IP 주소
   * @param userAgent      사용자 에이전트 정보
   * @param inputs         API 호출 시 전달된 입력 값
   * @param outputs        API 호출 후 반환된 출력 값
   */
  @Override
  public void publishEvent(String type,
                           String categoryCode,
                           String userId,
                           String endpoint,
                           String httpMethod,
                           String methodName,
                           int responseStatus,
                           double responseTime,
                           String ipAddress,
                           String userAgent,
                           String inputs,
                           String outputs
  ) {
    try {
      Map<String, String> eventData = new HashMap<>();
      eventData.put("type", type);
      eventData.put("categoryCode", categoryCode);
      eventData.put("userId", userId);
      eventData.put("endpoint", endpoint);
      eventData.put("httpMethod", httpMethod);
      eventData.put("methodName", methodName);
      eventData.put("responseStatus", String.valueOf(responseStatus));
      eventData.put("responseTime", String.format("%.3f", responseTime));
      eventData.put("ipAddress", ipAddress);
      eventData.put("userAgent", userAgent);
      eventData.put("inputs", inputs);
      eventData.put("outputs", outputs);
      eventData.put("createdAt", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

      stringRedisTemplate.opsForStream().add(EVENT_STREAM_KEY, eventData);
      log.info("event published: type: {}, eventData: {}", type, eventData);
    } catch (Exception exception) {
      log.error("Failed to publish event. Type={}, ErrorMessage={}", type, exception.getMessage(), exception);
    }
  }
}