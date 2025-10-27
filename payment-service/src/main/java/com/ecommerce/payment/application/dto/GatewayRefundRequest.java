package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;

import java.math.BigDecimal;

/**
 * 閘道退款請求 DTO
 */
public class GatewayRefundRequest extends BaseDto {
    
    private String refundTransactionId;
    private String originalGatewayTransactionId;
    private String merchantReference;
    private BigDecimal refundAmount;
    private String currency;
    private String reason;
    private boolean isPartialRefund;
    
    // Original transaction information
    private String originalMerchantReference;
    private BigDecimal originalAmount;
    
    // Constructors
    public GatewayRefundRequest() {
        this.currency = "TWD"; // Default to Taiwan Dollar
    }
    
    public GatewayRefundRequest(String refundTransactionId, String originalGatewayTransactionId,
                              String merchantReference, BigDecimal refundAmount, String reason) {
        this();
        this.refundTransactionId = refundTransactionId;
        this.originalGatewayTransactionId = originalGatewayTransactionId;
        this.merchantReference = merchantReference;
        this.refundAmount = refundAmount;
        this.reason = reason;
    }
    
    // Factory methods
    public static GatewayRefundRequest fullRefund(String refundTransactionId, String originalGatewayTransactionId,
                                                String merchantReference, BigDecimal originalAmount, String reason) {
        GatewayRefundRequest request = new GatewayRefundRequest(refundTransactionId, originalGatewayTransactionId,
                                                              merchantReference, originalAmount, reason);
        request.setOriginalAmount(originalAmount);
        request.setPartialRefund(false);
        return request;
    }
    
    public static GatewayRefundRequest partialRefund(String refundTransactionId, String originalGatewayTransactionId,
                                                   String merchantReference, BigDecimal refundAmount,
                                                   BigDecimal originalAmount, String reason) {
        GatewayRefundRequest request = new GatewayRefundRequest(refundTransactionId, originalGatewayTransactionId,
                                                              merchantReference, refundAmount, reason);
        request.setOriginalAmount(originalAmount);
        request.setPartialRefund(true);
        return request;
    }
    
    // Getters and Setters
    public String getRefundTransactionId() { return refundTransactionId; }
    public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }
    
    public String getOriginalGatewayTransactionId() { return originalGatewayTransactionId; }
    public void setOriginalGatewayTransactionId(String originalGatewayTransactionId) { this.originalGatewayTransactionId = originalGatewayTransactionId; }
    
    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public boolean isPartialRefund() { return isPartialRefund; }
    public void setPartialRefund(boolean partialRefund) { isPartialRefund = partialRefund; }
    
    public String getOriginalMerchantReference() { return originalMerchantReference; }
    public void setOriginalMerchantReference(String originalMerchantReference) { this.originalMerchantReference = originalMerchantReference; }
    
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
}