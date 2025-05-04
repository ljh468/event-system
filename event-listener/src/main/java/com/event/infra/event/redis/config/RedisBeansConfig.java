package com.event.infra.event.redis.config;

import com.event.infra.event.monitoring.RunnableMonitor;
import com.event.infra.event.redis.event.RedisErrorEventListener;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RedisBeansConfig {

  private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);

  @Value("${spring.data.redis.cluster.nodes}")
  private List<String> redisClusterNodes;

  @Value("${spring.data.redis.username}")
  private String username;

  @Value("${spring.data.redis.password}")
  private String password;

  @Value("${spring.data.redis.cluster.max-redirects}")
  private int maxRedirects;

  @Value("${spring.data.redis.timeout}")
  private int timeout;

  /**
   * redis 연결 클라이언트 LettuceConnectionFactory 생성
   * @return LettuceConnectionFactory
   */
  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    // Redis 클러스터 구성을 위한 ClusterConfiguration 생성
    RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(
        redisClusterNodes);

    // 사용자 인증이 필요하다면
    if (username != null && !username.isEmpty()) {
      redisClusterConfiguration.setUsername(username);
    }

    if (password != null && !password.isEmpty()) {
      redisClusterConfiguration.setPassword(password);
    }

    redisClusterConfiguration.setMaxRedirects(maxRedirects);

    // Topology 설정
    ClusterTopologyRefreshOptions topologyOptions = ClusterTopologyRefreshOptions.builder()
                                                                                 .enablePeriodicRefresh(Duration.ofMinutes(60))
                                                                                 .refreshTriggersReconnectAttempts(5)
                                                                                 .enableAllAdaptiveRefreshTriggers()
                                                                                 .build();

    SocketOptions socketOptions = SocketOptions.builder()
                                               .keepAlive(true)
                                               .connectTimeout(Duration.ofMillis(timeout))
                                               .build();

    ClientOptions clientOptions = ClusterClientOptions.builder()
                                                      .socketOptions(socketOptions)
                                                      .topologyRefreshOptions(topologyOptions)
                                                      .build();

    // LettuceClientConfiguration을 사용하여 타임아웃 설정
    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                                                                        .clientOptions(clientOptions)
                                                                        .clientResources(lettuceClientResources())
                                                                        .commandTimeout(Duration.ofMillis(timeout))
                                                                        .build();

    // LettuceConnectionFactory에 클러스터 설정과 클라이언트 설정 적용
    return new LettuceConnectionFactory(redisClusterConfiguration, clientConfig);
  }

  /**
   * LettuceClientResources Bean 정의
   * @return ClientResources
   */
  @Bean(destroyMethod = "shutdown")
  public ClientResources lettuceClientResources() {
    NioEventLoopGroup dnsEventLoopGroup = new NioEventLoopGroup(1);

    DnsAddressResolverGroup dnsAddressResolverGroup = new DnsAddressResolverGroup(
        new DnsNameResolverBuilder(dnsEventLoopGroup.next())
            .datagramChannelType(NioDatagramChannel.class)
            .resolveCache(NoopDnsCache.INSTANCE)
            .cnameCache(NoopDnsCnameCache.INSTANCE)
            .authoritativeDnsServerCache(NoopAuthoritativeDnsServerCache.INSTANCE)
            .consolidateCacheSize(0)
    );

    return ClientResources.builder()
                          .addressResolverGroup(dnsAddressResolverGroup)
                          .build();
  }

  /**
   * StringRedisTemplate Bean 정의
   * @param redisConnectionFactory LettuceConnectionFactory
   * @return StringRedisTemplate
   */
  @Bean
  public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
    return new StringRedisTemplate(redisConnectionFactory);
  }

  /**
   * Redis Stream Listener Container Bean 정의
   * @return StreamMessageListenerContainer
   */
  @Bean(destroyMethod = "stop")
  public StreamMessageListenerContainer<String, ?> streamMessageListenerContainer() {
    var options = StreamMessageListenerContainer
        .StreamMessageListenerContainerOptions
        .<String, MapRecord<String, String, String>>builder()
        .pollTimeout(POLL_TIMEOUT)
        .build();
    return StreamMessageListenerContainer.create(redisConnectionFactory(), options);
  }

  /**
   * Redis 에러 이벤트 리스너 Bean 정의
   * @param runnableMonitor HealthChecker
   * @return RedisErrorEventListener
   */
  @Bean
  public RedisErrorEventListener redisErrorEventListener(RunnableMonitor runnableMonitor) {
    return new RedisErrorEventListener(runnableMonitor);
  }
}
