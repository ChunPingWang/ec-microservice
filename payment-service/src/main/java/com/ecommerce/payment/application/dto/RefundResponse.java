package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.payment.domain.model.PaymentMethod;
import com.ecommerce.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款回應 DTO
 */
public class RefundResponse extends BaseDto {
    
    private String refundTransactionId;
    private String originalTransactionId;
    private String orderId;
    private String customerId;
    private BigDecimal refundAmount;
    private BigDecimal refundFee;
    private BigDecimal netRefundAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayRefundId;
    private String failureReason;
    private String reason;
    private LocalDateTime processedAt;
    private boolean isPartialRefund;
    
    // Constructors
    public RefundResponse() {}
    
    public RefundResponse(String refundTransactionId, String originalTransactionId, String orderId,
                         String customerId, BigDecimal refundAmount, PaymentMethod paymentMethod) {
        this.refundTransactionId = refundTransactionId;
        this.originalTransactionId = originalTransactionId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.refundAmount = refundAmount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }
    
    // Factory methods
    public static RefundResponse success(String refundTransactionId, String originalTransactionId,
                                       String orderId, String customerId, BigDecimal refundAmount,
                                       BigDecimal refundFee, PaymentMethod paymentMethod,
                                       String gatewayRefundId, String reason, boolean isPartialRefund) {
        RefundResponse response = new RefundResponse(refundTransactionId, originalTransactionId,
                                                   orderId, customerId, refundAmount, paymentMethod);
        response.setStatus(PaymentStatus.SUCCESS);
        response.setRefundFee(refundFee);
        response.setNetRefundAmount(refundAmount.subtract(refundFee));
        response.setGatewayRefundId(gatewayRefundId);
        response.setReason(reason);
        response.setProcessedAt(LocalDateTime.now());
        response.setPartialRefund(isPartialRefund);
        return response;
    }
    
    public static RefundResponse failure(String refundTransactionId, String originalTransactionId,
                                       String orderId, String customerId, BigDecimal refundAmount,
                                       PaymentMethod paymentMethod, String failureReason, String reason) {
        RefundResponse response = new RefundResponse(refundTransactionId, originalTransactionId,
                                                   orderId, customerId, refundAmount, paymentMethod);
        response.setStatus(PaymentStatus.FAILED);
        response.setFailureReason(failureReason);
        response.setReason(reason);
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }
    
    public static RefundResponse pending(String refundTransactionId, String originalTransactionId,
                                       String orderId, String customerId, BigDecimal refundAmount,
                                       PaymentMethod paymentMethod, String reason) {
        RefundResponse response = new RefundResponse(refundTransactionId, originalTransactionId,
                                                   orderId, customerId, refundAmount, paymentMethod);
        response.setReason(reason);
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
    
    // Getters and Setters
    public String getRefundTransactionId() { return refundTransactionId; }
    public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }
    
    public String getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(String originalTransactionId) { this.originalTransactionId = originalTransactionId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public BigDecimal getRefundFee() { return refundFee; }
    public void setRefundFee(BigDecimal refundFee) { this.refundFee = refundFee; }
    
    public BigDecimal getNetRefundAmount() { return netRefundAmount; }
    public void setNetRefundAmount(BigDecimal netRefundAmount) { this.netRefundAmount = netRefundAmount; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getGatewayRefundId() { return gatewayRefundId; }
    public void setGatewayRefundId(String gatewayRefundId) { this.gatewayRefundId = gatewayRefundId; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public boolean isPartialRefund() { return isPartialRefund; }
    public void setPartialRefund(boolean partialRefund) { isPartialRefund = partialRefund; }
}