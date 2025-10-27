package com.ecommerce.logistics.infrastructure.adapter.messaging;

import com.ecommerce.logistics.application.dto.AddressDto;
import com.ecommerce.logistics.application.dto.CreateDeliveryRequest;
import com.ecommerce.logistics.application.port.in.DeliveryManagementUseCase;
import com.ecommerce.logistics.domain.model.DeliveryType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 配送事件處理器
 * 遵循 SRP 原則 - 只負責處理外部事件並觸發配送相關操作
 * 處理付款完成事件，自動建立配送請求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventHandler {
    
    private final DeliveryManagementUseCase deliveryManagementUseCase;
    private final ObjectMapper objectMapper;
    
    /**
     * 處理付款完成事件
     * 當付款完成時，自動建立配送請求
     */
    @RabbitListener(queues = "payment.completed.delivery.queue")
    public void handlePaymentCompletedEvent(String eventMessage) {
        log.info("收到付款完成事件，準備建立配送請求");
        
        try {
            // 解析事件訊息
            JsonNode eventNode = objectMapper.readTree(eventMessage);
            
            String orderId = eventNode.get("orderId").asText();
            String customerId = eventNode.get("customerId").asText();
            
            log.info("處理付款完成事件 - 訂單ID: {}, 客戶ID: {}", orderId, customerId);
            
            // 檢查是否已存在配送請求
            try {
                deliveryManagementUseCase.getDeliveryByOrderId(orderId);
                log.warn("訂單已存在配送請求，跳過建立 - 訂單ID: {}", orderId);
                return;
            } catch (Exception e) {
                // 配送請求不存在，繼續建立
                log.debug("訂單尚無配送請求，準備建立 - 訂單ID: {}", orderId);
            }
            
            // 從事件中提取配送地址資訊
            AddressDto deliveryAddress = extractDeliveryAddress(eventNode);
            
            // 建立配送請求
            CreateDeliveryRequest request = new CreateDeliveryRequest(
                orderId,
                customerId,
                deliveryAddress,
                DeliveryType.STANDARD // 預設使用標準配送
            );
            
            deliveryManagementUseCase.createDeliveryRequest(request);
            
            log.info("付款完成事件處理完成，配送請求已建立 - 訂單ID: {}", orderId);
            
        } catch (Exception e) {
            log.error("處理付款完成事件失敗", e);
            // 在實際應用中，這裡可能需要重試機制或死信佇列處理
        }
    }
    
    /**
     * 處理訂單取消事件
     * 當訂單取消時，取消對應的配送請求
     */
    @RabbitListener(queues = "order.cancelled.delivery.queue")
    public void handleOrderCancelledEvent(String eventMessage) {
        log.info("收到訂單取消事件，準備取消配送請求");
        
        try {
            // 解析事件訊息
            JsonNode eventNode = objectMapper.readTree(eventMessage);
            String orderId = eventNode.get("orderId").asText();
            
            log.info("處理訂單取消事件 - 訂單ID: {}", orderId);
            
            // 查找對應的配送請求
            try {
                var delivery = deliveryManagementUseCase.getDeliveryByOrderId(orderId);
                
                // 如果配送請求可以取消，則取消它
                if (delivery.canBeCancelled()) {
                    deliveryManagementUseCase.cancelDelivery(delivery.getDeliveryId());
                    log.info("配送請求已取消 - 配送ID: {}", delivery.getDeliveryId());
                } else {
                    log.warn("配送請求無法取消，當前狀態: {} - 配送ID: {}", 
                        delivery.getStatusDescription(), delivery.getDeliveryId());
                }
                
            } catch (Exception e) {
                log.warn("找不到對應的配送請求 - 訂單ID: {}", orderId);
            }
            
        } catch (Exception e) {
            log.error("處理訂單取消事件失敗", e);
        }
    }
    
    /**
     * 處理客戶地址更新事件
     * 當客戶更新地址時，更新待配送的配送請求地址
     */
    @RabbitListener(queues = "customer.address.updated.delivery.queue")
    public void handleCustomerAddressUpdatedEvent(String eventMessage) {
        log.info("收到客戶地址更新事件");
        
        try {
            // 解析事件訊息
            JsonNode eventNode = objectMapper.readTree(eventMessage);
            String customerId = eventNode.get("customerId").asText();
            
            log.info("處理客戶地址更新事件 - 客戶ID: {}", customerId);
            
            // 查找客戶的待配送請求
            var deliveries = deliveryManagementUseCase.getDeliveriesByCustomerId(customerId);
            
            // 提取新地址
            AddressDto newAddress = extractDeliveryAddress(eventNode);
            
            // 更新所有待配送的配送請求地址
            for (var delivery : deliveries) {
                if (delivery.canBeCancelled()) { // 只有待配送或失敗狀態才能更新地址
                    try {
                        var updateRequest = new com.ecommerce.logistics.application.dto.UpdateDeliveryAddressRequest(
                            delivery.getDeliveryId(), newAddress
                        );
                        deliveryManagementUseCase.updateDeliveryAddress(updateRequest);
                        log.info("配送地址已更新 - 配送ID: {}", delivery.getDeliveryId());
                    } catch (Exception e) {
                        log.warn("更新配送地址失敗 - 配送ID: {}", delivery.getDeliveryId(), e);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("處理客戶地址更新事件失敗", e);
        }
    }
    
    /**
     * 從事件訊息中提取配送地址資訊
     */
    private AddressDto extractDeliveryAddress(JsonNode eventNode) {
        JsonNode addressNode = eventNode.get("deliveryAddress");
        
        if (addressNode == null) {
            // 如果沒有配送地址，使用預設台北地址
            return new AddressDto(
                "台北市",
                "中正區",
                "重慶南路一段122號",
                "100",
                "系統預設",
                "0912345678"
            );
        }
        
        return new AddressDto(
            addressNode.get("city").asText(),
            addressNode.get("district").asText(),
            addressNode.get("street").asText(),
            addressNode.get("postalCode").asText(),
            addressNode.get("recipientName").asText(),
            addressNode.get("recipientPhone").asText()
        );
    }
}