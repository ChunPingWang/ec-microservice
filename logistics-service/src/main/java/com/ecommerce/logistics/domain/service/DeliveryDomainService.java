package com.ecommerce.logistics.domain.service;

import com.ecommerce.logistics.domain.model.Address;
import com.ecommerce.logistics.domain.model.DeliveryRequest;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.logistics.domain.model.DeliveryType;
import com.ecommerce.logistics.domain.exception.InvalidDeliveryStateException;
import com.ecommerce.common.architecture.DomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 配送領域服務
 * 遵循 SRP 原則 - 只負責複雜的配送業務邏輯
 * 遵循 DIP 原則 - 不依賴基礎設施層
 */
@Slf4j
@Service
@DomainService
public class DeliveryDomainService {
    
    /**
     * 建立配送請求
     * 實作配送請求建立的業務規則
     */
    public DeliveryRequest createDeliveryRequest(String orderId, String customerId, 
                                               Address deliveryAddress, DeliveryType deliveryType) {
        log.info("建立配送請求 - 訂單ID: {}, 客戶ID: {}", orderId, customerId);
        
        // 生成配送ID
        String deliveryId = generateDeliveryId();
        
        // 建立配送請求
        DeliveryRequest deliveryRequest = new DeliveryRequest(
            deliveryId, orderId, customerId, deliveryAddress, deliveryType
        );
        
        log.info("配送請求已建立 - 配送ID: {}, 預計配送日期: {}", 
            deliveryId, deliveryRequest.getEstimatedDeliveryDate());
        
        return deliveryRequest;
    }
    
    /**
     * 開始配送處理
     * 實作配送開始的業務規則
     */
    public void startDelivery(DeliveryRequest deliveryRequest, String trackingNumber) {
        log.info("開始配送處理 - 配送ID: {}", deliveryRequest.getDeliveryId());
        
        if (deliveryRequest.getStatus() != DeliveryStatus.PENDING) {
            throw new InvalidDeliveryStateException("只有待配送狀態才能開始配送");
        }
        
        deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);
        deliveryRequest.setTrackingNumber(trackingNumber);
        
        log.info("配送已開始 - 配送ID: {}, 追蹤號碼: {}", 
            deliveryRequest.getDeliveryId(), trackingNumber);
    }
    
    /**
     * 更新配送狀態
     * 實作狀態更新的業務規則
     */
    public void updateDeliveryStatus(DeliveryRequest deliveryRequest, DeliveryStatus newStatus, 
                                   String notes) {
        log.info("更新配送狀態 - 配送ID: {}, 從 {} 到 {}", 
            deliveryRequest.getDeliveryId(), 
            deliveryRequest.getStatus().getDescription(), 
            newStatus.getDescription());
        
        DeliveryStatus oldStatus = deliveryRequest.getStatus();
        deliveryRequest.updateStatus(newStatus);
        
        if (notes != null && !notes.trim().isEmpty()) {
            deliveryRequest.setDeliveryNotes(notes);
        }
        
        // 記錄狀態變更
        logStatusChange(deliveryRequest, oldStatus, newStatus);
    }
    
    /**
     * 處理配送失敗
     * 實作配送失敗的業務規則
     */
    public void handleDeliveryFailure(DeliveryRequest deliveryRequest, String failureReason) {
        log.warn("配送失敗 - 配送ID: {}, 失敗原因: {}", 
            deliveryRequest.getDeliveryId(), failureReason);
        
        deliveryRequest.markAsFailed(failureReason);
        
        // 如果是地址問題，可以考慮重新安排配送
        if (isAddressRelatedFailure(failureReason)) {
            log.info("地址相關失敗，建議客戶更新配送地址 - 配送ID: {}", 
                deliveryRequest.getDeliveryId());
        }
    }
    
    /**
     * 完成配送
     * 實作配送完成的業務規則
     */
    public void completeDelivery(DeliveryRequest deliveryRequest) {
        log.info("完成配送 - 配送ID: {}", deliveryRequest.getDeliveryId());
        
        if (deliveryRequest.getStatus() != DeliveryStatus.OUT_FOR_DELIVERY) {
            throw new InvalidDeliveryStateException("只有派送中狀態才能完成配送");
        }
        
        deliveryRequest.updateStatus(DeliveryStatus.DELIVERED);
        
        log.info("配送已完成 - 配送ID: {}, 實際配送時間: {}", 
            deliveryRequest.getDeliveryId(), deliveryRequest.getActualDeliveryDate());
    }
    
    /**
     * 驗證地址更新
     * 實作地址更新的業務規則
     */
    public void validateAddressUpdate(DeliveryRequest deliveryRequest, Address newAddress) {
        log.info("驗證地址更新 - 配送ID: {}", deliveryRequest.getDeliveryId());
        
        if (deliveryRequest.getStatus() != DeliveryStatus.PENDING && 
            deliveryRequest.getStatus() != DeliveryStatus.FAILED) {
            throw new InvalidDeliveryStateException("只有待配送或配送失敗狀態才能更新地址");
        }
        
        // 驗證新地址
        newAddress.validate();
        
        // 如果是台北地址，進行額外驗證
        if (newAddress.isTaipeiAddress()) {
            log.info("台北地址驗證通過 - 配送ID: {}", deliveryRequest.getDeliveryId());
        }
    }
    
    /**
     * 生成配送ID
     */
    private String generateDeliveryId() {
        return "DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 記錄狀態變更
     */
    private void logStatusChange(DeliveryRequest deliveryRequest, DeliveryStatus oldStatus, 
                               DeliveryStatus newStatus) {
        log.info("配送狀態變更記錄 - 配送ID: {}, 訂單ID: {}, {} -> {}, 時間: {}", 
            deliveryRequest.getDeliveryId(),
            deliveryRequest.getOrderId(),
            oldStatus.getDescription(),
            newStatus.getDescription(),
            LocalDateTime.now());
    }
    
    /**
     * 檢查是否為地址相關失敗
     */
    private boolean isAddressRelatedFailure(String failureReason) {
        if (failureReason == null) {
            return false;
        }
        
        String reason = failureReason.toLowerCase();
        return reason.contains("地址") || reason.contains("找不到") || 
               reason.contains("無效") || reason.contains("錯誤");
    }
}