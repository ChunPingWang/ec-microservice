package com.ecommerce.payment.domain.event;

import com.ecommerce.common.architecture.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款事件基礎類別
 */
public abstract class PaymentEvent extends DomainEvent {
    
    private final String transactionId;
    private final String orderId;
    private final String customerId;
    private final BigDecimal amount;
    
    protected PaymentEvent(String eventType, String transactionId, String orderId, String customerId, BigDecimal amount) {
        super(eventType);
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
    }
    
    // Getters
    public String getTransactionId() { return transactionId; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
}