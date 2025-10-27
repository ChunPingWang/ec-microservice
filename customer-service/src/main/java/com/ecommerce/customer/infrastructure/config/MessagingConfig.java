package com.ecommerce.customer.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ messaging configuration for Customer service
 * Configures exchanges, queues, and message conversion
 */
@Configuration
public class MessagingConfig {
    
    @Value("${customer.events.exchange:customer.events}")
    private String exchangeName;
    
    @Value("${customer.events.queue.registered:customer.registered.queue}")
    private String customerRegisteredQueue;
    
    @Value("${customer.events.queue.address-updated:customer.address.updated.queue}")
    private String customerAddressUpdatedQueue;
    
    @Value("${customer.events.routing-key.registered:customer.registered}")
    private String customerRegisteredRoutingKey;
    
    @Value("${customer.events.routing-key.address-updated:customer.address.updated}")
    private String customerAddressUpdatedRoutingKey;
    
    /**
     * Configure the main exchange for customer events
     */
    @Bean
    public TopicExchange customerEventsExchange() {
        return new TopicExchange(exchangeName, true, false);
    }
    
    /**
     * Configure queue for customer registered events
     */
    @Bean
    public Queue customerRegisteredQueue() {
        return QueueBuilder.durable(customerRegisteredQueue)
            .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
            .withArgument("x-dead-letter-routing-key", "dead-letter")
            .build();
    }
    
    /**
     * Configure queue for customer address updated events
     */
    @Bean
    public Queue customerAddressUpdatedQueue() {
        return QueueBuilder.durable(customerAddressUpdatedQueue)
            .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
            .withArgument("x-dead-letter-routing-key", "dead-letter")
            .build();
    }
    
    /**
     * Bind customer registered queue to exchange
     */
    @Bean
    public Binding customerRegisteredBinding() {
        return BindingBuilder
            .bind(customerRegisteredQueue())
            .to(customerEventsExchange())
            .with(customerRegisteredRoutingKey);
    }
    
    /**
     * Bind customer address updated queue to exchange
     */
    @Bean
    public Binding customerAddressUpdatedBinding() {
        return BindingBuilder
            .bind(customerAddressUpdatedQueue())
            .to(customerEventsExchange())
            .with(customerAddressUpdatedRoutingKey);
    }
    
    /**
     * Configure dead letter exchange
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(exchangeName + ".dlx", true, false);
    }
    
    /**
     * Configure dead letter queue
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(exchangeName + ".dead-letter.queue").build();
    }
    
    /**
     * Bind dead letter queue to dead letter exchange
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
            .bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("dead-letter");
    }
    
    /**
     * Configure JSON message converter
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * Configure RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // Log failed message publishing
                System.err.println("Failed to publish message: " + cause);
            }
        });
        return template;
    }
}