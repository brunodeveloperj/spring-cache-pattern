package com.mds.cache.healthcheck;

import com.mds.cache.service.CacheService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

/**
 * Custom Spring Boot Actuator {@link HealthIndicator} for Redis.
 *
 * <p>On each health probe the check verifies Redis connectivity via
 * {@link CacheService#isAvailable()}, writes a sentinel key if absent,
 * and validates the stored value matches the expected {@code "UP"} marker.
 * The hash key is generated per application instance
 * ({@code appName:UUID}) so multiple instances do not collide.
 *
 * <p>Enabled by default; disable with
 * {@code management.health.custom.redis.enabled=false}.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@Configuration("redisCustomCheck")
@ConditionalOnProperty(prefix = "management.health.custom.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnProperty(name = "cache.provider", havingValue = "redis", matchIfMissing = true)
public class RedisHealthCheck implements HealthIndicator {

  private static final String ERROR = "error";
  private static final String UNKNOWN_HOST = "unknown-host";
  private static final String REDIS_KEY_PREFIX = "redis-health-check";
  private static final String HEALTH_VALUE = "UP";

  private final CacheService cacheService;
  private final String hashKey;

  public RedisHealthCheck(CacheService cacheService,
                          @Value("${spring.application.name}") String appName) {
    this.cacheService = cacheService;
    this.hashKey = resolveHashKey(appName);
  }

  @Override
  public Health health() {
    try {
      boolean available = cacheService.isAvailable();
      if (!available) {
        return healthDown();
      }

      if (!cacheService.exists(REDIS_KEY_PREFIX, hashKey)) {
        cacheService.addUnverified(REDIS_KEY_PREFIX, hashKey, HEALTH_VALUE);
        log.info("[RedisHealthCheck] - (health) : Health/Check - Redis does not have a verification key, adding default key.");
      }

      var value = cacheService.findObjectUnverified(REDIS_KEY_PREFIX, hashKey, String.class);
      if (ObjectUtils.isEmpty(value) || !HEALTH_VALUE.equals(value)) {
        log.warn("[RedisHealthCheck] - (health) : Health/Check - Redis verification key does not match expected value.");
        return healthDown();
      }

      return healthUp();
    } catch (Exception e) {
      log.error("[RedisHealthCheck] - (health) : Health/Check - Redis connection error - {}", e.getMessage(), e);
      return healthDown();
    }
  }

  private static Health healthDown() {
    return Health.down().withDetail(ERROR, "Failed redis connection...").build();
  }

  private static Health healthUp() {
    return Health.up().withDetail("redis_connection", "success").build();
  }

  private String resolveHashKey(String appName) {
    String appNameUUID = UUID.randomUUID().toString();
    return String.format("%s:%s", appName, appNameUUID);
  }
}
