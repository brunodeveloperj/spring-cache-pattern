package com.mds.cache.service.impl;

import static com.mds.cache.enumerator.LogLevel.DEBUG;
import static com.mds.cache.enumerator.LogLevel.ERROR;
import static com.mds.cache.enumerator.LogLevel.INFO;
import static com.mds.cache.enumerator.LogLevel.WARN;

import tools.jackson.databind.ObjectMapper;
import com.mds.cache.enumerator.ScopeEnum;
import com.mds.cache.exception.RetryFallbackFalseException;
import com.mds.cache.exception.RetryFallbackNullException;
import com.mds.cache.factory.LoggerFactory;
import com.mds.cache.provider.CacheProvider;
import com.mds.cache.service.CacheService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Backend-agnostic implementation of {@link CacheService} that delegates
 * all raw cache operations to a {@link CacheProvider}.
 *
 * <p>This class retains responsibility for:
 * <ul>
 *   <li>JSON serialisation/deserialisation via the injected
 *       {@code redisCacheObjectMapper} bean.</li>
 *   <li>Spring Retry / Recover fallback semantics.</li>
 *   <li>Scope-based TTL resolution (SESSION, APPLICATION, GLOBAL).</li>
 *   <li>Availability-gated operations ({@code add}, {@code findObject},
 *       {@code delete}).</li>
 * </ul>
 *
 * <p>Entries auto-expire based on their scope:
 * <ul>
 *   <li>SESSION — {@code cache.expire.session.in.minutes} (default 10)</li>
 *   <li>APPLICATION — {@code cache.expire.application.in.minutes} (default 60)</li>
 *   <li>GLOBAL — {@code cache.expire.global.in.minutes} (default -1, no expiration)</li>
 * </ul>
 *
 * @author MDS
 * @since 0.0.2
 */
@Service
public class CacheServiceImpl implements CacheService {

  @Value("${cache.expire.session.in.minutes:${redis.expire.session.in.minutes:10}}")
  private int expireSession;
  @Value("${cache.expire.application.in.minutes:${redis.expire.application.in.minutes:60}}")
  private int expireApplication;
  @Value("${cache.expire.global.in.minutes:${redis.expire.global.in.minutes:-1}}")
  private int expireGlobal;
  @Value("${cache.retry.enable:${redis.retry.enable:false}}")
  private boolean retryEnable;
  private final LoggerFactory logger;
  private final CacheProvider provider;
  private final ObjectMapper objectMapper;

  public CacheServiceImpl(LoggerFactory logger,
                          CacheProvider provider,
                          @Qualifier("redisCacheObjectMapper") ObjectMapper objectMapper) {
    this.logger = logger;
    this.provider = provider;
    this.objectMapper = objectMapper;
  }

  @Retryable(retryFor = {RetryFallbackFalseException.class},
             maxAttemptsExpression = "#{${cache.retry.max.attempts:${redis.retry.max.attempts:3}}}",
             backoff = @Backoff(delayExpression = "#{${cache.retry.delay:${redis.retry.delay:1000}}}"))
  @Override
  public boolean isAvailable() {
    try {
      return provider.isAvailable();
    } catch (Exception e) {
      logger.log(ERROR, "Cache provider is not available at the moment.", e);
      if (retryEnable) {
        throw new RetryFallbackFalseException("Retrying in CacheServiceImpl.isAvailable", e);
      }
    }
    return false;
  }

  @Override
  public boolean exists(String key) {
    try {
      return provider.exists(key);
    } catch (Exception e) {
      logger.log(WARN, "Key is not available.");
      return false;
    }
  }

  @Override
  public boolean exists(String key, String hashKey) {
    try {
      return provider.exists(key, hashKey);
    } catch (Exception e) {
      logger.log(WARN, "Key or HashKey is not available.");
      return false;
    }
  }

  @Override
  public <T> boolean add(final String key, final String hashKey, final T object) {
    boolean isAvailable = isAvailable();
    logger.log(INFO, "[add] >>isAvailable: [{}]", isAvailable);

    if (isAvailable) {
      return addUnverified(key, hashKey, object);
    }

    return false;
  }

  @Retryable(retryFor = {RetryFallbackFalseException.class},
             maxAttemptsExpression = "#{${cache.retry.max.attempts:${redis.retry.max.attempts:3}}}",
             backoff = @Backoff(delayExpression = "#{${cache.retry.delay:${redis.retry.delay:1000}}}"))
  @Override
  public <T> boolean addUnverified(String key, String hashKey, T object) {
    if (object == null) {
      logger.log(ERROR, "Object to add in cache is null. Key: {}, HashKey: {}", key, hashKey);
      return false;
    }
    try {
      String jsonObject = (object instanceof String value && !value.isBlank() && !value.equalsIgnoreCase("null")) ? value : objectMapper.writeValueAsString(object);
      logger.log(INFO, "[add] >>key: [{}]<< >>hashKey: [{}]<< >>object: [{}]<<", key, hashKey, jsonObject);

      provider.put(key, hashKey, jsonObject);
      applyExpiration(key);

      return true;
    } catch (Exception e) {
      logger.log(ERROR, "Unable to add object of key {} to cache collection '{}': {} >>{}<<", hashKey, key, e.getMessage(), e);
      if (retryEnable) {
        throw new RetryFallbackFalseException("Retrying in CacheServiceImpl.add", e);
      }
    }
    return false;
  }

