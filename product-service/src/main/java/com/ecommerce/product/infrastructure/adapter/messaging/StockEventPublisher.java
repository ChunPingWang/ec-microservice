package com.ecommerce.product.infrastructure.adapter.messaging;

import com.ecommerce.common.architecture.Adapter;
import com.ecommerce.product.domain.event.ProductOutOfStockEvent;
import com.ecommerce.product.domain.event.ProductRestockedEvent;
import com.ecommerce.product.domain.event.StockUpdatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stock Event Publisher
 * Publishes stock-related domain events to RabbitMQ
 * Follows SRP principle by handling only event publishing
 */
@Adapter
@Component
public class StockEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.messaging.exchange.stock:stock.exchange}")
    private String stockExchange;
    
    @Value("${app.messaging.routing-key.stock-updated:stock.updated}")
    private String stockUpdatedRoutingKey;
    
    @Value("${app.messaging.routing-key.out-of-stock:stock.out-of-stock}")
    private String outOfStockRoutingKey;
    
    @Value("${app.messaging.routing-key.restocked:stock.restocked}")
    private String restockedRoutingKey;
    
    public StockEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish stock updated event
     * @param event the stock updated event
     */
    public void publishStockUpdatedEvent(StockUpdatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(stockExchange, stockUpdatedRoutingKey, eventJson);
            
            // Log successful publication (in real implementation, use proper logging)
            System.out.println("Published StockUpdatedEvent: " + event.getProductId() + 
                             " - Quantity changed from " + event.getPreviousQuantity() + 
                             " to " + event.getNewQuantity());
            
        } catch (JsonProcessingException e) {
            // Log error (in real implementation, use proper logging and error handling)
            System.err.println("Failed to serialize StockUpdatedEvent: " + e.getMessage());
            throw new RuntimeException("Failed to publish stock updated event", e);
        } catch (Exception e) {
            // Log error (in real implementation, use proper logging and error handling)
            System.err.println("Failed to publish StockUpdatedEvent: " + e.getMessage());
            throw new RuntimeException("Failed to publish stock updated event", e);
        }
    }
    
    /**
     * Publish product out of stock event
     * @param event the product out of stock event
     */
    public void publishProductOutOfStockEvent(ProductOutOfStockEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(stockExchange, outOfStockRoutingKey, eventJson);
            
            // Log successful publication
            System.out.println("Published ProductOutOfStockEvent: " + event.getFullProductName() + 
                             " at warehouse " + event.getWarehouseLocation());
            
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize ProductOutOfStockEvent: " + e.getMessage());
            throw new RuntimeException("Failed to publish out of stock event", e);
        } catch (Exception e) {
            System.err.println("Failed to publish ProductOutOfStockEvent: " + e.getMessage());
            throw new RuntimeException("Failed to publish out of stock event", e);
        }
    }
    
    /**
     * Publish product restocked event
     * @param event the product restocked event
     */
    public void publishProductRestockedEvent(ProductRestockedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(stockExchange, restockedRoutingKey, eventJson);
            
            // Log successful publication
            System.out.println("Published ProductRestockedEvent: " + event.getFullProductName() + 
                             " - Added " + event.getQuantityAdded() + " units" +
                             (event.wasOutOfStock() ? " (was out of stock)" : ""));
            
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize ProductRestockedEvent: " + e.getMessage());
            throw new RuntimeException("Failed to publish restocked event", e);
        } catch (Exception e) {
            System.err.println("Failed to publish ProductRestockedEvent: " + e.getMessage());
            throw new RuntimeException("Failed to publish restocked event", e);
        }
    }
    
    /**
     * Publish multiple events in batch
     * @param events array of events to publish
     */
    public void publishEvents(Object... events) {
        for (Object event : events) {
            if (event instanceof StockUpdatedEvent stockEvent) {
                publishStockUpdatedEvent(stockEvent);
            } else if (event instanceof ProductOutOfStockEvent outOfStockEvent) {
                publishProductOutOfStockEvent(outOfStockEvent);
            } else if (event instanceof ProductRestockedEvent restockedEvent) {
                publishProductRestockedEvent(restockedEvent);
            } else {
                System.err.println("Unknown event type: " + event.getClass().getSimpleName());
            }
        }
    }
    
    /**
     * Check if messaging is available
     * @return true if messaging is available, false otherwise
     */
    public boolean isMessagingAvailable() {
        try {
            // Simple check by trying to get connection factory
            rabbitTemplate.getConnectionFactory().createConnection().close();
            return true;
        } catch (Exception e) {
            System.err.println("Messaging not available: " + e.getMessage());
            return false;
        }
    }
}