package com.mds.cache.exception;

import org.junit.Test;

public class RetryFallbackFalseExceptionTest {

	
	@Test(expected= RetryFallbackFalseException.class)
	public void testRetryFallbackFalseExceptionThenDefault() { 
	 
		throw new RetryFallbackFalseException();
	}
	
	@Test(expected= RetryFallbackFalseException.class) 
	public void testRetryFallbackFalseExceptionThenMessage() { 
		
		throw new RetryFallbackFalseException("Message Default");
	}
	
	@Test(expected= RetryFallbackFalseException.class) 
	public void testRetryFallbackFalseExceptionThenException() { 
		
		throw new RetryFallbackFalseException(new Exception("Message Default"));
	}
	
	@Test(expected= RetryFallbackFalseException.class) 
	public void testRetryFallbackFalseExceptionTWhenMessageAndException() { 
		
		throw new RetryFallbackFalseException("Message Default", new Exception("Message Default"));
	} 
}
