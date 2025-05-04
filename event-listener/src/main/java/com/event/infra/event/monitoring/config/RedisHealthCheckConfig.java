package com.event.infra.event.monitoring.config;

import com.event.infra.event.listener.RedisStreamListenerContainerManager;
import com.event.infra.event.monitoring.DefaultRedisHealthChecker;
import com.event.infra.event.monitoring.HealthChecker;
import com.event.infra.event.monitoring.RedisListenerMonitor;
import com.event.infra.event.monitoring.RunnableMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class RedisHealthCheckConfig {

  private static final int TASK_SCHEDULER_POOL_SIZE = 1; // ThreadPool 크기 상수화

  /**
   * ThreadPoolTaskScheduler Bean 정의
   * -> Redis Listener Health Check 작업에 사용
   */
  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(TASK_SCHEDULER_POOL_SIZE); // 스레드 풀 크기 설정
    return scheduler;
  }

  /**
   * RedisConnectionValidator Bean 정의
   * -> Redis 연결 상태와 Stream 데이터를 점검하기 위한 기본 구현체
   *
   * @param connectionFactory RedisConnectionFactory
   * @param redisTemplate     StringRedisTemplate
   *
   * @return RedisConnectionValidator
   */
  @Bean
  public HealthChecker redisConnectionValidator(
      RedisConnectionFactory connectionFactory,
      StringRedisTemplate redisTemplate
  ) {
    return new DefaultRedisHealthChecker(connectionFactory, redisTemplate);
  }

  /**
   * Redis Listener Health Checker 정의
   * -> Redis Listener의 동작 상태를 주기적으로 점검
   *
   * @param containerManager Redis Stream Listener Registrar
   * @param healthChecker    RedisConnectionValidator
   * @param scheduler        ThreadPoolTaskScheduler
   *
   * @return HealthChecker
   */
  @Bean
  public RunnableMonitor redisListenerHealthChecker(
      RedisStreamListenerContainerManager containerManager,
      HealthChecker healthChecker,
      ThreadPoolTaskScheduler scheduler
  ) {
    // HealthChecker 초기화
    return new RedisListenerMonitor(
        healthChecker,
        containerManager,
        scheduler
    );
  }
}