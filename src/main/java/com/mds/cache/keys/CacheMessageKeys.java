package com.mds.cache.keys;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Error code and message constants used by the cache exception resolvers
 * ({@link com.mds.cache.exception.resolver.RetryFallbackFalseExceptionResolver},
 * {@link com.mds.cache.exception.resolver.RetryFallbackNullExceptionResolver}).
 *
 * <p>Follows the naming convention {@code ARCCHE_NNNN} for cache-domain errors.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheMessageKeys {

  public static final String ERROR_ARCCHE_0001 = "ARCCHE_0001";
  public static final String ERROR_ARCCHE_0001_MESSAGE = "Um erro ocorreu, tente novamente.";

  public static final String ERROR_ARCCHE_0002 = "ARCCHE_0002";
  public static final String ERROR_ARCCHE_0002_MESSAGE = ERROR_ARCCHE_0001_MESSAGE;

}
