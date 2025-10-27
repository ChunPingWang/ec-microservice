package com.ecommerce.payment.domain.event;

import com.ecommerce.payment.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款退款事件
 */
public class PaymentRefundedEvent extends PaymentEvent {
    
    private final String originalTransactionId;
    private final PaymentMethod paymentMethod;
    private final String refundReason;
    private final LocalDateTime refundedAt;
    
    public PaymentRefundedEvent(String transactionId, String orderId, String customerId, 
                              BigDecimal refundAmount, String originalTransactionId,
                              PaymentMethod paymentMethod, String refundReason, 
                              LocalDateTime refundedAt) {
        super("PaymentRefunded", transactionId, orderId, customerId, refundAmount);
        this.originalTransactionId = originalTransactionId;
        this.paymentMethod = paymentMethod;
        this.refundReason = refundReason;
        this.refundedAt = refundedAt;
    }
    
    // Getters
    public String getOriginalTransactionId() { return originalTransactionId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getRefundReason() { return refundReason; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    

}