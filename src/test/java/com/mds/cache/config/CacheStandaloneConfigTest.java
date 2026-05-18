package com.mds.cache.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class CacheStandaloneConfigTest {

	@InjectMocks private CacheStandaloneConfig cacheStandaloneConfig = new CacheStandaloneConfig();
	@Mock private RedisConnectionFactory redisConnectionFactory;
	@Mock private RedisStandaloneConfiguration redisConfiguration;
	@Mock private LettucePoolingClientConfiguration lettucePoolConfig;
	@Mock private ClientOptions options;
	@Mock	private ClientResources dcr;

	@Test
	public void testRedisTemplateThenReturnObject() {
		CacheStandaloneConfig cacheStandaloneConfig = new CacheStandaloneConfig();

		RedisTemplate<Object, Object> redisTemplate = cacheStandaloneConfig.redisTemplate(redisConnectionFactory);
		assertNotNull("RedisTemplate is not null", redisTemplate);
	}

	@Test
	public void testConnectionFactoryThenReturnObject() {
		CacheStandaloneConfig cacheStandaloneConfig = new CacheStandaloneConfig();

		RedisConnectionFactory connectionFactory = cacheStandaloneConfig.connectionFactory(redisConfiguration, lettucePoolConfig);
		assertNotNull("RedisConnectionFactory is not null", connectionFactory);
	}

	@Test
	public void testClientOptionsThenReturnObject() {
		CacheStandaloneConfig cacheStandaloneConfig = new CacheStandaloneConfig();

		ClientOptions clientOptions = cacheStandaloneConfig.clientOptions();
		assertNotNull("ClientOptions is not null", clientOptions);
	}

	@Test
	public void testClientResourcesThenReturnObject() {
		CacheStandaloneConfig cacheStandaloneConfig = new CacheStandaloneConfig();

		ClientResources clientResources = cacheStandaloneConfig.clientResources();
		assertNotNull("ClientResources is not null", clientResources);
	}

	@Test
	public void testRedisConfigurationWithPasswordThenReturnObject() {
		ReflectionTestUtils.setField(cacheStandaloneConfig, "redisHost", "abcde,fghij,klmno");
		ReflectionTestUtils.setField(cacheStandaloneConfig, "redisPort", 1234);
		ReflectionTestUtils.setField(cacheStandaloneConfig, "secret", "qwer");
		ReflectionTestUtils.setField(cacheStandaloneConfig, "passwordEnabled", true);

		RedisStandaloneConfiguration redisConfiguration = cacheStandaloneConfig.redisConfiguration();
		assertNotNull("RedisConfiguration is not null", redisConfiguration);
	}

	@Test
	public void testRedisConfigurationThenReturnObject() {
		ReflectionTestUtils.setField(cacheStandaloneConfig, "redisHost", "abcde,fghij,klmno");
		ReflectionTestUtils.setField(cacheStandaloneConfig, "redisPort", 1234);
		ReflectionTestUtils.setField(cacheStandaloneConfig, "passwordEnabled", false);

		RedisStandaloneConfiguration redisConfiguration = cacheStandaloneConfig.redisConfiguration();
		assertNotNull("RedisConfiguration is not null", redisConfiguration);
	}

	@Test
	public void testLettucePoolingClientConfigurationThenReturnObject() {
		ReflectionTestUtils.setField(cacheStandaloneConfig, "poolMaxIdle", 100);
		ReflectionTestUtils.setField(cacheStandaloneConfig, "poolMinIdle", 5);
		ReflectionTestUtils.setField(cacheStandaloneConfig, "poolMaxWait", 5000);
		ReflectionTestUtils.setField(cacheStandaloneConfig, "poolMaxActive", 100);
		ReflectionTestUtils.setField(cacheStandaloneConfig, "testOnBorrow", true);
		ReflectionTestUtils.setField(cacheStandaloneConfig, "testWhileIdle", true);
		ReflectionTestUtils.setField(cacheStandaloneConfig, "redisTimeout", 10000);

		LettucePoolingClientConfiguration lettucePoolConfig = cacheStandaloneConfig.lettucePoolConfig(options, dcr);
		assertNotNull("LettucePoolingClientConfiguration is not null", lettucePoolConfig);
	}

}

