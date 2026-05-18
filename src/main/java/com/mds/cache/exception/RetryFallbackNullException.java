package com.mds.cache.exception;

import java.io.Serial;

/**
 * Exception thrown by {@link com.mds.cache.service.impl.CacheServiceImpl}
 * when a Redis read/find operation fails and retry is enabled
 * ({@code redis.retry.enable=true}).
 *
 * <p>Triggers Spring Retry's {@code @Retryable} mechanism. After all
 * retry attempts are exhausted, the {@code @Recover} fallback returns
 * {@code null} to the caller.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public class RetryFallbackNullException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 5299804483354242290L;

  public RetryFallbackNullException() {
    super();
  }

  public RetryFallbackNullException(String message) {
    super(message);
  }

  public RetryFallbackNullException(Throwable cause) {
    super(cause);
  }

  public RetryFallbackNullException(String message, Throwable cause) {
    super(message, cause);
  }
}
