package com.mds.cache.healthcheck;

import com.mds.cache.exception.RetryFallbackNullException;
import com.mds.cache.service.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisHealthCheckTest {

  @Mock private CacheService cacheService;

  @InjectMocks
  private RedisHealthCheck redisHealthCheck;


  @Test
  void should_equalsUp_when_healthCheckRedis() {
    when(cacheService.isAvailable()).thenReturn(true);
    when(cacheService.exists(anyString(), anyString())).thenReturn(false);
    when(cacheService.addUnverified(anyString(), anyString(), any())).thenReturn(true);
    when(cacheService.findObjectUnverified(anyString(),anyString(), any())).thenReturn(Status.UP.toString());
    assertEquals(Status.UP, redisHealthCheck.health().getStatus());
  }

  @Test
  void should_equalsDown_when_healthCheckRedisNotAvailable() {
    assertEquals(Status.DOWN, redisHealthCheck.health().getStatus());
  }

  @Test
  void should_equalsDown_when_healthCheckRedisWithError() {
    when(cacheService.isAvailable()).thenReturn(true);
    assertEquals(Status.DOWN, redisHealthCheck.health().getStatus());
  }

  @Test
  void should_equalsDown_when_healthCheckRedis() {
    when(cacheService.isAvailable()).thenReturn(true);
    when(cacheService.findObjectUnverified(anyString(), anyString(), any())).thenThrow(RetryFallbackNullException.class);
    assertEquals(Status.DOWN, redisHealthCheck.health().getStatus());
  }

  @Test
  void should_equalsDown_when_healthCheckRedis_existsCache() {
    when(cacheService.isAvailable()).thenReturn(true);
    when(cacheService.exists(anyString(), anyString())).thenReturn(true);
    when(cacheService.findObjectUnverified(anyString(),anyString(), any())).thenReturn(Status.DOWN.toString());
    assertEquals(Status.DOWN, redisHealthCheck.health().getStatus());
  }

}
