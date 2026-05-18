package com.mds.cache.enumerator;

/**
 * Supported cache provider types.
 *
 * <p>Controls which backend the library uses:
 * <ul>
 *   <li>{@link #REDIS} — distributed cache only (default).</li>
 *   <li>{@link #CAFFEINE} — local in-memory cache only.</li>
 *   <li>{@link #COMPOSITE} — two-tier: Caffeine (L1) + Redis (L2).</li>
 * </ul>
 *
 * @author MDS
 * @since 0.0.2
 */
public enum CacheProviderType {
  REDIS,
  CAFFEINE,
  COMPOSITE
}
