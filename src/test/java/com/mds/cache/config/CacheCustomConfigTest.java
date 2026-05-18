package com.mds.cache.config;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CacheCustomConfigTest {

    @InjectMocks CacheCustomConfig cacheCustomConfig;
    @Mock LettuceConnectionFactory lettuceConnectionFactory;

    @Test
    void should_return_object_when_redisCacheObjectMapper(){
        assertNotNull(cacheCustomConfig.redisCacheObjectMapper());
    }

    @Test
    void should_return_object_when_defaultCacheConfiguration(){
        ReflectionTestUtils.setField(cacheCustomConfig, "defaultTimeToLive", "1m");
        ObjectMapper redisCacheObjectMapper = cacheCustomConfig.redisCacheObjectMapper();
        assertNotNull(cacheCustomConfig.defaultCacheConfiguration(redisCacheObjectMapper));
    }

    @Test
    void should_return_object_when_customCacheConfiguration(){
        ObjectMapper redisCacheObjectMapper = cacheCustomConfig.redisCacheObjectMapper();
        assertNotNull(cacheCustomConfig.customCacheConfiguration(redisCacheObjectMapper));
    }

    @Test
    void should_return_object_when_cacheManager(){
        ReflectionTestUtils.setField(cacheCustomConfig, "defaultTimeToLive", "1m");
        Map<String, String> cache = new HashMap<>();
        cache.put("retryDelay","5s");
        cache.put("timeout","1h");
        ReflectionTestUtils.setField(cacheCustomConfig, "cache", cache);
        ObjectMapper redisCacheObjectMapper = cacheCustomConfig.redisCacheObjectMapper();
        RedisCacheConfiguration defaultCacheConfiguration = cacheCustomConfig.defaultCacheConfiguration(redisCacheObjectMapper);
        Map<String, RedisCacheConfiguration> customCacheConfiguration = cacheCustomConfig.customCacheConfiguration(redisCacheObjectMapper);
        assertNotNull(cacheCustomConfig.cacheManager(lettuceConnectionFactory, defaultCacheConfiguration, customCacheConfiguration));
    }

    @Test
    void should_throw_invalidCache_when_cacheManager(){
        ReflectionTestUtils.setField(cacheCustomConfig, "defaultTimeToLive", "1m");
        Map<String, String> cache = new HashMap<>();
        cache.put("retryDelay"," ");
        ReflectionTestUtils.setField(cacheCustomConfig, "cache", cache);
        ObjectMapper redisCacheObjectMapper = cacheCustomConfig.redisCacheObjectMapper();
        assertThrows(IllegalArgumentException.class, () -> cacheCustomConfig.customCacheConfiguration(redisCacheObjectMapper));
    }

}
