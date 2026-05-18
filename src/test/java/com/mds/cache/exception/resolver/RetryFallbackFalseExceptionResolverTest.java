package com.mds.cache.exception.resolver;

import com.mds.cache.exception.RetryFallbackFalseException;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class RetryFallbackFalseExceptionResolverTest {

    RetryFallbackFalseExceptionResolver resolver = new RetryFallbackFalseExceptionResolver();

    @Test
    public void shouldResolve() {
        assertNotNull(resolver.resolve(new RetryFallbackFalseException()));
    }
}
