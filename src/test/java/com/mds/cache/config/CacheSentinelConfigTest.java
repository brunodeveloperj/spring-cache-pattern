package com.mds.cache.config;

import static org.junit.Assert.assertNotNull;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class CacheSentinelConfigTest {

    @InjectMocks private CacheSentinelConfig cacheConfig = new CacheSentinelConfig();
    @Mock private RedisConnectionFactory redisConnectionFactory;
    @Mock private RedisSentinelConfiguration redisSentinelConfiguration;
    @Mock private LettucePoolingClientConfiguration lettucePoolConfig;
    @Mock private ClientOptions options;
    @Mock	private ClientResources dcr;

    @Test
    public void testRedisTemplateThenReturnObject() {
        CacheSentinelConfig cacheConfig = new CacheSentinelConfig();

        RedisTemplate<Object, Object> redisTemplate = cacheConfig.redisTemplate(redisConnectionFactory);
        assertNotNull("RedisTemplate is not null", redisTemplate);
    }

    @Test
    public void testConnectionFactoryThenReturnObject() {
        CacheSentinelConfig cacheConfig = new CacheSentinelConfig();

        RedisConnectionFactory connectionFactory = cacheConfig.connectionFactory(redisSentinelConfiguration, lettucePoolConfig);
        assertNotNull("RedisConnectionFactory is not null", connectionFactory);
    }

    @Test
    public void testClientOptionsThenReturnObject() {
        CacheSentinelConfig cacheConfig = new CacheSentinelConfig();

        ClientOptions clientOptions = cacheConfig.clientOptions();
        assertNotNull("ClientOptions is not null", clientOptions);
    }

    @Test
    public void testClientResourcesThenReturnObject() {
        CacheSentinelConfig cacheConfig = new CacheSentinelConfig();

        ClientResources clientResources = cacheConfig.clientResources();
        assertNotNull("ClientResources is not null", clientResources);
    }

    @Test
    public void testRedisSentinelConfigurationWithPasswordThenReturnObject() {

        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(cacheConfig, "redisHost", "abcde,fghij,klmno");
        ReflectionTestUtils.setField(cacheConfig, "redisPort", 1234);
        ReflectionTestUtils.setField(cacheConfig, "redisMaster", "fghi");
        ReflectionTestUtils.setField(cacheConfig, "secret", "qwer");
        ReflectionTestUtils.setField(cacheConfig, "passwordEnabled", true);

        RedisSentinelConfiguration redisSentinelConfiguration = cacheConfig.redisSentinelConfiguration();
        assertNotNull("RedisSentinelConfiguration is not null", redisSentinelConfiguration);
    }

    @Test
    public void testRedisSentinelConfigurationThenReturnObject() {

        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(cacheConfig, "redisHost", "abcde,fghij,klmno");
        ReflectionTestUtils.setField(cacheConfig, "redisPort", 1234);
        ReflectionTestUtils.setField(cacheConfig, "redisMaster", "fghi");
        ReflectionTestUtils.setField(cacheConfig, "passwordEnabled", false);

        RedisSentinelConfiguration redisSentinelConfiguration = cacheConfig.redisSentinelConfiguration();
        assertNotNull("RedisSentinelConfiguration is not null", redisSentinelConfiguration);
    }

    @Test
    public void testLettucePoolingClientConfigurationThenReturnObject() {

        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(cacheConfig, "poolMaxIdle", 100);
        ReflectionTestUtils.setField(cacheConfig, "poolMinIdle", 5);
        ReflectionTestUtils.setField(cacheConfig, "poolMaxWait", 5000);
        ReflectionTestUtils.setField(cacheConfig, "poolMaxActive", 100);
        ReflectionTestUtils.setField(cacheConfig, "testOnBorrow", true);
        ReflectionTestUtils.setField(cacheConfig, "testWhileIdle", true);
        ReflectionTestUtils.setField(cacheConfig, "redisTimeout", 10000);

        LettucePoolingClientConfiguration lettucePoolConfig = cacheConfig.lettucePoolConfig(options, dcr);
        assertNotNull("LettucePoolingClientConfiguration is not null", lettucePoolConfig);
    }

}

