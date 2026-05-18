package com.mds.cache.provider.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mds.cache.provider.CacheProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link CacheProvider} backed by Caffeine (local in-memory cache).
 *
 * <p>Simulates Redis hash semantics using a two-level structure:
 * top-level key → Caffeine {@link Cache} of hashKey → JSON value.
 *
 * <p>This provider is always available ({@link #isAvailable()} returns
 * {@code true}) since it operates purely in-process. TTL is applied
 * at cache creation time via Caffeine's {@code expireAfterWrite}.
 *
 * @author MDS
 * @since 0.0.2
 */
@Slf4j
public class CaffeineCacheProvider implements CacheProvider {

  private final ConcurrentMap<String, Cache<String, String>> caches = new ConcurrentHashMap<>();
  private final long defaultTtl;
  private final TimeUnit defaultTtlUnit;
  private final long maxSize;

  /**
   * Creates a new Caffeine-backed provider.
   *
   * @param defaultTtl     default time-to-live for entries
   * @param defaultTtlUnit time unit for the TTL
   * @param maxSize        maximum entries per cache key
   */
  public CaffeineCacheProvider(long defaultTtl, TimeUnit defaultTtlUnit, long maxSize) {
    this.defaultTtl = defaultTtl;
    this.defaultTtlUnit = defaultTtlUnit;
    this.maxSize = maxSize;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public boolean exists(String key) {
    Cache<String, String> cache = caches.get(key);
    return cache != null && !cache.asMap().isEmpty();
  }

  @Override
  public boolean exists(String key, String hashKey) {
    Cache<String, String> cache = caches.get(key);
    return cache != null && cache.getIfPresent(hashKey) != null;
  }

  @Override
  public boolean put(String key, String hashKey, String jsonValue) {
    try {
      getOrCreateCache(key).put(hashKey, jsonValue);
      return true;
    } catch (Exception e) {
      log.error("Caffeine put failed for {}:{} — {}", key, hashKey, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean delete(String key, String hashKey) {
    Cache<String, String> cache = caches.get(key);
    if (cache != null) {
      cache.invalidate(hashKey);
    }
    return true;
  }

  @Override
  public boolean delete(String key) {
    Cache<String, String> cache = caches.remove(key);
    if (cache != null) {
      cache.invalidateAll();
    }
    return true;
  }

  @Override
  public List<String> findHashKeys(String key) {
    Cache<String, String> cache = caches.get(key);
    if (cache == null) return List.of();
    return new ArrayList<>(cache.asMap().keySet());
  }

  @Override
  public String get(String key, String hashKey) {
    Cache<String, String> cache = caches.get(key);
    if (cache == null) return null;
    String value = cache.getIfPresent(hashKey);
    return (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) ? null : value;
  }

  @Override
  public void expire(String key, long timeout, TimeUnit timeUnit) {
    // Caffeine manages expiration internally via expireAfterWrite.
    // Per-key dynamic TTL override is not natively supported;
    // to honour the caller's TTL, we recreate the cache with the new TTL.
    Cache<String, String> existing = caches.get(key);
    if (existing == null) return;

    Map<String, String> snapshot = Map.copyOf(existing.asMap());
    Cache<String, String> replacement = Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfterWrite(timeout, timeUnit)
        .build();
    replacement.putAll(snapshot);
    caches.put(key, replacement);
  }

  private Cache<String, String> getOrCreateCache(String key) {
    return caches.computeIfAbsent(key, k -> Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfterWrite(defaultTtl, defaultTtlUnit)
        .build());
  }
}
