package com.ecommerce.payment.domain.event;

import com.ecommerce.payment.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款失敗事件
 */
public class PaymentFailedEvent extends PaymentEvent {
    
    private final PaymentMethod paymentMethod;
    private final String failureReason;
    private final boolean retryable;
    private final LocalDateTime failedAt;
    
    public PaymentFailedEvent(String transactionId, String orderId, String customerId, 
                            BigDecimal amount, PaymentMethod paymentMethod, 
                            String failureReason, boolean retryable, LocalDateTime failedAt) {
        super("PaymentFailed", transactionId, orderId, customerId, amount);
        this.paymentMethod = paymentMethod;
        this.failureReason = failureReason;
        this.retryable = retryable;
        this.failedAt = failedAt;
    }
    
    // Getters
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getFailureReason() { return failureReason; }
    public boolean isRetryable() { return retryable; }
    public LocalDateTime getFailedAt() { return failedAt; }
    

}