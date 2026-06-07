package com.syncspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

/**
 * SyncSpaceApplication is the main entry point for the SyncSpace backend application.
 * 
 * Configures:
 * - Spring Boot auto-configuration
 * - Component scanning for dependency injection
 * - Retry mechanisms for resilience
 * 
 * All Spring beans are managed by the dependency container,
 * avoiding direct singleton creation.
 * 
 * @author SyncSpace Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.syncspace")
@EnableRetry
public class SyncSpaceApplication {

    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args Command line arguments passed to application
     */
    public static void main(String[] args) {
        SpringApplication.run(SyncSpaceApplication.class, args);
    }

}
