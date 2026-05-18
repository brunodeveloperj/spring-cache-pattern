package com.mds.cache.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

/**
 * Redis Standalone connection configuration.
 *
 * <p>Activated when {@code redis.configuration=standalone}. Creates a
 * {@link RedisStandaloneConfiguration} pointing to a single Redis node
 * with optional password authentication via secret file or plain text.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@EnableConfigurationProperties(DataRedisProperties.class)
@Slf4j
@ConditionalOnProperty(prefix = "redis", name = "configuration", havingValue = "standalone")
public class CacheStandaloneConfig extends CacheAbstractConfig {

  @Bean
  public RedisStandaloneConfiguration redisConfiguration() {
    RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
    redisConfiguration.setHostName(redisHost);
    redisConfiguration.setPort(redisPort);

    if(passwordEnabled) {
      redisConfiguration.setPassword(super.extractSecretValue(secret));
    }
    logInfoPrint(redisHost, redisPort);
    return redisConfiguration;
  }

  @Bean
  public RedisConnectionFactory connectionFactory(RedisStandaloneConfiguration redisConfiguration, LettucePoolingClientConfiguration lettucePoolConfig) {
    return new LettuceConnectionFactory(redisConfiguration, lettucePoolConfig);
  }

}
