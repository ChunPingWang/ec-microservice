package com.ecommerce.logistics.infrastructure.adapter.messaging;

import com.ecommerce.logistics.application.port.out.DeliveryEventPort;
import com.ecommerce.logistics.domain.event.*;
import com.ecommerce.common.architecture.Adapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 配送事件發布器
 * 遵循 SRP 原則 - 只負責配送事件的發布
 * 遵循 DIP 原則 - 實作應用層定義的事件發布埠介面
 */
@Slf4j
@Component
@Adapter
@RequiredArgsConstructor
public class DeliveryEventPublisher implements DeliveryEventPort {
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    // 交換器名稱
    private static final String DELIVERY_EXCHANGE = "delivery.exchange";
    
    // 路由鍵
    private static final String DELIVERY_CREATED_ROUTING_KEY = "delivery.created";
    private static final String DELIVERY_STATUS_UPDATED_ROUTING_KEY = "delivery.status.updated";
    private static final String DELIVERY_COMPLETED_ROUTING_KEY = "delivery.completed";
    private static final String DELIVERY_FAILED_ROUTING_KEY = "delivery.failed";
    
    @Override
    public void publishDeliveryCreatedEvent(DeliveryCreatedEvent event) {
        log.info("發布配送建立事件 - 配送ID: {}, 訂單ID: {}", 
            event.getDeliveryId(), event.getOrderId());
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                DELIVERY_EXCHANGE,
                DELIVERY_CREATED_ROUTING_KEY,
                eventJson
            );
            
            log.info("配送建立事件已發布 - 配送ID: {}", event.getDeliveryId());
            
        } catch (JsonProcessingException e) {
            log.error("配送建立事件序列化失敗 - 配送ID: {}", event.getDeliveryId(), e);
        } catch (Exception e) {
            log.error("配送建立事件發布失敗 - 配送ID: {}", event.getDeliveryId(), e);
        }
    }
    
    @Override
    public void publishDeliveryStatusUpdatedEvent(DeliveryStatusUpdatedEvent event) {
        log.info("發布配送狀態更新事件 - 配送ID: {}, 狀態: {} -> {}", 
            event.getDeliveryId(), 
            event.getOldStatus().getDescription(), 
            event.getNewStatus().getDescription());
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                DELIVERY_EXCHANGE,
                DELIVERY_STATUS_UPDATED_ROUTING_KEY,
                eventJson
            );
            
            log.info("配送狀態更新事件已發布 - 配送ID: {}", event.getDeliveryId());
            
        } catch (JsonProcessingException e) {
            log.error("配送狀態更新事件序列化失敗 - 配送ID: {}", event.getDeliveryId(), e);
        } catch (Exception e) {
            log.error("配送狀態更新事件發布失敗 - 配送ID: {}", event.getDeliveryId(), e);
        }
    }
    
    @Override
    public void publishDeliveryCompletedEvent(DeliveryCompletedEvent event) {
        log.info("發布配送完成事件 - 配送ID: {}, 訂單ID: {}", 
            event.getDeliveryId(), event.getOrderId());
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                DELIVERY_EXCHANGE,
                DELIVERY_COMPLETED_ROUTING_KEY,
                eventJson
            );
            
            log.info("配送完成事件已發布 - 配送ID: {}", event.getDeliveryId());
            
        } catch (JsonProcessingException e) {
            log.error("配送完成事件序列化失敗 - 配送ID: {}", event.getDeliveryId(), e);
        } catch (Exception e) {
            log.error("配送完成事件發布失敗 - 配送ID: {}", event.getDeliveryId(), e);
        }
    }
    
    @Override
    public void publishDeliveryFailedEvent(DeliveryFailedEvent event) {
        log.info("發布配送失敗事件 - 配送ID: {}, 訂單ID: {}, 失敗原因: {}", 
            event.getDeliveryId(), event.getOrderId(), event.getFailureReason());
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                DELIVERY_EXCHANGE,
                DELIVERY_FAILED_ROUTING_KEY,
                eventJson
            );
            
            log.info("配送失敗事件已發布 - 配送ID: {}", event.getDeliveryId());
            
        } catch (JsonProcessingException e) {
            log.error("配送失敗事件序列化失敗 - 配送ID: {}", event.getDeliveryId(), e);
        } catch (Exception e) {
            log.error("配送失敗事件發布失敗 - 配送ID: {}", event.getDeliveryId(), e);
        }
    }
}