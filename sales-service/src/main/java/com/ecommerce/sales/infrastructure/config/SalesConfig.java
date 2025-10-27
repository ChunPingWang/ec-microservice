package com.ecommerce.sales.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 銷售服務配置
 * 遵循 SRP：只負責銷售服務的配置
 */
@Configuration
@EnableTransactionManagement
public class SalesConfig {
    
    // 交換器和佇列常數
    private static final String SALES_EXCHANGE = "sales.exchange";
    private static final String ORDER_COMPLETED_QUEUE = "sales.order.completed.queue";
    private static final String PAYMENT_COMPLETED_QUEUE = "sales.payment.completed.queue";
    private static final String SALES_RECORD_CREATED_QUEUE = "sales.record.created.queue";
    private static final String HIGH_VALUE_SALE_QUEUE = "sales.high-value.queue";
    private static final String SALES_REPORT_GENERATED_QUEUE = "sales.report.generated.queue";
    
    /**
     * 配置 ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    /**
     * 配置 RabbitMQ 訊息轉換器
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper());
    }
    
    /**
     * 配置 RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    /**
     * 配置 RabbitListener 容器工廠
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
    
    // 交換器配置
    
    @Bean
    public TopicExchange salesExchange() {
        return new TopicExchange(SALES_EXCHANGE, true, false);
    }
    
    // 佇列配置
    
    @Bean
    public Queue orderCompletedQueue() {
        return QueueBuilder.durable(ORDER_COMPLETED_QUEUE).build();
    }
    
    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build();
    }
    
    @Bean
    public Queue salesRecordCreatedQueue() {
        return QueueBuilder.durable(SALES_RECORD_CREATED_QUEUE).build();
    }
    
    @Bean
    public Queue highValueSaleQueue() {
        return QueueBuilder.durable(HIGH_VALUE_SALE_QUEUE).build();
    }
    
    @Bean
    public Queue salesReportGeneratedQueue() {
        return QueueBuilder.durable(SALES_REPORT_GENERATED_QUEUE).build();
    }
    
    // 綁定配置
    
    @Bean
    public Binding orderCompletedBinding() {
        return BindingBuilder
                .bind(orderCompletedQueue())
                .to(salesExchange())
                .with("order.completed");
    }
    
    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
                .bind(paymentCompletedQueue())
                .to(salesExchange())
                .with("payment.completed");
    }
    
    @Bean
    public Binding salesRecordCreatedBinding() {
        return BindingBuilder
                .bind(salesRecordCreatedQueue())
                .to(salesExchange())
                .with("sales.record.created");
    }
    
    @Bean
    public Binding highValueSaleBinding() {
        return BindingBuilder
                .bind(highValueSaleQueue())
                .to(salesExchange())
                .with("sales.high-value");
    }
    
    @Bean
    public Binding salesReportGeneratedBinding() {
        return BindingBuilder
                .bind(salesReportGeneratedQueue())
                .to(salesExchange())
                .with("sales.report.generated");
    }
}