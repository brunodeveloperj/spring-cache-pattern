package com.mds.cache.enumerator;

/**
 * Defines the visibility scope of a Redis cache entry.
 *
 * <p>Used by {@link com.mds.cache.repository.CacheRepository} to partition
 * cached data into logical namespaces:
 * <ul>
 *   <li>{@link #SESSION} — scoped to the current user session.</li>
 *   <li>{@link #APPLICATION} — scoped to the running application instance.</li>
 *   <li>{@link #GLOBAL} — shared across all users and instances.</li>
 * </ul>
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public enum ScopeEnum {

  /**
   * Scope temporary the session, used while the client is logged
   */
  SESSION
  /**
   * Scope temporary the application
   */
  , APPLICATION
  /**
   * Scope global shared among all users
   */
  , GLOBAL

}
