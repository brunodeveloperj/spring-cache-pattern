package com.mds.cache.exception.resolver;

import com.mds.cache.exception.RetryFallbackNullException;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class RetryFallbackNullExceptionResolverTest {

    RetryFallbackNullExceptionResolver resolver = new RetryFallbackNullExceptionResolver();

    @Test
    public void shouldResolve() {
        assertNotNull(resolver.resolve(new RetryFallbackNullException()));
    }
}
