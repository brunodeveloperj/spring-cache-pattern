package com.mds.cache.exception;

import java.io.Serial;

/**
 * Exception thrown by {@link com.mds.cache.service.impl.CacheServiceImpl}
 * when a Redis operation fails and retry is enabled
 * ({@code redis.retry.enable=true}).
 *
 * <p>Triggers Spring Retry's {@code @Retryable} mechanism. After all
 * retry attempts are exhausted, the {@code @Recover} fallback returns
 * {@code false} to the caller.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public class RetryFallbackFalseException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 5299804483354242290L;

  public RetryFallbackFalseException() {
    super();
  }

  public RetryFallbackFalseException(String message) {
    super(message);
  }

  public RetryFallbackFalseException(Throwable cause) {
    super(cause);
  }

  public RetryFallbackFalseException(String message, Throwable cause) {
    super(message, cause);
  }
}
