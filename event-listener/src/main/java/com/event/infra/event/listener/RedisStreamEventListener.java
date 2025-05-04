package com.event.infra.event.listener;

import com.event.infra.event.EventType;
import com.event.infra.event.handler.EventHandler;
import com.event.infra.event.handler.EventHandlerResolver;
import com.event.infra.util.JsonParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;

import java.util.Objects;

import static java.util.Objects.nonNull;

@Slf4j
public class RedisStreamEventListener implements
    StreamListener<String, MapRecord<String, String, String>> {

  private final String groupName;
  private final String consumerName;
  private final EventHandlerResolver eventHandlerResolver;
  private final StringRedisTemplate redisTemplate;

  public RedisStreamEventListener(String groupName,
                                  String consumerName,
                                  EventHandlerResolver eventHandlerResolver,
                                  StringRedisTemplate redisTemplate) {
    this.groupName = groupName;
    this.consumerName = consumerName;
    this.eventHandlerResolver = eventHandlerResolver;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void onMessage(MapRecord<String, String, String> record) {
    log.info("Received message. streamKey: {}, consumerName: {}, record: {}", record.getStream(), consumerName, record);

    String type = record.getValue().get("type");
    EventType.safeParse(type).ifPresentOrElse(eventType -> processEventType(record, eventType),
                                              () -> log.warn("Unsupported eventType. Type={}", type)
    );
  }

  private void processEventType(MapRecord<String, String, String> record, EventType eventType) {
    EventHandler handler = eventHandlerResolver.getEventHandler(eventType);

    if (nonNull(handler)) {
      try {
        handler.handleEvent(record);
        acknowledgeEvent(record);

      } catch (JsonParsingException jsonParsingException) {
        log.error("Event parsing failed. Record={}, ErrorMessage={}",
                  record, jsonParsingException.getMessage(), jsonParsingException);

      } catch (Exception exception) {
        log.error("Event handling failed. ClassName={}, ErrorMessage={}",
                  exception.getClass().getName(), exception.getMessage(), exception);

      }
    } else {
      log.warn("No EventHandler found for eventType: {}", eventType);
    }
  }

  private void acknowledgeEvent(MapRecord<String, String, String> record) {
    try {
      Long result = redisTemplate.opsForStream()
                                 .acknowledge(Objects.requireNonNull(record.getStream()),
                                              groupName, record.getId());

      log.info("Acknowledgement success. eventId: {}, Result: {}", record.getId(), result);
    } catch (Exception e) {
      log.error("Acknowledgement failed. eventId: {}, ErrorMessage={}", record.getId(), e.getMessage(), e);
    }
  }
}