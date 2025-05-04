package com.event.infra.scheduler;

import com.event.core.domain.access.event.AccessEventRepository;
import com.event.infra.event.redis.config.RedisStreamConsumerProperties;
import com.event.infra.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Objects.isNull;

@EnableScheduling
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisStreamAndDataBaseClearScheduler {

  private final StringRedisTemplate redisTemplate;
  private final AccessEventRepository accessEventRepository;
  private final RedisStreamConsumerProperties redisStreamConsumerProperties;
  private static final Duration EVENT_RETENTION_PERIOD = Duration.ofDays(60);

  /**
   * NOTE: 오래된 이벤트 정리 스케줄러 - Redis Stream 및 DB 이벤트를 주기적으로 삭제
   */
  @Scheduled(cron = "0 0 10 * * *") // 매일 오전 10시에 실행
  @Transactional
  public void cleanUpExpiredEventData() {
    cleanUpOldRedisStreams();
    cleanUpOldDatabaseEvents();
  }

  protected void cleanUpOldRedisStreams() {
    redisStreamConsumerProperties.consumers().forEach(consumer -> {
      String streamKey = consumer.getStreamKey();
      String groupName = consumer.getGroupName();
      StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();

      log.info("[RedisStream Cleanup Start] streamKey={}, groupName={}", streamKey, groupName);

      acknowledgePendingMessages(ops, streamKey, groupName);
      removeExpiredStreamMessages(ops, streamKey);

      log.info("[RedisStream Complete] streamKey={}, groupName={}", streamKey, groupName);
    });
  }

  private void acknowledgePendingMessages(StreamOperations<String, Object, Object> ops,
                                          String streamKey,
                                          String groupName) {

    PendingMessages pendingMessages = ops.pending(streamKey, groupName, Range.unbounded(), 1000L);

    if (pendingMessages.isEmpty()) {
      log.info("No pending messages to acknowledge in stream: {}", streamKey);
      return;
    }

    String[] idsToAck = pendingMessages.stream()
                                       .map(pending -> pending.getId().getValue())
                                       .toArray(String[]::new);

    // TODO: Pending 메시지 재처리 정책 필요 (메시지를 복구 큐로 보내거나 재처리 시도)
    if (idsToAck.length > 0) {
      Long ackCount = ops.acknowledge(streamKey, groupName, idsToAck);
      log.info("Acknowledged {} pending messages in stream: {}", ackCount, streamKey);
    }
  }

  private void removeExpiredStreamMessages(StreamOperations<String, Object, Object> ops,
                                           String streamKey) {

    String cutoffId = toStreamId(DateTimeUtils.nowKoreaOffset().minus(EVENT_RETENTION_PERIOD));

    // NOTE: "0-0"은 가장 처음에 추가된 메시지의 ID를 의미 (5000개까지 삭제)
    List<MapRecord<String, Object, Object>> expiredRecords =
        ops.range(streamKey, Range.closed("0-0", cutoffId), Limit.limit().count(5000));

    if (isNull(expiredRecords) || expiredRecords.isEmpty() || expiredRecords.size() == 1) {
      log.info("No expired stream messages found in stream: {}", streamKey);
      return;
    }

    // NOTE: 맨 앞 하나 제외하고 삭제 대상 찾기 (모든 메시지가 지워지면 streamKey가 사라짐)
    RecordId[] idsToDelete = expiredRecords.subList(1, expiredRecords.size()).stream()
                                           .map(MapRecord::getId)
                                           .toArray(RecordId[]::new);

    Long deletedCount = ops.delete(streamKey, idsToDelete);
    log.info("Deleted {} expired messages from stream: {} before cutoffId: {}",
             deletedCount, streamKey, cutoffId);
  }

  private String toStreamId(OffsetDateTime offsetDateTime) {
    // nanosecond까지 정확하게 표현하기 위해 toInstant().toEpochMilli() 사용
    long epochMilli = offsetDateTime.toInstant().toEpochMilli();
    long nanoAdjustment = offsetDateTime.getNano() / 1_000_000; // millisecond 이하 부분

    // Redis Stream ID 형식: milliseconds-sequence
    return String.format("%d-%03d", epochMilli, nanoAdjustment);
  }

  private OffsetDateTime fromStreamId(String streamId) {
    String[] parts = streamId.split("-");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid stream ID format: " + streamId);
    }
    long epochMilli = Long.parseLong(parts[0]);
    int nanoAdjustment = Integer.parseInt(parts[1]) * 1_000_000;
    return OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMilli),
                                    java.time.ZoneOffset.UTC).withNano(nanoAdjustment);
  }

  protected void cleanUpOldDatabaseEvents() {
    OffsetDateTime threshold = DateTimeUtils.nowKoreaOffset().minus(EVENT_RETENTION_PERIOD);
    log.info("[Database Cleanup Start] table=access_event, threshold={}", threshold);
    int deletedCount = accessEventRepository.deleteByCreatedAtBefore(threshold);
    log.info("Deleted {} old events from DB before {}", deletedCount, threshold);
    log.info("[Database Cleanup Complete] table=access_event.");
  }
}
