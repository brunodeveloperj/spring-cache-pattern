package com.mds.cache.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mds.cache.enumerator.ScopeEnum;
import com.mds.cache.repository.impl.CacheRepositoryImpl;
import com.mds.cache.service.CacheService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CacheRepositoryImplTest {

  @Mock
  private CacheService cacheService;

  @InjectMocks
  private CacheRepositoryImpl cacheRepository;

  @Test
  void shouldDelegateAvailabilityCheck() {
    when(cacheService.isAvailable()).thenReturn(true);

    assertTrue(cacheRepository.isAvailable());
    verify(cacheService).isAvailable();
  }

  @Test
  void shouldAddGlobalAndScopedEntries() {
    when(cacheService.add("GLOBAL", "hashKey", true)).thenReturn(true);
    when(cacheService.add("client_APPLICATION", "hashKey", true)).thenReturn(true);

    assertTrue(cacheRepository.addGlobal("hashKey", true));
    assertTrue(cacheRepository.add("client", ScopeEnum.APPLICATION, "hashKey", true));

    verify(cacheService).add("GLOBAL", "hashKey", true);
    verify(cacheService).add("client_APPLICATION", "hashKey", true);
  }

  @Test
  void shouldDeleteAcrossAllScopesOnlyWhenEveryScopeSucceeds() {
    when(cacheService.delete("client_SESSION")).thenReturn(true);
    when(cacheService.delete("client_APPLICATION")).thenReturn(true);
    when(cacheService.delete("GLOBAL", "client")).thenReturn(true);

    assertTrue(cacheRepository.delete("client"));

    when(cacheService.delete("client_SESSION")).thenReturn(false);
    assertFalse(cacheRepository.delete("client"));
  }

  @Test
  void shouldDeleteByScopeAndHashKeys() {
    when(cacheService.delete("client_APPLICATION", "A")).thenReturn(true);
    when(cacheService.delete("client_APPLICATION", "B")).thenReturn(true);
    when(cacheService.delete("client_APPLICATION")).thenReturn(true);
    when(cacheService.delete("GLOBAL", "A")).thenReturn(true);
    when(cacheService.delete("GLOBAL")).thenReturn(true);

    assertTrue(cacheRepository.delete("client", ScopeEnum.APPLICATION, "A"));
    assertTrue(cacheRepository.delete("client", ScopeEnum.APPLICATION, Set.of("A", "B")));
    assertTrue(cacheRepository.delete("client", ScopeEnum.APPLICATION, "A", "B"));
    assertTrue(cacheRepository.delete("client", ScopeEnum.APPLICATION));
    assertTrue(cacheRepository.deleteGlobal("A"));
    assertTrue(cacheRepository.deleteAllGlobal());
  }

  @Test
  void shouldReturnFalseForEmptyHashKeyCollections() {
    assertFalse(cacheRepository.delete("client", ScopeEnum.APPLICATION, (Set<String>) null));
    assertFalse(cacheRepository.delete("client", ScopeEnum.APPLICATION, Set.of()));
    assertFalse(cacheRepository.delete("client", ScopeEnum.APPLICATION, new String[0]));
    assertFalse(cacheRepository.delete("client", ScopeEnum.APPLICATION, (String[]) null));
  }

  @Test
  void shouldFindScopedAndGlobalEntries() {
    Object applicationObject = new Object();
    Object globalObject = new Object();

    when(cacheService.findObject("client_APPLICATION", "hashKey", Object.class))
        .thenReturn(applicationObject);
    when(cacheService.findObject("GLOBAL", "hashKey", Object.class))
        .thenReturn(globalObject);

    assertSame(applicationObject,
        cacheRepository.find("client", ScopeEnum.APPLICATION, "hashKey", Object.class));
    assertSame(globalObject,
        cacheRepository.find("client", ScopeEnum.GLOBAL, "hashKey", Object.class));
  }

  @Test
  void shouldFindListsAndInitializeFunctionality() {
    List<String> hashKeys = List.of("a", "b");
    List<Object> values = List.of(new Object(), new Object());

    when(cacheService.findHashKeys("client")).thenReturn(hashKeys);
    when(cacheService.findObjects(eq("client"), any())).thenReturn(values);
    when(cacheService.delete("feature_APPLICATION")).thenReturn(true);

    assertEquals(hashKeys, cacheRepository.find("client"));
    assertSame(values, cacheRepository.find("client", Object.class));
    assertTrue(cacheRepository.initializeFunctionality("feature"));

    verify(cacheService).delete("feature_APPLICATION");
  }
}
