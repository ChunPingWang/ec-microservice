package com.ecommerce.sales.infrastructure.adapter.messaging;

import com.ecommerce.sales.application.service.SalesEventHandlerService;
import com.ecommerce.sales.application.service.SalesEventHandlerService.OrderCompletedEvent;
import com.ecommerce.sales.application.service.SalesEventHandlerService.PaymentCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 銷售事件處理器
 * 遵循 SRP：只負責監聽和處理外部事件
 */
@Component
public class SalesEventHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesEventHandler.class);
    
    private final SalesEventHandlerService salesEventHandlerService;
    private final ObjectMapper objectMapper;
    
    public SalesEventHandler(SalesEventHandlerService salesEventHandlerService,
                           ObjectMapper objectMapper) {
        this.salesEventHandlerService = salesEventHandlerService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 監聽訂單完成事件
     */
    @RabbitListener(queues = "sales.order.completed.queue")
    public void handleOrderCompletedEvent(String eventJson) {
        try {
            logger.info("收到訂單完成事件: {}", eventJson);
            
            OrderCompletedEvent event = objectMapper.readValue(eventJson, OrderCompletedEvent.class);
            salesEventHandlerService.handleOrderCompletedEvent(event);
            
            logger.info("訂單完成事件處理成功: orderId={}", event.getOrderId());
            
        } catch (Exception e) {
            logger.error("處理訂單完成事件失敗: {}", eventJson, e);
            // 實際應用中應該有重試機制或死信佇列處理
            throw new RuntimeException("事件處理失敗", e);
        }
    }
    
    /**
     * 監聽付款完成事件
     */
    @RabbitListener(queues = "sales.payment.completed.queue")
    public void handlePaymentCompletedEvent(String eventJson) {
        try {
            logger.info("收到付款完成事件: {}", eventJson);
            
            PaymentCompletedEvent event = objectMapper.readValue(eventJson, PaymentCompletedEvent.class);
            salesEventHandlerService.handlePaymentCompletedEvent(event);
            
            logger.info("付款完成事件處理成功: orderId={}", event.getOrderId());
            
        } catch (Exception e) {
            logger.error("處理付款完成事件失敗: {}", eventJson, e);
            // 實際應用中應該有重試機制或死信佇列處理
            throw new RuntimeException("事件處理失敗", e);
        }
    }
}