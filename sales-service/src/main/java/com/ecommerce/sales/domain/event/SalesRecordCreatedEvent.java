package com.ecommerce.sales.domain.event;

import com.ecommerce.common.architecture.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 銷售記錄建立事件
 * 當新的銷售記錄建立時發布此事件
 */
public class SalesRecordCreatedEvent extends DomainEvent {
    
    private final String salesRecordId;
    private final String orderId;
    private final String customerId;
    private final String productId;
    private final BigDecimal totalAmount;
    private final String category;
    private final String channel;
    
    public SalesRecordCreatedEvent(String salesRecordId, String orderId, String customerId,
                                 String productId, BigDecimal totalAmount, String category,
                                 String channel) {
        super("SalesRecordCreated");
        this.salesRecordId = salesRecordId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.totalAmount = totalAmount;
        this.category = category;
        this.channel = channel;
    }
    
    public String getSalesRecordId() { return salesRecordId; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCategory() { return category; }
    public String getChannel() { return channel; }
    
    @Override
    public String toString() {
        return "SalesRecordCreatedEvent{" +
                "salesRecordId='" + salesRecordId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", productId='" + productId + '\'' +
                ", totalAmount=" + totalAmount +
                ", timestamp=" + getOccurredOn() +
                '}';
    }
}