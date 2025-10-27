package com.ecommerce.logistics.domain.event;

import com.ecommerce.common.architecture.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配送失敗事件
 * 遵循 SRP 原則 - 只負責配送失敗事件的資料
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryFailedEvent extends DomainEvent {
    
    private final String deliveryId;
    private final String orderId;
    private final String customerId;
    private final String failureReason;
    private final String trackingNumber;
    
    public DeliveryFailedEvent(String deliveryId, String orderId, String customerId,
                             String failureReason, String trackingNumber) {
        super("DeliveryFailed");
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.failureReason = failureReason;
        this.trackingNumber = trackingNumber;
    }
}