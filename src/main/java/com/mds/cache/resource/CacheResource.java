package com.mds.cache.resource;

import com.mds.cache.service.CacheService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing diagnostic endpoints for Redis cache inspection.
 *
 * <p>Mapped under {@code /cache-manager}, provides two read-only endpoints:
 * <ul>
 *   <li>{@code GET /findBy/{key}} — lists all hash keys stored under a Redis key.</li>
 *   <li>{@code GET /findBy/{key}/and/{hashKey}} — retrieves a single cached value
 *       by its key and hash key.</li>
 * </ul>
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/cache-manager")
@RequiredArgsConstructor
@ConditionalOnBean(CacheService.class)
@ConditionalOnClass(RestController.class)
public class CacheResource {

  private final CacheService cacheService;

  @GetMapping(value = "/findBy/{key}", produces = "application/json")
  public ResponseEntity<?> findByKey(@PathVariable String key) {
    var responseValue = cacheService.findHashKeys(key);
    return Optional.ofNullable(responseValue)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(value = "/findBy/{key}/and/{hashKey}", produces = "application/json")
  public ResponseEntity<?> findByKeyAndHashKey(@PathVariable String key, @PathVariable String hashKey) {
    var responseValue = cacheService.findObjectUnverified(key, hashKey, String.class);
    return Optional.ofNullable(responseValue)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
  }


}

