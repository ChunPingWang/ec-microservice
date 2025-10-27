package com.ecommerce.logistics.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.logistics.domain.exception.InvalidDeliveryStateException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送請求實體
 * 遵循 SRP 原則 - 只負責配送請求的業務邏輯
 * 實作配送業務規則
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "delivery_requests")
public class DeliveryRequest extends BaseEntity {
    
    @Id
    @Column(name = "delivery_id", nullable = false, length = 50)
    private String deliveryId;
    
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;
    
    @Embedded
    private Address deliveryAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;
    
    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;
    
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    
    @Column(name = "delivery_notes", length = 500)
    private String deliveryNotes;
    
    @Column(name = "failure_reason", length = 200)
    private String failureReason;
    
    public DeliveryRequest(String deliveryId, String orderId, String customerId, 
                          Address deliveryAddress, DeliveryType deliveryType) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.deliveryType = deliveryType;
        this.status = DeliveryStatus.PENDING;
        this.deliveryFee = deliveryType.getFee();
        this.estimatedDeliveryDate = calculateEstimatedDeliveryDate();
        
        // 驗證地址
        if (deliveryAddress != null) {
            deliveryAddress.validate();
        }
    }
    
    /**
     * 計算預計配送日期
     * 實作配送時間計算業務規則
     */
    private LocalDateTime calculateEstimatedDeliveryDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.plusDays(deliveryType.getDeliveryDays());
    }
    
    /**
     * 更新配送狀態
     * 實作狀態轉換業務規則
     */
    public void updateStatus(DeliveryStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidDeliveryStateException(
                String.format("無法從 %s 狀態轉換到 %s 狀態", 
                    this.status.getDescription(), newStatus.getDescription())
            );
        }
        
        this.status = newStatus;
        
        // 根據狀態更新相關欄位
        if (newStatus == DeliveryStatus.DELIVERED) {
            this.actualDeliveryDate = LocalDateTime.now();
        }
    }
    
    /**
     * 設定追蹤號碼
     */
    public void setTrackingNumber(String trackingNumber) {
        if (this.status == DeliveryStatus.PENDING) {
            throw new InvalidDeliveryStateException("配送請求尚未開始處理，無法設定追蹤號碼");
        }
        this.trackingNumber = trackingNumber;
    }
    
    /**
     * 標記配送失敗
     */
    public void markAsFailed(String failureReason) {
        this.status = DeliveryStatus.FAILED;
        this.failureReason = failureReason;
    }
    
    /**
     * 更新配送地址
     * 只有在待配送狀態才能更新地址
     */
    public void updateDeliveryAddress(Address newAddress) {
        if (this.status != DeliveryStatus.PENDING) {
            throw new InvalidDeliveryStateException("只有待配送狀態才能更新配送地址");
        }
        
        newAddress.validate();
        this.deliveryAddress = newAddress;
    }
    
    /**
     * 檢查是否已完成配送
     */
    public boolean isCompleted() {
        return status == DeliveryStatus.DELIVERED || 
               status == DeliveryStatus.CANCELLED || 
               status == DeliveryStatus.RETURNED;
    }
    
    /**
     * 檢查是否可以取消
     */
    public boolean canBeCancelled() {
        return status == DeliveryStatus.PENDING || status == DeliveryStatus.FAILED;
    }
    
    /**
     * 取消配送
     */
    public void cancel() {
        if (!canBeCancelled()) {
            throw new InvalidDeliveryStateException("當前狀態無法取消配送");
        }
        this.status = DeliveryStatus.CANCELLED;
    }
}