  @Retryable(retryFor = {RetryFallbackFalseException.class},
             maxAttemptsExpression = "#{${cache.retry.max.attempts:${redis.retry.max.attempts:3}}}",
             backoff = @Backoff(delayExpression = "#{${cache.retry.delay:${redis.retry.delay:1000}}}"))
  @Override
  public boolean delete(final String key, final String hashKey) {
    try {
      boolean isAvailable = isAvailable();
      logger.log(INFO, "[delete] >>isAvailable: [{}]<< >>key: [{}]<< >>hashKey: [{}]<<", isAvailable, key, hashKey);

      if (isAvailable) {
        provider.delete(key, hashKey);
        applyExpiration(key);
        return true;
      }
    } catch (RetryFallbackFalseException e) {
      throw e;
    } catch (Exception e) {
      logger.log(ERROR, "Unable to delete entry {} from cache collection '{}': {} >>{}<<", hashKey, key, e.getMessage(), e);
      if (retryEnable) {
        throw new RetryFallbackFalseException("Retrying in CacheServiceImpl.delete", e);
      }
    }
    return false;
  }

  @Retryable(retryFor = {RetryFallbackFalseException.class},
             maxAttemptsExpression = "#{${cache.retry.max.attempts:${redis.retry.max.attempts:3}}}",
             backoff = @Backoff(delayExpression = "#{${cache.retry.delay:${redis.retry.delay:1000}}}"))
  @Override
  public boolean delete(final String key) {
    try {
      boolean isAvailable = isAvailable();
      logger.log(INFO, "[delete] >>isAvailable: [{}]<< >>key: [{}]<< ", isAvailable, key);

      if (isAvailable) {
        provider.delete(key);
        applyExpiration(key);
        return true;
      }
    } catch (RetryFallbackFalseException e) {
      throw e;
    } catch (Exception e) {
      logger.log(ERROR, "Unable to delete key {} from cache: {} >>{}<<", key, e.getMessage(), e);
      if (retryEnable) {
        throw new RetryFallbackFalseException("Retrying in CacheServiceImpl.delete", e);
      }
    }
    return false;
  }

  @Retryable(retryFor = {RetryFallbackNullException.class},
             maxAttemptsExpression = "#{${cache.retry.max.attempts:${redis.retry.max.attempts:3}}}",
             backoff = @Backoff(delayExpression = "#{${cache.retry.delay:${redis.retry.delay:1000}}}"))
  @Override
  public List<String> findHashKeys(String key) {
    applyExpiration(key);
    try {
      return provider.findHashKeys(key);
    } catch (Exception e) {
      logger.log(ERROR, "Error finding hash keys for key: {} >>{}<<", key, e.getMessage(), e);
      if (retryEnable) {
        throw new RetryFallbackNullException("Error finding hash keys for key: " + key, e);
      }
      return List.of();
    }
  }

  @Override
  public <T> List<T> findObjects(String key, Class<T> clazz) {
    List<String> targetKeys = findHashKeys(key);
    List<T> responseKeys = new ArrayList<>(targetKeys.size());
    targetKeys.forEach(hashKey -> {
      T response = findObjectUnverified(key, hashKey, clazz);
      responseKeys.add(response);
    });
    return responseKeys;
  }

  @Retryable(retryFor = {RetryFallbackNullException.class},
             maxAttemptsExpression = "#{${cache.retry.max.attempts:${redis.retry.max.attempts:3}}}",
             backoff = @Backoff(delayExpression = "#{${cache.retry.delay:${redis.retry.delay:1000}}}"))
  @Override
  public <T> T findObjectUnverified(String key, String hashKey, Class<T> tClass) {
    try {
      String jsonObj = provider.get(key, hashKey);
      if (jsonObj == null) {
        logger.log(ERROR, "Entry '{}' does not exist in cache >>{}<<", hashKey, key);
        return null;
      }

      logger.log(DEBUG, "[find] >>tClass: [{}]<< >>jsonObj: [{}]<< ", tClass, jsonObj);
      applyExpiration(key);
      return tClass == String.class ? tClass.cast(jsonObj) : objectMapper.readValue(jsonObj, tClass);
    } catch (Exception e) {
      logger.log(ERROR, "Unable to find entry '{}' in cache collection '{}': {} >>{}<<", hashKey, key, e.getMessage(), e);
      if (retryEnable) {
        throw new RetryFallbackNullException("Retrying in CacheServiceImpl.find", e);
      }
    }
    return null;
  }

  @Override
  public <T> T findObject(String key, String hashKey, Class<T> tClass) {
    boolean isAvailable = isAvailable();
    logger.log(INFO, "[find] >>isAvailable: [{}]<< >>key: [{}]<< >>hashKey: [{}]<< >>tClass: [{}]<<", isAvailable, key, hashKey, tClass);
    if (isAvailable) {
      return findObjectUnverified(key, hashKey, tClass);
    }
    return null;
  }

  private void applyExpiration(String key) {
    int ttl = resolveExpirationMinutes(key);
    if (ttl > 0) {
      provider.expire(key, ttl, TimeUnit.MINUTES);
    }
  }

  private int resolveExpirationMinutes(String key) {
    if (key.endsWith("_" + ScopeEnum.GLOBAL.name())
        || key.equals(ScopeEnum.GLOBAL.name())) {
      return expireGlobal;
    }
    if (key.endsWith("_" + ScopeEnum.APPLICATION.name())) {
      return expireApplication;
    }
    return expireSession;
  }

  @Recover
  @Override
  public boolean fallbackBoolean(RetryFallbackFalseException e) {
    logger.log(WARN, "All retries completed, so Fallback method called!!! [RetryException], return false", e);
    return false;
  }

  @Recover
  @Override
  public <T> T fallbackNull(RetryFallbackNullException e) {
    logger.log(WARN, "All retries completed, so Fallback method called!!! [RetryFallbackNullException], return null", e);
    return null;
  }
}
