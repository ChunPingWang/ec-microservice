package com.ecommerce.logistics.infrastructure.adapter.persistence.mapper;

import com.ecommerce.logistics.domain.model.Address;
import com.ecommerce.logistics.domain.model.DeliveryRequest;
import com.ecommerce.logistics.infrastructure.adapter.persistence.entity.DeliveryJpaEntity;
import org.springframework.stereotype.Component;

/**
 * 配送JPA映射器
 * 遵循 SRP 原則 - 只負責領域物件與JPA實體之間的轉換
 */
@Component
public class DeliveryJpaMapper {
    
    /**
     * 將領域物件轉換為JPA實體
     */
    public DeliveryJpaEntity toEntity(DeliveryRequest deliveryRequest) {
        if (deliveryRequest == null) {
            return null;
        }
        
        DeliveryJpaEntity entity = new DeliveryJpaEntity();
        entity.setDeliveryId(deliveryRequest.getDeliveryId());
        entity.setOrderId(deliveryRequest.getOrderId());
        entity.setCustomerId(deliveryRequest.getCustomerId());
        
        // 地址資訊
        if (deliveryRequest.getDeliveryAddress() != null) {
            Address address = deliveryRequest.getDeliveryAddress();
            entity.setCity(address.getCity());
            entity.setDistrict(address.getDistrict());
            entity.setStreet(address.getStreet());
            entity.setPostalCode(address.getPostalCode());
            entity.setRecipientName(address.getRecipientName());
            entity.setRecipientPhone(address.getRecipientPhone());
        }
        
        entity.setDeliveryType(deliveryRequest.getDeliveryType());
        entity.setStatus(deliveryRequest.getStatus());
        entity.setDeliveryFee(deliveryRequest.getDeliveryFee());
        entity.setEstimatedDeliveryDate(deliveryRequest.getEstimatedDeliveryDate());
        entity.setActualDeliveryDate(deliveryRequest.getActualDeliveryDate());
        entity.setTrackingNumber(deliveryRequest.getTrackingNumber());
        entity.setDeliveryNotes(deliveryRequest.getDeliveryNotes());
        entity.setFailureReason(deliveryRequest.getFailureReason());
        entity.setCreatedAt(deliveryRequest.getCreatedAt());
        entity.setUpdatedAt(deliveryRequest.getUpdatedAt());
        
        return entity;
    }
    
    /**
     * 將JPA實體轉換為領域物件
     */
    public DeliveryRequest toDomain(DeliveryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // 建立地址物件
        Address address = new Address(
            entity.getCity(),
            entity.getDistrict(),
            entity.getStreet(),
            entity.getPostalCode(),
            entity.getRecipientName(),
            entity.getRecipientPhone()
        );
        
        // 建立配送請求物件
        DeliveryRequest deliveryRequest = new DeliveryRequest(
            entity.getDeliveryId(),
            entity.getOrderId(),
            entity.getCustomerId(),
            address,
            entity.getDeliveryType()
        );
        
        // 設定其他屬性
        deliveryRequest.setStatus(entity.getStatus());
        deliveryRequest.setDeliveryFee(entity.getDeliveryFee());
        deliveryRequest.setEstimatedDeliveryDate(entity.getEstimatedDeliveryDate());
        deliveryRequest.setActualDeliveryDate(entity.getActualDeliveryDate());
        deliveryRequest.setTrackingNumber(entity.getTrackingNumber());
        deliveryRequest.setDeliveryNotes(entity.getDeliveryNotes());
        deliveryRequest.setFailureReason(entity.getFailureReason());
        deliveryRequest.setCreatedAt(entity.getCreatedAt());
        deliveryRequest.setUpdatedAt(entity.getUpdatedAt());
        
        return deliveryRequest;
    }
}