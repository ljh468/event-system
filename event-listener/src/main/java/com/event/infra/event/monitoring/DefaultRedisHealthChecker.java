package com.event.infra.event.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public class DefaultRedisHealthChecker implements HealthChecker {

  private final RedisConnectionFactory connectionFactory;
  private final StringRedisTemplate redisTemplate;

  private static final String CLUSTER_STATE_OK = "ok";

  /**
   * 클러스터 상태 및 스트림 읽기 가능 여부를 복합적으로 확인
   */
  @Override
  public boolean isHealthy() {
    return isClusterHealthy() && isStreamReadable();
  }

  /**
   * Redis 클러스터가 정상 상태인지 확인
   */
  private boolean isClusterHealthy() {
    try {
      // Redis 클러스터 연결 가져오기
      RedisClusterConnection clusterConnection = connectionFactory.getClusterConnection();

      // 클러스터 상태 정보 가져오기
      ClusterInfo clusterInfo = clusterConnection.clusterCommands().clusterGetClusterInfo();

      // 클러스터 상태가 "ok"인지 확인
      String state = clusterInfo.getState();
      log.info("redis cluster state: {}", state);

      return CLUSTER_STATE_OK.equalsIgnoreCase(state); // 상태가 "ok"면 정상
    } catch (Exception e) {
      log.error("redis cluster state check error: {}, Cause: {}", e.getMessage(), e.getCause().getMessage());
      return false; // 예외가 발생한 경우 비정상
    }
  }

  /**
   * Redis 스트림 데이터가 정상적으로 읽히는지 확인
   */
  @SuppressWarnings("unchecked")
  private boolean isStreamReadable() {
    try {
      // EVENT_STREAM 데이터를 읽기 테스트
      List<ObjectRecord<String, String>> messages = redisTemplate.opsForStream()
                                                                 .read(String.class,
                                                                       StreamReadOptions.empty().count(1),
                                                                       StreamOffset.fromStart("EVENT_STREAM"));

      // 읽은 메시지가 있는지 확인
      boolean isReadable = nonNull(messages) && !messages.isEmpty();
      log.info("redis stream readable: {}", isReadable);
      return isReadable;
    } catch (Exception e) {
      log.error("redis stream read error: {}, Cause: {}", e.getMessage(), e.getCause().getMessage());
      return false;
    }
  }
}