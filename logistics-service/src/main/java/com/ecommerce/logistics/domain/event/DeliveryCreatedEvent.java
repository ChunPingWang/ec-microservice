package com.ecommerce.logistics.domain.event;

import com.ecommerce.common.architecture.DomainEvent;
import com.ecommerce.logistics.domain.model.DeliveryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送請求建立事件
 * 遵循 SRP 原則 - 只負責配送建立事件的資料
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryCreatedEvent extends DomainEvent {
    
    private final String deliveryId;
    private final String orderId;
    private final String customerId;
    private final String deliveryAddress;
    private final DeliveryType deliveryType;
    private final BigDecimal deliveryFee;
    private final LocalDateTime estimatedDeliveryDate;
    
    public DeliveryCreatedEvent(String deliveryId, String orderId, String customerId,
                               String deliveryAddress, DeliveryType deliveryType,
                               BigDecimal deliveryFee, LocalDateTime estimatedDeliveryDate) {
        super("DeliveryCreated");
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.deliveryType = deliveryType;
        this.deliveryFee = deliveryFee;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
}