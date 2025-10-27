package com.ecommerce.logistics.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.logistics.domain.model.DeliveryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送資料傳輸物件
 * 遵循 SRP 原則 - 只負責配送資料的傳輸
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto extends BaseDto {
    
    private String deliveryId;
    private String orderId;
    private String customerId;
    private AddressDto deliveryAddress;
    private DeliveryType deliveryType;
    private DeliveryStatus status;
    private BigDecimal deliveryFee;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private String trackingNumber;
    private String deliveryNotes;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
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
     * 取得狀態描述
     */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "";
    }
    
    /**
     * 取得配送類型描述
     */
    public String getDeliveryTypeDescription() {
        return deliveryType != null ? deliveryType.getDescription() : "";
    }
}