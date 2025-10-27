package com.ecommerce.logistics.infrastructure.adapter.persistence.entity;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.logistics.domain.model.DeliveryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送請求JPA實體
 * 遵循 SRP 原則 - 只負責配送資料的持久化映射
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "delivery_requests")
public class DeliveryJpaEntity extends BaseEntity {
    
    @Id
    @Column(name = "delivery_id", nullable = false, length = 50)
    private String deliveryId;
    
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;
    
    // 地址資訊
    @Column(name = "city", nullable = false, length = 50)
    private String city;
    
    @Column(name = "district", nullable = false, length = 50)
    private String district;
    
    @Column(name = "street", nullable = false, length = 200)
    private String street;
    
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;
    
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;
    
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;
    
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
    
    public DeliveryJpaEntity(String deliveryId, String orderId, String customerId,
                           String city, String district, String street, String postalCode,
                           String recipientName, String recipientPhone,
                           DeliveryType deliveryType, DeliveryStatus status,
                           BigDecimal deliveryFee, LocalDateTime estimatedDeliveryDate) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.city = city;
        this.district = district;
        this.street = street;
        this.postalCode = postalCode;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.deliveryType = deliveryType;
        this.status = status;
        this.deliveryFee = deliveryFee;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
}