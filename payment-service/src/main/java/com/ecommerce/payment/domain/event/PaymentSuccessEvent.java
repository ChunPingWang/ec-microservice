package com.ecommerce.payment.domain.event;

import com.ecommerce.payment.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款成功事件
 */
public class PaymentSuccessEvent extends PaymentEvent {
    
    private final PaymentMethod paymentMethod;
    private final String gatewayTransactionId;
    private final LocalDateTime processedAt;
    
    public PaymentSuccessEvent(String transactionId, String orderId, String customerId, 
                             BigDecimal amount, PaymentMethod paymentMethod, 
                             String gatewayTransactionId, LocalDateTime processedAt) {
        super("PaymentSuccess", transactionId, orderId, customerId, amount);
        this.paymentMethod = paymentMethod;
        this.gatewayTransactionId = gatewayTransactionId;
        this.processedAt = processedAt;
    }
    
    // Getters
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    

}