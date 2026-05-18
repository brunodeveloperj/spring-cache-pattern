package com.mds.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.mds.cache.provider.CacheProvider;
import com.mds.cache.provider.impl.CaffeineCacheProvider;
import com.mds.cache.provider.impl.CompositeCacheProvider;
import com.mds.cache.provider.impl.RedisCacheProvider;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Auto-configuration that creates the correct {@link CacheProvider} bean
 * based on the {@code cache.provider} property.
 *
 * <table>
 *   <tr><th>Value</th><th>Provider</th><th>Requirements</th></tr>
 *   <tr><td>{@code redis} (default)</td><td>{@link RedisCacheProvider}</td><td>Redis on classpath + {@code redis.configuration}</td></tr>
 *   <tr><td>{@code caffeine}</td><td>{@link CaffeineCacheProvider}</td><td>Caffeine on classpath</td></tr>
 *   <tr><td>{@code composite}</td><td>{@link CompositeCacheProvider}</td><td>Both on classpath</td></tr>
 * </table>
 *
 * @author MDS
 * @since 0.0.2
 */
@Slf4j
@Configuration
public class CacheProviderAutoConfiguration {

  // ── Redis provider ────────────────────────────────────────────────

  @Bean
  @ConditionalOnProperty(name = "cache.provider", havingValue = "redis", matchIfMissing = true)
  @ConditionalOnMissingBean(CacheProvider.class)
  public CacheProvider redisCacheProvider(RedisTemplate<String, String> redisTemplate) {
    log.info("Cache provider: REDIS");
    return new RedisCacheProvider(redisTemplate);
  }

  // ── Caffeine provider ─────────────────────────────────────────────

  @Bean
  @ConditionalOnProperty(name = "cache.provider", havingValue = "caffeine")
  @ConditionalOnClass(Caffeine.class)
  @ConditionalOnMissingBean(CacheProvider.class)
  public CacheProvider caffeineCacheProvider(
      @Value("${cache.caffeine.ttl-minutes:10}") long ttlMinutes,
      @Value("${cache.caffeine.max-size:10000}") long maxSize) {
    log.info("Cache provider: CAFFEINE (ttl={}m, maxSize={})", ttlMinutes, maxSize);
    return new CaffeineCacheProvider(ttlMinutes, TimeUnit.MINUTES, maxSize);
  }

  // ── Fallback ObjectMapper (when CacheCustomConfig is not loaded) ──

  @Bean("redisCacheObjectMapper")
  @ConditionalOnMissingBean(name = "redisCacheObjectMapper")
  public ObjectMapper redisCacheObjectMapper() {
    return JsonMapper.builder()
        .defaultTimeZone(TimeZone.getTimeZone("UTC"))
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
  }

  // ── Composite provider (L1 Caffeine + L2 Redis) ───────────────────

  @Bean
  @ConditionalOnProperty(name = "cache.provider", havingValue = "composite")
  @ConditionalOnClass(Caffeine.class)
  @ConditionalOnMissingBean(CacheProvider.class)
  public CacheProvider compositeCacheProvider(
      RedisTemplate<String, String> redisTemplate,
      @Value("${cache.caffeine.ttl-minutes:5}") long caffeineTtlMinutes,
      @Value("${cache.caffeine.max-size:10000}") long caffeineMaxSize) {
    log.info("Cache provider: COMPOSITE (L1=Caffeine ttl={}m maxSize={}, L2=Redis)",
        caffeineTtlMinutes, caffeineMaxSize);
    CacheProvider l1 = new CaffeineCacheProvider(caffeineTtlMinutes, TimeUnit.MINUTES, caffeineMaxSize);
    CacheProvider l2 = new RedisCacheProvider(redisTemplate);
    return new CompositeCacheProvider(l1, l2);
  }
}
