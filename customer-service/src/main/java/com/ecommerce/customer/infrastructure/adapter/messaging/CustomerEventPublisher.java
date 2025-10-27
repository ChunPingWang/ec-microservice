package com.ecommerce.customer.infrastructure.adapter.messaging;

import com.ecommerce.common.architecture.Adapter;
import com.ecommerce.customer.application.port.out.CustomerEventPort;
import com.ecommerce.customer.domain.event.CustomerAddressUpdatedEvent;
import com.ecommerce.customer.domain.event.CustomerRegisteredEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ adapter for publishing customer domain events
 * Implements the CustomerEventPort using RabbitMQ
 */
@Adapter
@Component
public class CustomerEventPublisher implements CustomerEventPort {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerEventPublisher.class);
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${customer.events.exchange:customer.events}")
    private String exchangeName;
    
    @Value("${customer.events.routing-key.registered:customer.registered}")
    private String customerRegisteredRoutingKey;
    
    @Value("${customer.events.routing-key.address-updated:customer.address.updated}")
    private String customerAddressUpdatedRoutingKey;
    
    public CustomerEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void publishCustomerRegistered(CustomerRegisteredEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                exchangeName,
                customerRegisteredRoutingKey,
                eventJson
            );
            
            logger.info("Published CustomerRegisteredEvent for customer: {}", event.getCustomerId());
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize CustomerRegisteredEvent: {}", event, e);
            throw new RuntimeException("Failed to publish customer registered event", e);
        } catch (Exception e) {
            logger.error("Failed to publish CustomerRegisteredEvent: {}", event, e);
            throw new RuntimeException("Failed to publish customer registered event", e);
        }
    }
    
    @Override
    public void publishCustomerAddressUpdated(CustomerAddressUpdatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                exchangeName,
                customerAddressUpdatedRoutingKey,
                eventJson
            );
            
            logger.info("Published CustomerAddressUpdatedEvent for customer: {}, address: {}", 
                       event.getCustomerId(), event.getAddressId());
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize CustomerAddressUpdatedEvent: {}", event, e);
            throw new RuntimeException("Failed to publish customer address updated event", e);
        } catch (Exception e) {
            logger.error("Failed to publish CustomerAddressUpdatedEvent: {}", event, e);
            throw new RuntimeException("Failed to publish customer address updated event", e);
        }
    }
}