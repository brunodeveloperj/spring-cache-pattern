package com.mds.cache.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.util.List;

/**
 * Redis Sentinel connection configuration.
 *
 * <p>Activated when {@code redis.configuration=sentinel}. Creates a
 * {@link RedisSentinelConfiguration} with optional password support.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@EnableConfigurationProperties(DataRedisProperties.class)
@Slf4j
@ConditionalOnProperty(prefix = "redis", name = "configuration", havingValue = "sentinel")
public class CacheSentinelConfig extends CacheAbstractConfig {

    private static final String STR_COMMA = ",";

    @Value("${redis.master:env-redis.master-not-found}") private String redisMaster;

    @Bean
    public RedisSentinelConfiguration redisSentinelConfiguration() {
        try {
            return passwordEnabled ? generateInstanceWithSecret() : generateInstanceWithoutSecret();
        } finally {
            log.info("Redis sentinel is available.");
        }
    }

    @Bean
    public RedisConnectionFactory connectionFactory(RedisSentinelConfiguration redisSentinelConfiguration, LettucePoolingClientConfiguration lettucePoolConfig) {
        return new LettuceConnectionFactory(redisSentinelConfiguration, lettucePoolConfig);
    }

    private RedisSentinelConfiguration generateInstanceWithSecret() {
        List<String> hostInList = convertRedisHostinList();

        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration().master(redisMaster);

        for (String host : hostInList) {
            sentinelConfiguration.sentinel(host, redisPort);
            logInfoPrint(redisMaster, host, redisPort);
        }

        sentinelConfiguration.setPassword(extractSecretValue(secret));

        return sentinelConfiguration;
    }

    private RedisSentinelConfiguration generateInstanceWithoutSecret() {
        logInfoPrint(redisMaster, redisHost, redisPort);
        return new RedisSentinelConfiguration().master(redisMaster).sentinel(redisHost, redisPort);
    }

    private List<String> convertRedisHostinList() {
        return List.of(redisHost.split(STR_COMMA));
    }

}