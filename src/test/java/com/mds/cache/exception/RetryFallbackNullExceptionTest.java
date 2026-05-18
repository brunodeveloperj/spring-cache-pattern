package com.mds.cache.exception;

import org.junit.Test;

public class RetryFallbackNullExceptionTest {
	
	@Test(expected= RetryFallbackNullException.class)
	public void testRetryFallbackNullExceptionTWhenDefault() { 
	 
		throw new RetryFallbackNullException();
	}
	
	@Test(expected= RetryFallbackNullException.class) 
	public void testRetryFallbackNullExceptionThenMessage() { 
		
		throw new RetryFallbackNullException("Message Default");
	}
	
	@Test(expected= RetryFallbackNullException.class) 
	public void testRetryFallbackNullExceptionThenException() { 
		
		throw new RetryFallbackNullException(new Exception("Message Default"));
	}
	
	@Test(expected= RetryFallbackNullException.class) 
	public void testRetryFallbackNullExceptionThenMessageAndException() { 
		
		throw new RetryFallbackNullException("Message Default", new Exception("Message Default"));
	}

}
