package com.mds.cache.repository;

import com.mds.cache.enumerator.ScopeEnum;

import java.util.List;
import java.util.Set;

/**
 * High-level repository contract for Redis hash-based caching.
 *
 * <p>Extends plain key/hashKey operations with:
 * <ul>
 *   <li>{@link ScopeEnum}-aware variants that automatically prefix the
 *       key with the scope name (SESSION, APPLICATION, GLOBAL).</li>
 *   <li>{@code @Async} counterparts for non-blocking writes and deletes.</li>
 * </ul>
 *
 * <p>All operations delegate to {@link com.mds.cache.service.CacheService}
 * through the default implementation
 * {@link com.mds.cache.repository.impl.CacheRepositoryImpl}.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public interface CacheRepository{

	/**
	 * Check availability of redis
	 * @return true or false
	 */
	boolean isAvailable();
	
	/**
	 * Add object redis in Scope global shared among all users
	 * @param <T> Type of object to add
	 * @param hashKey Attribute to identify object
	 * @param object Object to add
	 * @return true or false
	 */
	<T> boolean addGlobal(String hashKey, T object);

	/**
	 * Add object async redis in Scope global shared among all users
	 * @param <T> Type of object to add
	 * @param hashKey Attribute to identify object
	 * @param object Object to add
	 */
	<T> void addGlobalAsync(String hashKey, T object);
	
	/**
	 * Add object in redis
	 * @param <T> Type of object to add
	 * @param key Attribute to identify client
	 * @param scope Scope to add
	 * @param hashKey Attribute to identify object
	 * @param object Object to add
	 * @return true or false
	 */
	<T> boolean add(String key, ScopeEnum scope, String hashKey, T object);

	/**
	 * Add object async in redis 
	 * @param <T> Type of object to add
	 * @param key Attribute to identify client
	 * @param scope Scope to add
	 * @param hashKey Attribute to identify object
	 * @param object Object to add
	 */
	<T> void addAsync(String key, ScopeEnum scope, String hashKey, T object);

	/**
	 * Remove object redis in Scope global shared among all users
	 * @param hashKey Attribute to identify object
	 * @return true or false
	 */	
	boolean deleteGlobal(String hashKey);
	
	/**
	 * Remove object async redis in Scope global shared among all users
	 * @param hashKey Attribute to identify object
	 */	
	void deleteGlobalAsync(String hashKey);
	
	
	/**
	 * Remove object in redis
	 * @param key Attribute to identify client
	 * @param scope Scope to delete
	 * @return true or false
	 */
	boolean delete(String key, ScopeEnum scope);

	/**
	 * Remove object async in redis
	 * @param key Attribute to identify client
	 * @param scope Scope to delete
	 */
	void deleteAsync(String key, ScopeEnum scope);
	
	/**
	 * Remove all objects in all scopes
	 * @param key Attribute to identify client
	 * @return true or false
	 */
	boolean delete(String key);

	/**
	 * Remove all objects async in all scopes
	 * @param key Attribute to identify client
	 */
	void deleteAsync(String key);
	
	/**
	 * Remove object in redis
	 * @param key Attribute to identify client
	 * @param scope Scope to delete
	 * @param hashKey Attribute to identify object
	 * @return true or false
	 */
	boolean delete(String key, ScopeEnum scope, String hashKey);

	/**
	 * Remove object async in redis
	 * @param key Attribute to identify client
	 * @param scope Scope to delete
	 * @param hashKey Attribute to identify object
	 */
	void deleteAsync(String key, ScopeEnum scope, String hashKey);

	/**
	 * Remove object in redis
	 * @param key Attribute to identify client
	 * @param scope Scope to delete
	 * @param hashKeys Attribute to identify object
	 * @return true or false
	 */
	boolean delete(String key, ScopeEnum scope, Set<String> hashKeys);

	/**
	 * Remove object async in redis
	 * @param key Attribute to identify client
	 * @param scope Scope to delete
	 * @param hashKeys Attribute to identify object
	 */
	void deleteAsync(String key, ScopeEnum scope, Set<String> hashKeys);

	/**
	 * Remove object in redis
	 * @param key Attribute to identify client
	 * @param scope Scope to delete
	 * @param hashKeys Attribute to identify object
	 * @return true or false
	 */
	boolean delete(String key, ScopeEnum scope, String... hashKeys);

	/**
	 * Remove object async in redis
	 * @param key Attribute to identify client
	 * @param scope Scope to delete
	 * @param hashKeys Attribute to identify object
	 */
	void deleteAsync(String key, ScopeEnum scope, String... hashKeys);

	/**
	 * Remove all objects in all scopes
	 * @param key Attribute to identify client
	 * @return true or false
	 */
	boolean deleteAll(String key);

	/**
	 * Remove all objects async in all scopes
	 * @param key Attribute to identify client
	 */
	void deleteAllAsync(String key);
	
	/**
	 * Remove all objects in scope global shared among all users
	 * @return true or false
	 */
	boolean deleteAllGlobal();
	
	/**
	 * Remove all objects async in scope global shared among all users
	 */
	void deleteAllGlobalAsync();

	List<String> find(final String key);

	<T> List<T> find(final String key, final Class<T> clazz);
	
	/**
	 * Search object in redis
	 * @param <T> Type of object to search
	 * @param key Attribute to identify client
	 * @param scope Scope to find
	 * @param hashKey Attribute to identify object
	 * @param tClass Type of object to convert
	 * @return object
	 */
	<T> T find(String key, ScopeEnum scope, String hashKey, Class<T> tClass);
	
	/**
	 * Search object redis in scope Global 
	 * @param <T> Type of object to search
	 * @param hashKey Attribute to identify object
	 * @param tClass Type of object to convert
	 * @return object
	 */
	<T> T findGlobal(String hashKey, Class<T> tClass);
	
	/**
	 * Initialize functionality deleting all objects in scope application
	 * @param key Attribute to identify client
	 * @return true or false
	 */
	boolean initializeFunctionality(String key);

	/**
	 * Initialize functionality deleting all objects async in scope application
	 * @param key Attribute to identify client
	 */
	void initializeFunctionalityAsync(String key);
}
