package com.event.infra.event.listener;

import com.event.infra.event.handler.EventHandlerResolver;
import com.event.infra.event.redis.config.RedisStreamConsumerProperties;
import com.event.infra.event.redis.event.RedisCriticalErrorEvent;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.ConsumerStreamReadRequest;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamListenerContainerManager {

  private final RedisStreamConsumerProperties properties;
  private final StringRedisTemplate redisTemplate;
  private final EventHandlerResolver eventHandlerResolver;
  private final ApplicationEventPublisher publisher;
  private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

  /**
   * 컨테이너 초기화 및 스트림 리스너 등록
   */
  @PostConstruct
  public void init() {
    registerListeners(container);
    startContainer();
  }

  /**
   * Redis 컨테이너 시작
   */
  public void startContainer() {
    try {
      container.start();
      log.info("Redis Listener Container has started successfully.");
    } catch (Exception e) {
      log.error("Failed to start Redis Listener Container: {}", e.getMessage(), e);
    }
  }

  /**
   * Redis 컨테이너 중지
   */
  public void stopContainer() {
    try {
      container.stop();
      log.info("Redis Listener Container has been stopped.");
    } catch (Exception e) {
      log.error("Failed to stop Redis Listener Container: {}", e.getMessage(), e);
    }
  }

  /**
   * Redis 컨테이너 (재)시작
   */
  public void restartContainer() {
    try {
      log.info("Restarting Redis Listener Container...");
      stopContainer();
      startContainer();
    } catch (Exception e) {
      log.error("Failed to restart Redis Listener Container: {}", e.getMessage(), e);
    }
  }

  /**
   * Redis 스트림 리스너 등록
   */
  public void registerListeners(StreamMessageListenerContainer<String, MapRecord<String, String, String>> container) {
    for (var consumerSet : properties.consumers()) {
      String streamKey = consumerSet.getStreamKey();
      String groupName = consumerSet.getGroupName();
      String prefix = consumerSet.getConsumerPrefix();
      int consumerCount = consumerSet.getConsumerCount();

      createGroupIfNotExists(streamKey, groupName); // 그룹 존재 여부 확인 및 생성

      for (int i = 1; i <= consumerCount; i++) {
        String consumerName = prefix + "-" + i;

        try {
          var listener = new RedisStreamEventListener(groupName, consumerName, eventHandlerResolver, redisTemplate);
          var request = createListenerRequest(streamKey, groupName, consumerName);

          container.register(request, listener); // 리스너 등록
          log.info("Registered Redis Listener: stream={}, group={}, consumer={}", streamKey, groupName, consumerName);

        } catch (Exception e) {
          log.error("Failed to register Redis Listener: stream={}, group={}, consumer={}, error={}",
                    streamKey, groupName, consumerName, e.getMessage(), e);
        }
      }
    }
  }

  private void createGroupIfNotExists(String streamKey, String groupName) {
    if (Boolean.FALSE.equals(redisTemplate.hasKey(streamKey))) {
      redisTemplate.opsForStream().add(streamKey, Map.of("event_id", "preservedId"));
    }

    try {
      var groups = redisTemplate.execute(conn -> conn.streamCommands().xInfoGroups(streamKey.getBytes()), true);

      if (isNull(groups) || groups.stream().noneMatch(g -> g.groupName().equals(groupName))) {
        redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(), groupName);
        log.info("Created Redis stream group: stream={}, group={}", streamKey, groupName);
      }
    } catch (Exception e) {
      log.warn("Failed to create stream group '{}': {}", groupName, e.getMessage());
    }
  }

  private ConsumerStreamReadRequest<String> createListenerRequest(String streamKey, String groupName, String consumerName) {
    return ConsumerStreamReadRequest.builder(StreamOffset.create(streamKey, ReadOffset.lastConsumed()))
                                    .consumer(Consumer.from(groupName, consumerName))
                                    .cancelOnError(this::handleCriticalError)
                                    .autoAcknowledge(false)
                                    .errorHandler(this::eventRedisError)
                                    .build();
  }

  private boolean handleCriticalError(Throwable error) {
    log.error("Consumer error occurred. Message: {}, Cause: {}", error.getMessage(), error.getCause().getMessage());
    if (isCriticalError(error)) {
      publisher.publishEvent(new RedisCriticalErrorEvent());
      return true; // 컨테이너 중지
    }
    return false; // 컨테이너 유지
  }

  private void eventRedisError(Throwable error) {
    if (error instanceof RedisCommandExecutionException) {
      log.error("RedisCommandExecutionException: {}", error.getMessage());
    } else if (error instanceof RedisCommandTimeoutException) {
      log.error("RedisCommandTimeoutException: {}", error.getMessage());
    } else if (error instanceof RedisSystemException) {
      log.error("RedisSystemException: {}", error.getMessage());
    } else {
      log.error("Unhandled Redis error: {}", error.getMessage(), error);
    }
  }

  private boolean isCriticalError(Throwable error) {
    return error instanceof TimeoutException ||
        error instanceof ConnectException ||
        error instanceof RedisCommandExecutionException ||
        error instanceof RedisConnectionFailureException ||
        error instanceof RedisSystemException;
  }
}