package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 閘道退款回應 DTO
 */
public class GatewayRefundResponse extends BaseDto {
    
    private String gatewayRefundId;
    private String refundTransactionId;
    private String originalGatewayTransactionId;
    private String merchantReference;
    private boolean success;
    private String status;
    private BigDecimal refundAmount;
    private String currency;
    private String responseCode;
    private String responseMessage;
    private String failureReason;
    private LocalDateTime processedAt;
    private String receiptNumber;
    
    // Constructors
    public GatewayRefundResponse() {}
    
    public GatewayRefundResponse(String gatewayRefundId, String refundTransactionId,
                               String originalGatewayTransactionId, String merchantReference,
                               boolean success, String status, BigDecimal refundAmount) {
        this.gatewayRefundId = gatewayRefundId;
        this.refundTransactionId = refundTransactionId;
        this.originalGatewayTransactionId = originalGatewayTransactionId;
        this.merchantReference = merchantReference;
        this.success = success;
        this.status = status;
        this.refundAmount = refundAmount;
        this.processedAt = LocalDateTime.now();
    }
    
    // Factory methods
    public static GatewayRefundResponse success(String gatewayRefundId, String refundTransactionId,
                                              String originalGatewayTransactionId, String merchantReference,
                                              BigDecimal refundAmount, String receiptNumber) {
        GatewayRefundResponse response = new GatewayRefundResponse(gatewayRefundId, refundTransactionId,
                                                                 originalGatewayTransactionId, merchantReference,
                                                                 true, "SUCCESS", refundAmount);
        response.setResponseCode("00");
        response.setResponseMessage("Refund approved");
        response.setReceiptNumber(receiptNumber);
        return response;
    }
    
    public static GatewayRefundResponse failure(String refundTransactionId, String originalGatewayTransactionId,
                                              String merchantReference, BigDecimal refundAmount,
                                              String responseCode, String responseMessage, String failureReason) {
        GatewayRefundResponse response = new GatewayRefundResponse(null, refundTransactionId,
                                                                 originalGatewayTransactionId, merchantReference,
                                                                 false, "FAILED", refundAmount);
        response.setResponseCode(responseCode);
        response.setResponseMessage(responseMessage);
        response.setFailureReason(failureReason);
        return response;
    }
    
    public static GatewayRefundResponse pending(String gatewayRefundId, String refundTransactionId,
                                              String originalGatewayTransactionId, String merchantReference,
                                              BigDecimal refundAmount) {
        GatewayRefundResponse response = new GatewayRefundResponse(gatewayRefundId, refundTransactionId,
                                                                 originalGatewayTransactionId, merchantReference,
                                                                 false, "PENDING", refundAmount);
        response.setResponseCode("01");
        response.setResponseMessage("Refund pending");
        return response;
    }
    
    // Query methods
    public boolean isSuccessful() {
        return success;
    }
    
    public boolean isFailed() {
        return !success && !"PENDING".equals(status);
    }
    
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    // Getters and Setters
    public String getGatewayRefundId() { return gatewayRefundId; }
    public void setGatewayRefundId(String gatewayRefundId) { this.gatewayRefundId = gatewayRefundId; }
    
    public String getRefundTransactionId() { return refundTransactionId; }
    public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }
    
    public String getOriginalGatewayTransactionId() { return originalGatewayTransactionId; }
    public void setOriginalGatewayTransactionId(String originalGatewayTransactionId) { this.originalGatewayTransactionId = originalGatewayTransactionId; }
    
    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
}