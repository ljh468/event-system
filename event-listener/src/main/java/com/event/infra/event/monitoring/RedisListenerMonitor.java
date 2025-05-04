package com.event.infra.event.monitoring;

import com.event.infra.event.listener.RedisStreamListenerContainerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static java.util.Objects.nonNull;

@Slf4j
public class RedisListenerMonitor implements RunnableMonitor {

  public static final int CHECK_INTERVAL = 10;

  private final HealthChecker healthChecker;
  private final RedisStreamListenerContainerManager containerManager;
  private final TaskScheduler scheduler;
  private ScheduledFuture<?> scheduledTask; // 헬스체크 작업을 관리하는 ScheduledFuture 객체

  public RedisListenerMonitor(
      HealthChecker healthChecker,
      RedisStreamListenerContainerManager containerManager, // 의존성 주입
      TaskScheduler scheduler
  ) {
    this.healthChecker = healthChecker;
    this.containerManager = containerManager;
    this.scheduler = scheduler;
  }

  @Override
  public synchronized void start() {
    if (isMonitoring()) {
      log.debug("Redis listener monitor is already running.");
      return;
    }
    log.info("Starting redis listener health monitoring...");
    scheduledTask = scheduler.scheduleAtFixedRate(this::checkAndRestart, Duration.ofSeconds(CHECK_INTERVAL));
  }

  @Override
  public synchronized void stop() {
    if (isMonitoring()) {
      log.info("Stopping redis listener health monitoring...");
      scheduledTask.cancel(true);
    }
    scheduledTask = null;
  }

  @Override
  public boolean isMonitoring() {
    return nonNull(scheduledTask) && !scheduledTask.isCancelled();
  }

  /**
   * Redis 상태를 점검하고 ListenerContainer 제어
   */
  private void checkAndRestart() {
    boolean redisHealthy = healthChecker.isHealthy();
    log.info("Redis health check result: {}", redisHealthy);

    if (redisHealthy) {
      log.info("Redis is healthy. Restarting ListenerContainer and stopping monitor.");
      containerManager.restartContainer();
      stop(); // 성공적으로 처리되었으면 모니터링 종료
    } else {
      log.warn("Redis is still unhealthy. Will retry...");
    }
  }
}