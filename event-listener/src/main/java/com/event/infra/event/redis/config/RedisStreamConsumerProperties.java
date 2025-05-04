package com.event.infra.event.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.data.redis.streams")
public record RedisStreamConsumerProperties(List<RedisStreamConsumerSet> consumers) {

  @Data
  public static class RedisStreamConsumerSet {

    private String streamKey;
    private String groupName;
    private String consumerPrefix;
    private int consumerCount;
  }
}