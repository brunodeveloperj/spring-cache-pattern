package com.mds.cache.provider;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Backend-agnostic contract for cache operations.
 *
 * <p>Each implementation wraps a specific cache technology
 * (Redis, Caffeine, or a composite of both). The higher-level
 * {@link com.mds.cache.service.CacheService} adds retry, fallback,
 * and scope semantics on top of this interface.
 *
 * <p>All methods follow a hash-style model: entries live under
 * a {@code key} and are identified by a {@code hashKey}.
 *
 * @author MDS
 * @since 0.0.2
 */
public interface CacheProvider {

  /**
   * Checks whether the cache backend is reachable.
   *
   * @return {@code true} if a ping/health check succeeds
   */
  boolean isAvailable();

  /**
   * Checks whether the top-level {@code key} exists.
   *
   * @param key the cache key
   * @return {@code true} if the key exists
   */
  boolean exists(String key);

  /**
   * Checks whether a specific {@code hashKey} exists under {@code key}.
   *
   * @param key     the cache key
   * @param hashKey the hash key within the cache key
   * @return {@code true} if the entry exists
   */
  boolean exists(String key, String hashKey);

  /**
   * Stores a JSON-serialised value under {@code key → hashKey}.
   *
   * @param key       the cache key
   * @param hashKey   the hash key
   * @param jsonValue the value already serialised to JSON
   * @return {@code true} on success
   */
  boolean put(String key, String hashKey, String jsonValue);

  /**
   * Removes a single entry identified by {@code key → hashKey}.
   *
   * @param key     the cache key
   * @param hashKey the hash key
   * @return {@code true} on success
   */
  boolean delete(String key, String hashKey);

  /**
   * Removes an entire key and all its hash entries.
   *
   * @param key the cache key
   * @return {@code true} on success
   */
  boolean delete(String key);

  /**
   * Returns all hash keys stored under {@code key}.
   *
   * @param key the cache key
   * @return list of hash keys (may be empty, never {@code null})
   */
  List<String> findHashKeys(String key);

  /**
   * Returns the raw JSON string stored under {@code key → hashKey}.
   *
   * @param key     the cache key
   * @param hashKey the hash key
   * @return the JSON string, or {@code null} if not found
   */
  String get(String key, String hashKey);

  /**
   * Sets expiration on a key.
   *
   * @param key      the cache key
   * @param timeout  the timeout value
   * @param timeUnit the time unit
   */
  void expire(String key, long timeout, TimeUnit timeUnit);
}
