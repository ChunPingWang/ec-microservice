package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 退款請求 DTO
 */
public class RefundRequest extends BaseDto {
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero")
    private BigDecimal refundAmount;
    
    @NotBlank(message = "Refund reason is required")
    private String reason;
    
    private boolean isPartialRefund;
    
    // Constructors
    public RefundRequest() {}
    
    public RefundRequest(String transactionId, String orderId, String customerId,
                        BigDecimal refundAmount, String reason) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.isPartialRefund = false;
    }
    
    // Factory methods
    public static RefundRequest fullRefund(String transactionId, String orderId, String customerId, String reason) {
        RefundRequest request = new RefundRequest();
        request.setTransactionId(transactionId);
        request.setOrderId(orderId);
        request.setCustomerId(customerId);
        request.setReason(reason);
        request.setPartialRefund(false);
        return request;
    }
    
    public static RefundRequest partialRefund(String transactionId, String orderId, String customerId,
                                            BigDecimal refundAmount, String reason) {
        RefundRequest request = new RefundRequest(transactionId, orderId, customerId, refundAmount, reason);
        request.setPartialRefund(true);
        return request;
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public boolean isPartialRefund() { return isPartialRefund; }
    public void setPartialRefund(boolean partialRefund) { isPartialRefund = partialRefund; }
}