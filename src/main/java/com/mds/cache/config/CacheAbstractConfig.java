package com.mds.cache.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Abstract base configuration for Redis connections using Lettuce.
 *
 * <p>Defines shared connection pool settings, client options, a
 * {@link RedisTemplate} bean, and a secret-extraction helper used by
 * both standalone and sentinel sub-configurations.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
public abstract class CacheAbstractConfig {

  @Value("${redis.host:env-redis.host-not-found}") protected String redisHost;
  @Value("${redis.port:0001}") protected int redisPort;
  @Value("${redis.timeout:900000}") protected int redisTimeout;
  @Value("${redis.pool.max.idle:10}") protected int poolMaxIdle;
  @Value("${redis.pool.min.idle:5}") protected int poolMinIdle;
  @Value("${redis.pool.max.wait:5000}") protected int poolMaxWait;
  @Value("${redis.pool.max.active:20}") protected int poolMaxActive;
  @Value("${redis.test.on.borrow:true}") protected boolean testOnBorrow;
  @Value("${redis.test.while.idle:true}") protected boolean testWhileIdle;
  @Value("${redis.password.secret:env-redis.password-not-found}") protected String secret;
  @Value("${redis.password.enabled:false}") protected boolean passwordEnabled;

  private static final String PREFIX_INFO = "Redis @ {} {}:{}";

  @Bean(destroyMethod = "shutdown")
  public ClientResources clientResources() {
    return DefaultClientResources.create();
  }

  @Bean
  public ClientOptions clientOptions() {
    return ClientOptions.builder()
                        .disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS)
                        .autoReconnect(true)
                        .build();
  }

  @Bean
  public LettucePoolingClientConfiguration lettucePoolConfig(ClientOptions options, ClientResources dcr) {
    GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
    poolConfig.setTestOnReturn(true);
    poolConfig.setMaxIdle(poolMaxIdle);
    poolConfig.setMinIdle(poolMinIdle);
    poolConfig.setMaxWait(Duration.ofMillis(poolMaxWait));
    poolConfig.setMaxTotal(poolMaxActive);
    poolConfig.setTestOnBorrow(testOnBorrow);
    poolConfig.setTestWhileIdle(testWhileIdle);

    try {
      return LettucePoolingClientConfiguration.builder()
                                              .commandTimeout(Duration.ofMillis(redisTimeout))
                                              .poolConfig(poolConfig)
                                              .clientOptions(options)
                                              .clientResources(dcr)
                                              .build();
    } finally {
      log.info("Redis >>poolMaxIdle<<[{}] >>poolMinIdle<<[{}] >>poolMaxWait<<[{}] >>poolMaxActive<<[{}] >>testOnBorrow<<[{}] >>testWhileIdle<<[{}] >>redisTimeout<< [{}]", poolMaxIdle, poolMinIdle, poolMaxWait, poolMaxActive, testOnBorrow, testWhileIdle, redisTimeout);
    }
  }

  @Bean
  @ConditionalOnMissingBean(name = "redisTemplate")
  @Primary
  public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<Object, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    return template;
  }

  /**
   * Tests if the provided secret is an existing file. If it's a file, the contents are read and
   * assumed as the password. Otherwise, we assume the password was passed in plain text.
   *
   * @param secret Path to a file contain the password or the password itself.
   * @return The password to be used for connection.
   */
  RedisPassword extractSecretValue(String secret) {
    Path secretPath = Path.of(secret);
    try {
      return RedisPassword.of(Files.readString(secretPath));
    } catch (IOException e) {
      return RedisPassword.of(secret);
    }
  }

  void logInfoPrint(Object... value) {
    log.info(PREFIX_INFO, value);
  }

}
