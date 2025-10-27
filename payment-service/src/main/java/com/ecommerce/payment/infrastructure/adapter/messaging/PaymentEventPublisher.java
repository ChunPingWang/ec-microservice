package com.ecommerce.payment.infrastructure.adapter.messaging;

import com.ecommerce.common.architecture.EventPublisher;
import com.ecommerce.payment.application.dto.PaymentNotification;
import com.ecommerce.payment.application.port.out.PaymentNotificationPort;
import com.ecommerce.payment.domain.event.PaymentEvent;
import com.ecommerce.payment.domain.event.PaymentFailedEvent;
import com.ecommerce.payment.domain.event.PaymentRefundedEvent;
import com.ecommerce.payment.domain.event.PaymentSuccessEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 付款事件發布器
 * 負責發布付款相關的領域事件和通知
 */
@Component
@EventPublisher
public class PaymentEventPublisher implements PaymentNotificationPort {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private final ObjectMapper objectMapper;
    
    // 在實際實作中，這裡會注入 RabbitMQ 或其他訊息佇列的發送器
    // 目前使用日誌模擬事件發布
    
    public PaymentEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 發布付款成功事件
     */
    public void publishPaymentSuccessEvent(PaymentSuccessEvent event) {
        logger.info("Publishing payment success event for transaction: {}", event.getTransactionId());
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            // 模擬發送到訊息佇列
            publishToMessageQueue("payment.success", eventJson);
            
            logger.info("Payment success event published successfully: {}", event.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize payment success event: {}", event.getTransactionId(), e);
        }
    }
    
    /**
     * 發布付款失敗事件
     */
    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        logger.info("Publishing payment failed event for transaction: {}", event.getTransactionId());
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            // 模擬發送到訊息佇列
            publishToMessageQueue("payment.failed", eventJson);
            
            logger.info("Payment failed event published successfully: {}", event.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize payment failed event: {}", event.getTransactionId(), e);
        }
    }
    
    /**
     * 發布退款事件
     */
    public void publishPaymentRefundedEvent(PaymentRefundedEvent event) {
        logger.info("Publishing payment refunded event for transaction: {}", event.getTransactionId());
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            // 模擬發送到訊息佇列
            publishToMessageQueue("payment.refunded", eventJson);
            
            logger.info("Payment refunded event published successfully: {}", event.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize payment refunded event: {}", event.getTransactionId(), e);
        }
    }
    
    /**
     * 發布通用付款事件
     */
    public void publishPaymentEvent(PaymentEvent event) {
        logger.info("Publishing payment event: {} for transaction: {}", 
                   event.getClass().getSimpleName(), event.getTransactionId());
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String routingKey = determineRoutingKey(event);
            
            // 模擬發送到訊息佇列
            publishToMessageQueue(routingKey, eventJson);
            
            logger.info("Payment event published successfully: {}", event.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize payment event: {}", event.getTransactionId(), e);
        }
    }
    
    @Override
    public void sendPaymentSuccessNotification(PaymentNotification notification) {
        logger.info("Sending payment success notification for transaction: {}", 
                   notification.getTransactionId());
        
        try {
            String notificationJson = objectMapper.writeValueAsString(notification);
            
            // 模擬發送通知
            publishToMessageQueue("notification.payment.success", notificationJson);
            
            logger.info("Payment success notification sent successfully: {}", 
                       notification.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to send payment success notification: {}", 
                        notification.getTransactionId(), e);
        }
    }
    
    @Override
    public void sendPaymentFailureNotification(PaymentNotification notification) {
        logger.info("Sending payment failure notification for transaction: {}", 
                   notification.getTransactionId());
        
        try {
            String notificationJson = objectMapper.writeValueAsString(notification);
            
            // 模擬發送通知
            publishToMessageQueue("notification.payment.failure", notificationJson);
            
            logger.info("Payment failure notification sent successfully: {}", 
                       notification.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to send payment failure notification: {}", 
                        notification.getTransactionId(), e);
        }
    }
    
    @Override
    public void sendRefundSuccessNotification(PaymentNotification notification) {
        logger.info("Sending refund success notification for transaction: {}", 
                   notification.getTransactionId());
        
        try {
            String notificationJson = objectMapper.writeValueAsString(notification);
            
            // 模擬發送通知
            publishToMessageQueue("notification.refund.success", notificationJson);
            
            logger.info("Refund success notification sent successfully: {}", 
                       notification.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to send refund success notification: {}", 
                        notification.getTransactionId(), e);
        }
    }
    
    @Override
    public void sendRefundFailureNotification(PaymentNotification notification) {
        logger.info("Sending refund failure notification for transaction: {}", 
                   notification.getTransactionId());
        
        try {
            String notificationJson = objectMapper.writeValueAsString(notification);
            
            // 模擬發送通知
            publishToMessageQueue("notification.refund.failure", notificationJson);
            
            logger.info("Refund failure notification sent successfully: {}", 
                       notification.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to send refund failure notification: {}", 
                        notification.getTransactionId(), e);
        }
    }
    
    @Override
    public void sendPaymentCancellationNotification(PaymentNotification notification) {
        logger.info("Sending payment cancellation notification for transaction: {}", 
                   notification.getTransactionId());
        
        try {
            String notificationJson = objectMapper.writeValueAsString(notification);
            
            // 模擬發送通知
            publishToMessageQueue("notification.payment.cancellation", notificationJson);
            
            logger.info("Payment cancellation notification sent successfully: {}", 
                       notification.getTransactionId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to send payment cancellation notification: {}", 
                        notification.getTransactionId(), e);
        }
    }
    
    /**
     * 模擬發送訊息到訊息佇列
     * 在實際實作中，這裡會使用 RabbitMQ、Kafka 等訊息佇列
     */
    private void publishToMessageQueue(String routingKey, String message) {
        // 模擬訊息佇列發送
        logger.debug("Publishing message to queue with routing key: {} - Message: {}", 
                    routingKey, message);
        
        // 在實際實作中，這裡會是：
        // rabbitTemplate.convertAndSend(exchange, routingKey, message);
        // 或
        // kafkaTemplate.send(topic, message);
        
        // 模擬發送延遲
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Message publishing interrupted");
        }
    }
    
    /**
     * 根據事件類型決定路由鍵
     */
    private String determineRoutingKey(PaymentEvent event) {
        if (event instanceof PaymentSuccessEvent) {
            return "payment.success";
        } else if (event instanceof PaymentFailedEvent) {
            return "payment.failed";
        } else if (event instanceof PaymentRefundedEvent) {
            return "payment.refunded";
        } else {
            return "payment.general";
        }
    }
}