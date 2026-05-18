package com.mds.cache.service;

import com.mds.cache.exception.RetryFallbackFalseException;
import com.mds.cache.exception.RetryFallbackNullException;
import java.util.List;

/**
 * Core service contract for hash-based cache operations.
 *
 * <p>Backend-agnostic — delegates to a
 * {@link com.mds.cache.provider.CacheProvider} (Redis, Caffeine, or Composite).
 * Defines the full lifecycle of cached entries: availability check,
 * existence, add/delete (by key + hashKey), and find (single value,
 * all hash keys, or specific typed values). Also declares
 * {@code fallback*} methods used as {@code @Recover} targets by
 * Spring Retry when {@link RetryFallbackNullException} or
 * {@link RetryFallbackFalseException} is thrown.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 * @see com.mds.cache.service.impl.CacheServiceImpl
 */
public interface CacheService {

	/**
	 * Check availability of redis
	 * @return true or false
	 */
	boolean isAvailable();

	/**
	 * Check if key exists in redis
	 * @param key Attribute to identify client
	 * @return true or false
	 */
	boolean exists(String key);

	/**
	 * Check if key and hashKey exists in redis
	 * @param key Attribute to identify client
	 * @param hashKey Attribute to identify object
	 * @return true or false
	 */
	boolean exists(String key, String hashKey);

	/**
	 * Add object in redis
	 * @param <T> Type of object to add
	 * @param key Attribute to identify client
	 * @param hashKey Attribute to identify object
	 * @param object Object to add
	 * @return true or false
	 */
	<T> boolean add(String key, String hashKey, T object);

	/**
	 * Add object in redis without availability validation
	 * @param <T> Type of object to add
	 * @param key Attribute to identify client
	 * @param hashKey Attribute to identify object
	 * @param object Object to add
	 * @return true or false
	 */
	<T> boolean addUnverified(String key, String hashKey, T object);

	/**
	 * Remove object in redis
	 * @param key Attribute to identify client
	 * @param hashKey Attribute to identify object
	 * @return true or false
	 */
	boolean delete(String key, String hashKey);

	/**
	 * Remove object in redis
	 * @param key Attribute to identify client
	 * @return true or false
	 */
	boolean delete(String key);

	/**
	 * Search object in redis
	 * @param key Attribute to identify client
	 * @return List of objects found
	 */
	List<String> findHashKeys(String key);

	/**
	 * Search object in redis
	 * @param <T> Type of object to search
	 * @param key Attribute to identify client
	 * @return List of objects found
	 */
	<T> List<T> findObjects(String key, Class<T> clazz);

	/**
	 * Search object in redis without availability validation
	 * @param <T> Type of object to search
	 * @param key Attribute to identify client
	 * @param hashKey Attribute to identify object
	 * @param tClass Type of object to convert
	 * @return Object found or null if not found
	 */
	<T> T findObjectUnverified(final String key, final String hashKey, final Class<T> tClass);

	/**
	 * Search object in redis
	 * @param <T> Type of object to search
	 * @param key Attribute to identify client
	 * @param hashKey Attribute to identify object
	 * @param tClass Type of object to convert
	 * @return true or false
	 */
	<T> T findObject(String key, String hashKey, Class<T> tClass);

	/**
	 * Fallback return default false
	 * @param e Exception to retry
	 * @return false
	 */
	boolean fallbackBoolean(RetryFallbackFalseException e);
	
	/**
	 * Fallback return default null
	 * @param <T> Type of object to search
	 * @param e Exception to retry
	 * @return null
	 */
	<T> T fallbackNull(RetryFallbackNullException e);
	
}
