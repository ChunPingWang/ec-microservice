package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 閘道付款回應 DTO
 */
public class GatewayPaymentResponse extends BaseDto {
    
    private String gatewayTransactionId;
    private String merchantReference;
    private boolean success;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String responseCode;
    private String responseMessage;
    private String failureReason;
    private LocalDateTime processedAt;
    private String authorizationCode;
    private String receiptNumber;
    private boolean retryable;
    
    // Additional gateway-specific fields
    private String gatewayName;
    private String gatewayVersion;
    private String riskScore;
    private String fraudStatus;
    
    // Constructors
    public GatewayPaymentResponse() {}
    
    public GatewayPaymentResponse(String gatewayTransactionId, String merchantReference,
                                boolean success, String status, BigDecimal amount) {
        this.gatewayTransactionId = gatewayTransactionId;
        this.merchantReference = merchantReference;
        this.success = success;
        this.status = status;
        this.amount = amount;
        this.processedAt = LocalDateTime.now();
    }
    
    // Factory methods
    public static GatewayPaymentResponse success(String gatewayTransactionId, String merchantReference,
                                               BigDecimal amount, String authorizationCode,
                                               String receiptNumber) {
        GatewayPaymentResponse response = new GatewayPaymentResponse(gatewayTransactionId,
                                                                   merchantReference, true, "SUCCESS", amount);
        response.setResponseCode("00");
        response.setResponseMessage("Transaction approved");
        response.setAuthorizationCode(authorizationCode);
        response.setReceiptNumber(receiptNumber);
        response.setRetryable(false);
        return response;
    }
    
    public static GatewayPaymentResponse failure(String gatewayTransactionId, String merchantReference,
                                               BigDecimal amount, String responseCode, String responseMessage,
                                               String failureReason, boolean retryable) {
        GatewayPaymentResponse response = new GatewayPaymentResponse(gatewayTransactionId,
                                                                   merchantReference, false, "FAILED", amount);
        response.setResponseCode(responseCode);
        response.setResponseMessage(responseMessage);
        response.setFailureReason(failureReason);
        response.setRetryable(retryable);
        return response;
    }
    
    public static GatewayPaymentResponse pending(String gatewayTransactionId, String merchantReference,
                                               BigDecimal amount) {
        GatewayPaymentResponse response = new GatewayPaymentResponse(gatewayTransactionId,
                                                                   merchantReference, false, "PENDING", amount);
        response.setResponseCode("01");
        response.setResponseMessage("Transaction pending");
        response.setRetryable(false);
        return response;
    }
    
    public static GatewayPaymentResponse timeout(String merchantReference, BigDecimal amount) {
        GatewayPaymentResponse response = new GatewayPaymentResponse(null, merchantReference,
                                                                   false, "TIMEOUT", amount);
        response.setResponseCode("99");
        response.setResponseMessage("Transaction timeout");
        response.setFailureReason("Gateway timeout");
        response.setRetryable(true);
        return response;
    }
    
    public static GatewayPaymentResponse networkError(String merchantReference, BigDecimal amount) {
        GatewayPaymentResponse response = new GatewayPaymentResponse(null, merchantReference,
                                                                   false, "NETWORK_ERROR", amount);
        response.setResponseCode("98");
        response.setResponseMessage("Network error");
        response.setFailureReason("Network connection failed");
        response.setRetryable(true);
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
    
    public boolean isTimeout() {
        return "TIMEOUT".equals(status);
    }
    
    public boolean isNetworkError() {
        return "NETWORK_ERROR".equals(status);
    }
    
    // Getters and Setters
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
    
    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
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
    
    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }
    
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    
    public boolean isRetryable() { return retryable; }
    public void setRetryable(boolean retryable) { this.retryable = retryable; }
    
    public String getGatewayName() { return gatewayName; }
    public void setGatewayName(String gatewayName) { this.gatewayName = gatewayName; }
    
    public String getGatewayVersion() { return gatewayVersion; }
    public void setGatewayVersion(String gatewayVersion) { this.gatewayVersion = gatewayVersion; }
    
    public String getRiskScore() { return riskScore; }
    public void setRiskScore(String riskScore) { this.riskScore = riskScore; }
    
    public String getFraudStatus() { return fraudStatus; }
    public void setFraudStatus(String fraudStatus) { this.fraudStatus = fraudStatus; }
}