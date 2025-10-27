package com.ecommerce.payment.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 付款服務基礎設施配置
 * 配置基礎設施層相關的 Bean 和設定
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.payment.infrastructure.adapter.persistence.repository")
@EnableTransactionManagement
public class PaymentInfrastructureConfig {
    
    /**
     * 配置 JSON 序列化器
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}