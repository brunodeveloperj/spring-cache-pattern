package com.mds.cache.service.impl;

import com.mds.cache.provider.CacheProvider;
import com.mds.cache.service.CacheEvictService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link CacheEvictService}.
 *
 * <p>Delegates to the Spring {@link CacheManager} to locate the named
 * cache and invokes {@code clear()} on it, evicting all stored entries.
 * Logs an informational message after successful eviction.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j @Component
public class CacheEvictServiceImpl implements CacheEvictService {

  @Autowired private CacheManager cacheManager;

  @Override
  public void evictCacheValues(String cacheName) {
    Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
    log.info("All values evicted for cache {}", cacheName);
  }
}
