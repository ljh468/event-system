package com.event.core.aop;

import com.event.core.domain.access.category.entity.CategoryCode;
import com.event.core.exception.handler.HttpStatusMapper;
import com.event.infra.event.EventType;
import com.event.infra.event.producer.RedisStreamEventProducer;
import com.event.infra.util.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Aspect
@Component
public class EventAspect {

  public static final String ANONYMOUS_USER_ID = "anonymous";

  private final HttpServletRequest httpServletRequest;
  private final JsonParser jsonParser;
  private final RedisStreamEventProducer redisStreamEventProducer;
  private final ThreadLocal<Long> startTime = new ThreadLocal<>();

  @Autowired
  public EventAspect(HttpServletRequest httpServletRequest,
                     JsonParser jsonParser,
                     RedisStreamEventProducer redisStreamEventProducer) {
    this.httpServletRequest = httpServletRequest;
    this.jsonParser = jsonParser;
    this.redisStreamEventProducer = redisStreamEventProducer;
  }

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void restControllerMethods() {
  }

  @Before("restControllerMethods()")
  public void beforeRequest(JoinPoint joinPoint) {
    startTime.set(System.currentTimeMillis());
    log.info("Start processing: {}", joinPoint.getSignature().toShortString());
  }

  @AfterReturning(pointcut = "restControllerMethods()", returning = "result")
  public void afterRequest(JoinPoint joinPoint, Object result) {
    processEvent(joinPoint, HttpStatus.OK, result, null);
  }

  @AfterThrowing(pointcut = "restControllerMethods()", throwing = "exception")
  public void afterThrowing(JoinPoint joinPoint, Throwable exception) {
    HttpStatus httpStatus = HttpStatusMapper.getHttpStatus(exception.getClass());

    ResponseEntity<Object> resultEntity = ResponseEntity
        .status(httpStatus)
        .body(Map.of(
            "error", exception.getClass().getSimpleName(),
            "message", exception.getMessage()
        ));
    processEvent(joinPoint, httpStatus, resultEntity, exception);
  }

  /**
   * 공통 이벤트 처리 메서드
   */
  private void processEvent(JoinPoint joinPoint, HttpStatus httpStatus, Object result, Throwable exception) {
    try {
      long duration = System.currentTimeMillis() - startTime.get();
      double responseTime = duration / 1000.0;

      String endpoint = httpServletRequest.getRequestURI();
      String httpMethod = httpServletRequest.getMethod();
      String methodName = joinPoint.getSignature().toShortString();
      String ipAddress = httpServletRequest.getRemoteAddr();
      String userAgent = httpServletRequest.getHeader("User-Agent");

      String inputs = convertRequestEntityToJson(joinPoint.getArgs());
      String outputs = convertResponseEntityToJson(result);

      redisStreamEventProducer.publishEvent(
          EventType.ACCESS.name(),
          CategoryCode.determineCategoryFromURI(endpoint),
          ANONYMOUS_USER_ID, // TODO: 임시 ID -> 사용자 ID 처리 필요
          endpoint,
          httpMethod,
          methodName,
          httpStatus.value(),
          responseTime,
          ipAddress,
          userAgent,
          inputs,
          outputs
      );

      if (isNull(exception)) {
        log.info("Completed processing: {}, Response Time: {}s", methodName, responseTime);
      } else {
        log.error("Error in processing: {}, Exception: {}", methodName, exception.getMessage());
      }
    } catch (Exception ex) {
      log.error("Error while processing log: {}", ex.getMessage(), ex);
    } finally {
      startTime.remove();
    }
  }

  private String convertRequestEntityToJson(Object[] args) {
    try {
      if (isNull(args) || args.length == 0) {
        return null;
      }

      return jsonParser.convertObjectToJson(args);
    } catch (Exception ex) {
      log.error("Failed to convert request to JSON: {}", ex.getMessage(), ex);
      return null;
    }
  }

  public String convertResponseEntityToJson(Object result) {
    try {
      if (isNull(result)) {
        return null;
      }

      // ResponseEntity 처리
      if (result instanceof ResponseEntity<?> resultEntity) {
        HttpStatusCode statusCode = resultEntity.getStatusCode();

        return jsonParser.convertObjectToJson(Map.of(
            "statusCode", statusCode.value(),
            "statusMessage", ((HttpStatus) statusCode).getReasonPhrase(),
            "body", nonNull(resultEntity.getBody()) ? resultEntity.getBody() : "null")
        );
      }

      // 단순 타입 & 객체 처리
      return jsonParser.convertObjectToJson(result);

    } catch (Exception exception) {
      log.error("예상치 못한 오류 발생: {}", exception.getMessage(), exception);
    }
    return null;
  }
}