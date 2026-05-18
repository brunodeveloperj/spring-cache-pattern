package com.mds.cache.config;

import static com.mds.cache.keys.CacheKeys.EMPTY;
import static com.mds.cache.keys.CacheKeys.HOURS;
import static com.mds.cache.keys.CacheKeys.MINUTES;
import static com.mds.cache.keys.CacheKeys.ONE_INDEX;
import static com.mds.cache.keys.CacheKeys.ONE_MINUTES;
import static com.mds.cache.keys.CacheKeys.SECONDS;
import static java.lang.Long.parseLong;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.springframework.data.redis.cache.RedisCacheWriter.nonLockingRedisCacheWriter;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Custom Redis cache manager configuration.
 *
 * <p>Creates a {@link RedisCacheManager} with a default TTL and per-cache
 * TTL overrides parsed from {@code redis.manager.initial.cache} properties.
 * Values are serialized with Jackson (including Java Time support).
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@Configuration(value = "cache-custom")
@ConditionalOnProperty(prefix = "redis.manager", name = "available", havingValue = "true", matchIfMissing = true)
public class CacheCustomConfig {

  @Value("${redis.manager.default.timeToLive:1m}") String defaultTimeToLive;
  @Value("#{{${redis.manager.initial.cache:{}}}}") Map<String, String> cache = new HashMap<>();

  @Bean("redisCacheObjectMapper")
  public ObjectMapper redisCacheObjectMapper() {
    return JsonMapper.builder()
        .defaultTimeZone(TimeZone.getTimeZone("UTC"))
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("com.mds.")
                .allowIfSubType("java.")
                .build(),
            DefaultTyping.NON_FINAL, As.PROPERTY)
        .build();
  }

  @Bean
  @DependsOn("redisCacheObjectMapper")
  public RedisCacheConfiguration defaultCacheConfiguration(@Qualifier("redisCacheObjectMapper") ObjectMapper redisMapper) {
    return RedisCacheConfiguration.defaultCacheConfig()
                                  .entryTtl(generateDurationCache(defaultTimeToLive))
                                  .serializeValuesWith(fromSerializer(new GenericJacksonJsonRedisSerializer(redisMapper)));
  }

  @Bean
  @DependsOn("redisCacheObjectMapper")
  public Map<String, RedisCacheConfiguration> customCacheConfiguration(@Qualifier("redisCacheObjectMapper") ObjectMapper redisMapper) {
    return cache.entrySet()
                .stream()
                .map(c -> convertCustomCache(c, redisMapper))
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Bean
  @DependsOn({"defaultCacheConfiguration", "customCacheConfiguration"})
  public RedisCacheManager cacheManager(LettuceConnectionFactory lettuceConnectionFactory,
                                        RedisCacheConfiguration defaultCacheConfiguration,
                                        Map<String, RedisCacheConfiguration> customCacheConfiguration) {
    try {
      return new RedisCacheManager(nonLockingRedisCacheWriter(lettuceConnectionFactory), defaultCacheConfiguration, customCacheConfiguration);
    } finally {
      log.info("Redis custom manager is available.");
    }
  }

  private Map<String, RedisCacheConfiguration> convertCustomCache(Entry<String, String> customCache,
                                                                  ObjectMapper redisMapper) {
    String cacheName = customCache.getKey();
    String timeToLive = customCache.getValue();
    Duration duration = generateDurationCache(timeToLive);

    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                                            .entryTtl(duration)
                                                            .serializeValuesWith(fromSerializer(new GenericJacksonJsonRedisSerializer(redisMapper)));
    return Map.of(cacheName, config);
  }

  Duration generateDurationCache(String timeToLive) {
    Duration duration;
    String timeSuffix = timeToLive.substring(timeToLive.length() - ONE_INDEX);
    if(timeSuffix.isBlank()) throw new IllegalArgumentException("Cache lifetime suffix not found.");
    duration = switch (timeSuffix) {
      case HOURS -> ofHours(parseLong(timeToLive.replace(HOURS, EMPTY)));
      case MINUTES -> ofMinutes(parseLong(timeToLive.replace(MINUTES, EMPTY)));
      case SECONDS -> ofSeconds(parseLong(timeToLive.replace(SECONDS, EMPTY)));
      default -> ofMinutes(ONE_MINUTES);
    };
    return duration;
  }

}
