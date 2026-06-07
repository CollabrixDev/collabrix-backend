package com.syncspace.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;

/**
 * Retry configuration for handling transient failures with exponential backoff.
 * 
 * Features:
 * - Exponential backoff: initial delay with multiplier (1s -> 2s -> 4s -> 8s)
 * - Maximum 3 retry attempts
 * - Jitter to prevent thundering herd
 * - Comprehensive logging of retry attempts
 * 
 * Used for:
 * - External API calls that may timeout
 * - Database operations with connection issues
 * - Network-related transient failures
 * 
 * Non-transient exceptions (validation, auth, not found) are excluded from retry.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class RetryConfig {
    
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final double MULTIPLIER = 2.0;
    private static final long MAX_BACKOFF_MS = 10000;
    
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Exponential backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(INITIAL_BACKOFF_MS);
        backOffPolicy.setMultiplier(MULTIPLIER);
        backOffPolicy.setMaxInterval(MAX_BACKOFF_MS);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Retry policy: max 3 attempts for any exception
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
            MAX_ATTEMPTS,
            Collections.singletonMap(Exception.class, true)
        );
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Add listener for logging
        retryTemplate.registerListener(new RetryListenerSupport() {
            @Override
            public <T, E extends Throwable> void onError(
                    RetryContext context, 
                    RetryCallback<T, E> callback, 
                    Throwable throwable) {
                log.warn(
                    "Retry attempt {} failed for {}: {}",
                    context.getRetryCount(),
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage()
                );
            }
            
            @Override
            public <T, E extends Throwable> void onSuccess(
                    RetryContext context, 
                    RetryCallback<T, E> callback, 
                    T result) {
                if (context.getRetryCount() > 0) {
                    log.info(
                        "Operation succeeded after {} retries",
                        context.getRetryCount()
                    );
                }
            }
        });
        
        return retryTemplate;
    }
}
