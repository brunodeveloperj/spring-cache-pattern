package com.mds.cache.provider.impl;

import com.mds.cache.provider.CacheProvider;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Two-tier {@link CacheProvider}: Caffeine (L1, local) + Redis (L2, distributed).
 *
 * <h3>Read path</h3>
 * <ol>
 *   <li>Check L1 (Caffeine) — nanosecond latency.</li>
 *   <li>On L1 miss → check L2 (Redis) — millisecond latency.</li>
 *   <li>On L2 hit → backfill L1 for subsequent reads.</li>
 * </ol>
 *
 * <h3>Write path</h3>
 * <p>Writes go to <strong>both</strong> L1 and L2 (write-through).
 *
 * <h3>Delete path</h3>
 * <p>Deletes are applied to both tiers to keep them consistent.
 *
 * <h3>Availability</h3>
 * <p>Returns {@code true} if <em>at least one</em> tier is available.
 * L1 (Caffeine) is always available; L2 (Redis) may be temporarily down.
 *
 * @author MDS
 * @since 0.0.2
 */
@Slf4j
public class CompositeCacheProvider implements CacheProvider {

  private final CacheProvider l1;
  private final CacheProvider l2;

  /**
   * Creates a composite provider.
   *
   * @param l1 local cache (Caffeine)
   * @param l2 distributed cache (Redis)
   */
  public CompositeCacheProvider(CacheProvider l1, CacheProvider l2) {
    this.l1 = l1;
    this.l2 = l2;
  }

  @Override
  public boolean isAvailable() {
    return l1.isAvailable() || l2.isAvailable();
  }

  @Override
  public boolean exists(String key) {
    return l1.exists(key) || l2.exists(key);
  }

  @Override
  public boolean exists(String key, String hashKey) {
    return l1.exists(key, hashKey) || l2.exists(key, hashKey);
  }

  @Override
  public boolean put(String key, String hashKey, String jsonValue) {
    boolean l1Result = l1.put(key, hashKey, jsonValue);
    boolean l2Result = l2.put(key, hashKey, jsonValue);
    return l1Result || l2Result;
  }

  @Override
  public boolean delete(String key, String hashKey) {
    boolean l1Result = l1.delete(key, hashKey);
    boolean l2Result = l2.delete(key, hashKey);
    return l1Result || l2Result;
  }

  @Override
  public boolean delete(String key) {
    boolean l1Result = l1.delete(key);
    boolean l2Result = l2.delete(key);
    return l1Result || l2Result;
  }

  @Override
  public List<String> findHashKeys(String key) {
    List<String> keys = l1.findHashKeys(key);
    if (!keys.isEmpty()) return keys;
    // Fallback to L2
    keys = l2.findHashKeys(key);
    return keys;
  }

  @Override
  public String get(String key, String hashKey) {
    // L1 first
    String value = l1.get(key, hashKey);
    if (value != null) return value;

    // L1 miss → try L2
    value = l2.get(key, hashKey);
    if (value != null) {
      // Backfill L1
      l1.put(key, hashKey, value);
      log.debug("L1 backfill: {}:{}", key, hashKey);
    }
    return value;
  }

  @Override
  public void expire(String key, long timeout, TimeUnit timeUnit) {
    l1.expire(key, timeout, timeUnit);
    l2.expire(key, timeout, timeUnit);
  }
}
