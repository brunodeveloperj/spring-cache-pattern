package com.mds.cache.exception.resolver;

import com.mds.cache.exception.RetryFallbackNullException;
import com.mds.cache.keys.CacheMessageKeys;
import com.mds.error.handler.model.response.ErrorResponse;
import com.mds.error.handler.enumerator.Action;
import com.mds.error.handler.enumerator.Type;
import com.mds.error.handler.exception.helper.ErrorExceptionHandlerHelper;
import com.mds.error.handler.exception.resolver.ExceptionResolver;
import com.mds.error.handler.exception.keys.ExceptionMessageKeys;
import com.mds.error.handler.exception.keys.ErrorStatusKeys;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RetryFallbackNullException} into a standardized
 * {@link ErrorResponse} with error code {@code ARCCHE_0001},
 * transaction type, and method-failure HTTP status.
 *
 * <p>Registered as a Spring component so the global
 * {@link com.mds.error.handler.exception.handler.ErrorExceptionHandler}
 * can automatically delegate to this resolver.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class RetryFallbackNullExceptionResolver implements ExceptionResolver<RetryFallbackNullException> {

  @Override
  public ErrorResponse resolve(RetryFallbackNullException error) {
    return ErrorExceptionHandlerHelper.createError(error,
                                                   Action.RETRY_ON_STATE,
                                                   Type.TRANSACTION,
                                                   ErrorStatusKeys.METHOD_FAILURE,
                                                   ExceptionMessageKeys.DEFAULT_ERROR_TITLE,
                                                   CacheMessageKeys.ERROR_ARCCHE_0001,
                                                   CacheMessageKeys.ERROR_ARCCHE_0001_MESSAGE);
  }

}
