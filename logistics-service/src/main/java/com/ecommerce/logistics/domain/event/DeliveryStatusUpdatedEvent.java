package com.ecommerce.logistics.domain.event;

import com.ecommerce.common.architecture.DomainEvent;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配送狀態更新事件
 * 遵循 SRP 原則 - 只負責配送狀態更新事件的資料
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryStatusUpdatedEvent extends DomainEvent {
    
    private final String deliveryId;
    private final String orderId;
    private final String customerId;
    private final DeliveryStatus oldStatus;
    private final DeliveryStatus newStatus;
    private final String trackingNumber;
    
    public DeliveryStatusUpdatedEvent(String deliveryId, String orderId, String customerId,
                                    DeliveryStatus oldStatus, DeliveryStatus newStatus,
                                    String trackingNumber) {
        super("DeliveryStatusUpdated");
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.trackingNumber = trackingNumber;
    }
}