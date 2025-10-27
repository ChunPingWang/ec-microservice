package com.ecommerce.customer.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration for Customer service
 * Configures JPA repositories and entity scanning
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.customer.infrastructure.adapter.persistence.repository")
@EntityScan(basePackages = "com.ecommerce.customer.infrastructure.adapter.persistence.entity")
@EnableTransactionManagement
public class DatabaseConfig {
    
    // JPA configuration is handled by Spring Boot auto-configuration
    // This class provides explicit configuration for package scanning
}