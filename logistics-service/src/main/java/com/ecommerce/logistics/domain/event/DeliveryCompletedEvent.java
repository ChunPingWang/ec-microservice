package com.ecommerce.logistics.domain.event;

import com.ecommerce.common.architecture.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配送完成事件
 * 遵循 SRP 原則 - 只負責配送完成事件的資料
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryCompletedEvent extends DomainEvent {
    
    private final String deliveryId;
    private final String orderId;
    private final String customerId;
    private final LocalDateTime actualDeliveryDate;
    private final String trackingNumber;
    
    public DeliveryCompletedEvent(String deliveryId, String orderId, String customerId,
                                LocalDateTime actualDeliveryDate, String trackingNumber) {
        super("DeliveryCompleted");
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.actualDeliveryDate = actualDeliveryDate;
        this.trackingNumber = trackingNumber;
    }
}