package com.mds.cache.provider.impl;

import com.mds.cache.provider.CacheProvider;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import static java.util.Objects.requireNonNull;

/**
 * {@link CacheProvider} backed by Redis via {@link RedisTemplate}.
 *
 * <p>All operations use Redis hash structures. Serialisation/deserialisation
 * of values to JSON is handled by the caller ({@code CacheServiceImpl});
 * this provider works with raw JSON strings.
 *
 * @author MDS
 * @since 0.0.2
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheProvider implements CacheProvider {

  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public boolean isAvailable() {
    try {
      return requireNonNull(redisTemplate.getConnectionFactory())
          .getConnection().ping() != null;
    } catch (Exception e) {
      log.error("Redis server is not available: {}", e.getMessage());
      return false;
    }
  }

  @Override
  public boolean exists(String key) {
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    } catch (Exception e) {
      log.warn("Failed to check key existence: {}", key);
      return false;
    }
  }

  @Override
  public boolean exists(String key, String hashKey) {
    try {
      return redisTemplate.opsForHash().hasKey(key, hashKey);
    } catch (Exception e) {
      log.warn("Failed to check hash key existence: {}:{}", key, hashKey);
      return false;
    }
  }

  @Override
  public boolean put(String key, String hashKey, String jsonValue) {
    try {
      redisTemplate.opsForHash().put(key, hashKey, jsonValue);
      return true;
    } catch (Exception e) {
      log.error("Failed to put {}:{} — {}", key, hashKey, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean delete(String key, String hashKey) {
    try {
      redisTemplate.opsForHash().delete(key, hashKey);
      return true;
    } catch (Exception e) {
      log.error("Failed to delete {}:{} — {}", key, hashKey, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean delete(String key) {
    try {
      Set<Object> hashKeys = redisTemplate.opsForHash().keys(key);
      hashKeys.forEach(hk -> redisTemplate.opsForHash().delete(key, hk));
      return true;
    } catch (Exception e) {
      log.error("Failed to delete key {} — {}", key, e.getMessage());
      return false;
    }
  }

  @Override
  public List<String> findHashKeys(String key) {
    return redisTemplate.opsForHash().keys(key).stream()
        .filter(obj -> obj instanceof String s && !s.isBlank())
        .map(Object::toString)
        .toList();
  }

  @Override
  public String get(String key, String hashKey) {
    Object value = redisTemplate.opsForHash().get(key, hashKey);
    if (value == null) return null;
    String str = value.toString();
    return (str.isBlank() || "null".equalsIgnoreCase(str)) ? null : str;
  }

  @Override
  public void expire(String key, long timeout, TimeUnit timeUnit) {
    redisTemplate.expire(key, timeout, timeUnit);
  }
}
