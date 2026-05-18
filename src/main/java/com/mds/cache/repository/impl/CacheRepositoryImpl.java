package com.mds.cache.repository.impl;

import com.mds.cache.enumerator.ScopeEnum;
import com.mds.cache.repository.CacheRepository;
import com.mds.cache.service.CacheService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;


/**
 * Default implementation of {@link CacheRepository}.
 *
 * <p>Delegates every operation to {@link CacheService}, prepending
 * the {@link com.mds.cache.enumerator.ScopeEnum} name as a key prefix
 * (e.g. {@code SESSION_myKey}) when scope-aware methods are called.
 * Async variants are annotated with {@link Async @Async} for
 * non-blocking writes and deletes.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Repository
public class CacheRepositoryImpl implements CacheRepository {

  @Autowired
  private CacheService cacheService;

  private static final String UNDERLINE_SEPARATOR = "_";

  @Override
  public boolean isAvailable() {
    return cacheService.isAvailable();
  }

  @Override
  public <T> boolean addGlobal(final String hashKey, final T object) {
    return cacheService.add(ScopeEnum.GLOBAL.toString(), hashKey, object);
  }

  @Async
  @Override
  public <T> void addGlobalAsync(final String hashKey, final T object) {
    addGlobal(hashKey, object);
  }

  @Override
  public <T> boolean add(final String key, final ScopeEnum scope, final String hashKey, final T object) {
    return cacheService.add(key.concat(UNDERLINE_SEPARATOR).concat(scope.toString()), hashKey, object);
  }

  @Async
  @Override
  public <T> void addAsync(final String key, final ScopeEnum scope, final String hashKey, final T object) {
    add(key, scope, hashKey, object);
  }

  @Override
  public boolean deleteGlobal(final String hashKey) {
    return cacheService.delete(ScopeEnum.GLOBAL.toString(), hashKey);
  }

  @Async
  @Override
  public void deleteGlobalAsync(final String hashKey) {
    deleteGlobal(hashKey);
  }

  @Override
  public boolean delete(final String key, final ScopeEnum scope, final String hashKey) {
    return cacheService.delete(key.concat(UNDERLINE_SEPARATOR).concat(scope.toString()), hashKey);
  }

  @Async
  @Override
  public void deleteAsync(final String key, final ScopeEnum scope, final String hashKey) {
    delete(key, scope, hashKey);
  }

  @Override
  public boolean delete(final String key) {

    boolean isDelete = true;
    for (ScopeEnum current : ScopeEnum.values()) {

      boolean isRemoved = false;
      if (ScopeEnum.GLOBAL == current) {
        isRemoved = deleteGlobal(key);
      } else {
        isRemoved = delete(key, current);
      }

      if (!isRemoved) {
        isDelete = isRemoved;
      }
    }
    return isDelete;
  }

  @Async
  @Override
  public void deleteAsync(final String key) {
    delete(key);
  }

  @Override
  public boolean delete(final String key, final ScopeEnum scope) {
    return cacheService.delete(key.concat(UNDERLINE_SEPARATOR).concat(scope.toString()));
  }

  @Async
  @Override
  public void deleteAsync(final String key, final ScopeEnum scope) {
    delete(key, scope);
  }

  @Override
  public boolean delete(final String key, final ScopeEnum scope, final Set<String> hashKeys) {
    if (hashKeys == null || hashKeys.isEmpty()) {
      return false;
    }

    boolean isDelete = true;

    for (String hashKey : hashKeys) {
      boolean isRemoved = delete(key, scope, hashKey);

      if (!isRemoved) {
        isDelete = isRemoved;
      }
    }
    return isDelete;
  }

  @Async
  @Override
  public void deleteAsync(final String key, final ScopeEnum scope, final Set<String> hashKeys) {
    delete(key, scope, hashKeys);
  }

  @Override
  public boolean delete(final String key, final ScopeEnum scope, final String... hashKeys) {
    if (hashKeys == null || hashKeys.length == 0) {
      return false;
    }
    Set<String> listHashKeys = new HashSet<>(Arrays.asList(hashKeys));
    return delete(key, scope, listHashKeys);
  }

  @Async
  @Override
  public void deleteAsync(final String key, final ScopeEnum scope, final String... hashKeys) {
    delete(key, scope, hashKeys);
  }

  @Override
  public boolean deleteAll(final String key) {
    return delete(key);
  }

  @Async
  @Override
  public void deleteAllAsync(final String key) {
    delete(key);
  }

  @Override
  public boolean deleteAllGlobal() {
    return cacheService.delete(ScopeEnum.GLOBAL.toString());
  }

  @Async
  @Override
  public void deleteAllGlobalAsync() {
    deleteAllGlobal();
  }

  @Override
  public List<String> find(String key) {
    return cacheService.findHashKeys(key);
  }

  @Override
  public <T> List<T> find(final String key, final Class<T> clazz) {
    return cacheService.findObjects(key, clazz);
  }

  @Override
  public <T> T find(final String key, final ScopeEnum scope, final String hashKey, final Class<T> tClass) {
    if (ScopeEnum.GLOBAL == scope) {
      return findGlobal(hashKey, tClass);
    } else {
      return cacheService.findObject(key.concat(UNDERLINE_SEPARATOR).concat(scope.toString()), hashKey, tClass);
    }
  }

  @Override
  public <T> T findGlobal(final String hashKey, final Class<T> tClass) {
    return cacheService.findObject(ScopeEnum.GLOBAL.toString(), hashKey, tClass);
  }

  @Override
  public boolean initializeFunctionality(final String key) {
    return delete(key, ScopeEnum.APPLICATION);
  }

  @Async
  @Override
  public void initializeFunctionalityAsync(final String key) {
    delete(key, ScopeEnum.APPLICATION);
  }
}
