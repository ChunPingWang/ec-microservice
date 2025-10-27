package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.payment.domain.model.PaymentMethod;
import com.ecommerce.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款回應 DTO
 */
public class PaymentResponse extends BaseDto {
    
    private String transactionId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String failureReason;
    private String description;
    private LocalDateTime processedAt;
    private boolean retryable;
    private String maskedCardNumber;
    
    // Constructors
    public PaymentResponse() {}
    
    public PaymentResponse(String transactionId, String orderId, String customerId,
                          BigDecimal amount, PaymentMethod paymentMethod, PaymentStatus status) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }
    
    // Factory methods for different response types
    public static PaymentResponse success(String transactionId, String orderId, String customerId,
                                        BigDecimal amount, PaymentMethod paymentMethod,
                                        String gatewayTransactionId, LocalDateTime processedAt) {
        PaymentResponse response = new PaymentResponse(transactionId, orderId, customerId,
                                                     amount, paymentMethod, PaymentStatus.SUCCESS);
        response.setGatewayTransactionId(gatewayTransactionId);
        response.setProcessedAt(processedAt);
        response.setRetryable(false);
        return response;
    }
    
    public static PaymentResponse failure(String transactionId, String orderId, String customerId,
                                        BigDecimal amount, PaymentMethod paymentMethod,
                                        String failureReason, boolean retryable) {
        PaymentResponse response = new PaymentResponse(transactionId, orderId, customerId,
                                                     amount, paymentMethod, PaymentStatus.FAILED);
        response.setFailureReason(failureReason);
        response.setRetryable(retryable);
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }
    
    public static PaymentResponse pending(String transactionId, String orderId, String customerId,
                                        BigDecimal amount, PaymentMethod paymentMethod) {
        return new PaymentResponse(transactionId, orderId, customerId,
                                 amount, paymentMethod, PaymentStatus.PENDING);
    }
    
    public static PaymentResponse processing(String transactionId, String orderId, String customerId,
                                           BigDecimal amount, PaymentMethod paymentMethod) {
        return new PaymentResponse(transactionId, orderId, customerId,
                                 amount, paymentMethod, PaymentStatus.PROCESSING);
    }
    
    public static PaymentResponse cancelled(String transactionId, String orderId, String customerId,
                                          BigDecimal amount, PaymentMethod paymentMethod, String reason) {
        PaymentResponse response = new PaymentResponse(transactionId, orderId, customerId,
                                                     amount, paymentMethod, PaymentStatus.CANCELLED);
        response.setFailureReason(reason);
        response.setRetryable(false);
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }
    
    // Query methods
    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
    
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }
    
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
    
    public boolean isProcessing() {
        return status == PaymentStatus.PROCESSING;
    }
    
    public boolean isCancelled() {
        return status == PaymentStatus.CANCELLED;
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public boolean isRetryable() { return retryable; }
    public void setRetryable(boolean retryable) { this.retryable = retryable; }
    
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }
}