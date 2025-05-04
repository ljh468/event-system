package com.event.infra.event.redis.event;

import com.event.infra.event.monitoring.RunnableMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

@Slf4j
public class RedisErrorEventListener {

  private final RunnableMonitor redisListenerMonitor;

  public RedisErrorEventListener(RunnableMonitor redisListenerMonitor) {
    this.redisListenerMonitor = redisListenerMonitor;
  }

  @EventListener
  public void handleRedisListenerErrorEvent(RedisCriticalErrorEvent event) {
    log.warn("RedisListenerErrorEvent detected: {}", event);

    try {
      redisListenerMonitor.start(); // 내부에서 중복 여부를 판단
    } catch (Exception ex) {
      log.error("Failed to start Redis health monitor", ex);
    }
  }
}