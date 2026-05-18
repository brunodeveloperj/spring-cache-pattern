package com.mds.cache.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import com.mds.cache.exception.RetryFallbackFalseException;
import com.mds.cache.exception.RetryFallbackNullException;
import com.mds.cache.factory.LoggerFactory;
import com.mds.cache.provider.CacheProvider;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CacheServiceImplTest {

  private static final String KEY = "key_APPLICATION";
  private static final String SESSION_KEY = "key_SESSION";
  private static final String GLOBAL_KEY = "GLOBAL";
  private static final String HASH_KEY = "hashKey";

  @Mock
  private LoggerFactory logger;

  @Mock
  private CacheProvider provider;

  @Mock
  private ObjectMapper objectMapper;

  private CacheServiceImpl cacheService;

  @BeforeEach
  void setUp() {
    cacheService = new CacheServiceImpl(logger, provider, objectMapper);
    ReflectionTestUtils.setField(cacheService, "expireSession", 10);
    ReflectionTestUtils.setField(cacheService, "expireApplication", 60);
    ReflectionTestUtils.setField(cacheService, "expireGlobal", -1);
    ReflectionTestUtils.setField(cacheService, "retryEnable", false);
  }

  @Test
  void shouldReturnAvailabilityFromProvider() {
    when(provider.isAvailable()).thenReturn(true);
    assertTrue(cacheService.isAvailable());

    when(provider.isAvailable()).thenThrow(new RuntimeException("down"));
    assertFalse(cacheService.isAvailable());
  }

  @Test
  void shouldThrowRetryFallbackWhenAvailabilityFailsAndRetryEnabled() {
    ReflectionTestUtils.setField(cacheService, "retryEnable", true);
    when(provider.isAvailable()).thenThrow(new RuntimeException("down"));

    assertThrows(RetryFallbackFalseException.class, () -> cacheService.isAvailable());
  }

  @Test
  void shouldAddSerializedObjectAndApplyApplicationExpiration() throws Exception {
    when(provider.isAvailable()).thenReturn(true);
    Object payload = List.of("value");
    when(objectMapper.writeValueAsString(payload)).thenReturn("[\"value\"]");

    assertTrue(cacheService.add(KEY, HASH_KEY, payload));

    verify(provider).put(KEY, HASH_KEY, "[\"value\"]");
    verify(provider).expire(KEY, 60, TimeUnit.MINUTES);
  }

  @Test
  void shouldAddRawStringWithoutSerializing() {
    when(provider.isAvailable()).thenReturn(true);

    assertTrue(cacheService.add(KEY, HASH_KEY, "{\"abc\":\"cde\"}"));

    verify(provider).put(KEY, HASH_KEY, "{\"abc\":\"cde\"}");
  }

  @Test
  void shouldNotAddWhenUnavailableOrObjectIsNull() {
    when(provider.isAvailable()).thenReturn(false);
    assertFalse(cacheService.add(KEY, HASH_KEY, "value"));
    verify(provider, never()).put(any(), any(), any());

    assertFalse(cacheService.addUnverified(KEY, HASH_KEY, null));
  }

  @Test
  void shouldThrowRetryFallbackWhenAddFailsAndRetryEnabled() throws Exception {
    ReflectionTestUtils.setField(cacheService, "retryEnable", true);
    when(provider.isAvailable()).thenReturn(true);
    Object payload = new Object();
    when(objectMapper.writeValueAsString(payload)).thenThrow(new RuntimeException("serialize"));

    assertThrows(RetryFallbackFalseException.class, () -> cacheService.add(KEY, HASH_KEY, payload));
  }

  @Test
  void shouldDeleteEntryAndApplyExpiration() {
    when(provider.isAvailable()).thenReturn(true);

    assertTrue(cacheService.delete(KEY, HASH_KEY));

    verify(provider).delete(KEY, HASH_KEY);
    verify(provider).expire(KEY, 60, TimeUnit.MINUTES);
  }

  @Test
  void shouldDeleteWholeKeyAndRespectTtlRules() {
    when(provider.isAvailable()).thenReturn(true);

    assertTrue(cacheService.delete(KEY));
    assertTrue(cacheService.delete(SESSION_KEY));
    assertTrue(cacheService.delete(GLOBAL_KEY));

    verify(provider).delete(KEY);
    verify(provider).delete(SESSION_KEY);
    verify(provider).delete(GLOBAL_KEY);
    verify(provider).expire(KEY, 60, TimeUnit.MINUTES);
    verify(provider).expire(SESSION_KEY, 10, TimeUnit.MINUTES);
    verify(provider, never()).expire(eq(GLOBAL_KEY), any(Long.class), any());
  }

  @Test
  void shouldReturnFalseOrThrowWhenDeleteFails() {
    when(provider.isAvailable()).thenReturn(false);
    assertFalse(cacheService.delete(KEY, HASH_KEY));
    assertFalse(cacheService.delete(KEY));

    ReflectionTestUtils.setField(cacheService, "retryEnable", true);
    when(provider.isAvailable()).thenReturn(true);
    when(provider.delete(KEY, HASH_KEY)).thenThrow(new RuntimeException("delete"));
    when(provider.delete(KEY)).thenThrow(new RuntimeException("delete"));

    assertThrows(RetryFallbackFalseException.class, () -> cacheService.delete(KEY, HASH_KEY));
    assertThrows(RetryFallbackFalseException.class, () -> cacheService.delete(KEY));
  }

  @Test
  void shouldFindObjectAndDeserializeWhenAvailable() throws Exception {
    when(provider.isAvailable()).thenReturn(true);
    when(provider.get(KEY, HASH_KEY)).thenReturn("{\"field\":\"value\"}");
    when(objectMapper.readValue("{\"field\":\"value\"}", Object.class)).thenReturn("parsed");

    assertEquals("parsed", cacheService.findObject(KEY, HASH_KEY, Object.class));
    verify(provider).expire(KEY, 60, TimeUnit.MINUTES);
  }

  @Test
  void shouldReturnRawStringForStringClass() {
    when(provider.isAvailable()).thenReturn(true);
    when(provider.get(KEY, HASH_KEY)).thenReturn("plain");

    assertEquals("plain", cacheService.findObject(KEY, HASH_KEY, String.class));
  }

  @Test
  void shouldReturnNullWhenFindFailsOrUnavailable() throws Exception {
    when(provider.isAvailable()).thenReturn(false);
    assertNull(cacheService.findObject(KEY, HASH_KEY, Object.class));

    when(provider.isAvailable()).thenReturn(true);
    when(provider.get(KEY, HASH_KEY)).thenReturn(null);
    assertNull(cacheService.findObject(KEY, HASH_KEY, Object.class));

    when(provider.get(KEY, HASH_KEY)).thenThrow(new RuntimeException("read"));
    assertNull(cacheService.findObjectUnverified(KEY, HASH_KEY, Object.class));

    ReflectionTestUtils.setField(cacheService, "retryEnable", true);
    assertThrows(RetryFallbackNullException.class,
        () -> cacheService.findObjectUnverified(KEY, HASH_KEY, Object.class));
  }

  @Test
  void shouldFindHashKeysAndObjects() throws Exception {
    when(provider.findHashKeys(KEY)).thenReturn(List.of("a", "b"));
    when(provider.get(KEY, "a")).thenReturn("{\"id\":1}");
    when(provider.get(KEY, "b")).thenReturn("{\"id\":2}");
    when(objectMapper.readValue("{\"id\":1}", Object.class)).thenReturn("one");
    when(objectMapper.readValue("{\"id\":2}", Object.class)).thenReturn("two");

    assertEquals(List.of("a", "b"), cacheService.findHashKeys(KEY));
    assertEquals(List.of("one", "two"), cacheService.findObjects(KEY, Object.class));
  }

  @Test
  void shouldReturnEmptyHashKeysOrThrowOnFailure() {
    when(provider.findHashKeys(KEY)).thenThrow(new RuntimeException("find"));
    assertEquals(List.of(), cacheService.findHashKeys(KEY));

    ReflectionTestUtils.setField(cacheService, "retryEnable", true);
    assertThrows(RetryFallbackNullException.class, () -> cacheService.findHashKeys(KEY));
  }

  @Test
  void shouldCheckExistenceSafely() {
    when(provider.exists(KEY)).thenReturn(true);
    when(provider.exists(KEY, HASH_KEY)).thenReturn(true);

    assertTrue(cacheService.exists(KEY));
    assertTrue(cacheService.exists(KEY, HASH_KEY));

    when(provider.exists(KEY)).thenThrow(new RuntimeException("exists"));
    when(provider.exists(KEY, HASH_KEY)).thenThrow(new RuntimeException("exists"));

    assertFalse(cacheService.exists(KEY));
    assertFalse(cacheService.exists(KEY, HASH_KEY));
  }

  @Test
  void shouldExposeFallbackHelpers() {
    assertFalse(cacheService.fallbackBoolean(new RetryFallbackFalseException()));
    assertNull(cacheService.fallbackNull(new RetryFallbackNullException()));
  }

  @Test
  void shouldNotThrowOnLoggingOnlyScenarios() {
    assertDoesNotThrow(() -> cacheService.exists(KEY));
    assertNotNull(cacheService);
  }
}
