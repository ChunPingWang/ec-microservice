package com.ecommerce.order.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Order Service 配置類別
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.order.infrastructure.adapter.persistence.repository")
@EntityScan(basePackages = "com.ecommerce.order.infrastructure.adapter.persistence.entity")
@EnableTransactionManagement
@EnableCaching
public class OrderConfig {
    
    // 可以在這裡添加其他配置 Bean
    
}