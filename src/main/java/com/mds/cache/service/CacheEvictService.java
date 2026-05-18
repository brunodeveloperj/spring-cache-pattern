package com.mds.cache.service;

/**
 * Service contract for evicting all entries from a named
 * {@link org.springframework.cache.Cache} managed by the
 * {@link org.springframework.cache.CacheManager}.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 * @see com.mds.cache.service.impl.CacheEvictServiceImpl
 */
public interface CacheEvictService {

  void evictCacheValues(String cacheName);
}
