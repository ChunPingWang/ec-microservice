package com.ecommerce.logistics.infrastructure.config;

import com.ecommerce.logistics.application.port.out.DeliveryEventPort;
import com.ecommerce.logistics.application.port.out.DeliveryPersistencePort;
import com.ecommerce.logistics.application.port.out.AddressValidationPort;
import com.ecommerce.logistics.domain.repository.DeliveryRepository;
import com.ecommerce.logistics.infrastructure.adapter.persistence.DeliveryJpaAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

/**
 * 物流服務配置
 * 遵循 DIP 原則 - 配置依賴注入和基礎設施組件
 */
@Configuration
@RequiredArgsConstructor
public class LogisticsConfig {
    
    /**
     * 配置DeliveryRepository的實作
     * 將JPA適配器註冊為DeliveryRepository的實作
     */
    @Bean
    public DeliveryRepository deliveryRepository(DeliveryPersistencePort deliveryPersistencePort) {
        // 由於DeliveryJpaAdapter同時實作了DeliveryPersistencePort和DeliveryRepository
        // 這裡直接轉型返回
        return (DeliveryRepository) deliveryPersistencePort;
    }
    
    /**
     * 配置ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    /**
     * 配置RabbitMQ訊息轉換器
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }
    
    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    // ========== 配送事件相關的交換器和佇列配置 ==========
    
    /**
     * 配送事件交換器
     */
    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange("delivery.exchange");
    }
    
    /**
     * 配送建立事件佇列
     */
    @Bean
    public Queue deliveryCreatedQueue() {
        return QueueBuilder.durable("delivery.created.queue").build();
    }
    
    /**
     * 配送狀態更新事件佇列
     */
    @Bean
    public Queue deliveryStatusUpdatedQueue() {
        return QueueBuilder.durable("delivery.status.updated.queue").build();
    }
    
    /**
     * 配送完成事件佇列
     */
    @Bean
    public Queue deliveryCompletedQueue() {
        return QueueBuilder.durable("delivery.completed.queue").build();
    }
    
    /**
     * 配送失敗事件佇列
     */
    @Bean
    public Queue deliveryFailedQueue() {
        return QueueBuilder.durable("delivery.failed.queue").build();
    }
    
    // ========== 綁定配置 ==========
    
    @Bean
    public Binding deliveryCreatedBinding() {
        return BindingBuilder
            .bind(deliveryCreatedQueue())
            .to(deliveryExchange())
            .with("delivery.created");
    }
    
    @Bean
    public Binding deliveryStatusUpdatedBinding() {
        return BindingBuilder
            .bind(deliveryStatusUpdatedQueue())
            .to(deliveryExchange())
            .with("delivery.status.updated");
    }
    
    @Bean
    public Binding deliveryCompletedBinding() {
        return BindingBuilder
            .bind(deliveryCompletedQueue())
            .to(deliveryExchange())
            .with("delivery.completed");
    }
    
    @Bean
    public Binding deliveryFailedBinding() {
        return BindingBuilder
            .bind(deliveryFailedQueue())
            .to(deliveryExchange())
            .with("delivery.failed");
    }
    
    // ========== 監聽外部事件的佇列配置 ==========
    
    /**
     * 監聽付款完成事件的佇列
     */
    @Bean
    public Queue paymentCompletedDeliveryQueue() {
        return QueueBuilder.durable("payment.completed.delivery.queue").build();
    }
    
    /**
     * 監聽訂單取消事件的佇列
     */
    @Bean
    public Queue orderCancelledDeliveryQueue() {
        return QueueBuilder.durable("order.cancelled.delivery.queue").build();
    }
    
    /**
     * 監聽客戶地址更新事件的佇列
     */
    @Bean
    public Queue customerAddressUpdatedDeliveryQueue() {
        return QueueBuilder.durable("customer.address.updated.delivery.queue").build();
    }
    
    // ========== 外部事件交換器綁定 ==========
    
    /**
     * 付款事件交換器
     */
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange("payment.exchange");
    }
    
    /**
     * 訂單事件交換器
     */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange");
    }
    
    /**
     * 客戶事件交換器
     */
    @Bean
    public TopicExchange customerExchange() {
        return new TopicExchange("customer.exchange");
    }
    
    @Bean
    public Binding paymentCompletedDeliveryBinding() {
        return BindingBuilder
            .bind(paymentCompletedDeliveryQueue())
            .to(paymentExchange())
            .with("payment.completed");
    }
    
    @Bean
    public Binding orderCancelledDeliveryBinding() {
        return BindingBuilder
            .bind(orderCancelledDeliveryQueue())
            .to(orderExchange())
            .with("order.cancelled");
    }
    
    @Bean
    public Binding customerAddressUpdatedDeliveryBinding() {
        return BindingBuilder
            .bind(customerAddressUpdatedDeliveryQueue())
            .to(customerExchange())
            .with("customer.address.updated");
    }
}