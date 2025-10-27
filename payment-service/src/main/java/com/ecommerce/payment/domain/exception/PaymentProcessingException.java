package com.ecommerce.payment.domain.exception;

import com.ecommerce.common.exception.DomainException;
import com.ecommerce.payment.domain.model.PaymentFailureReason;

/**
 * 付款處理異常
 */
public class PaymentProcessingException extends DomainException {
    
    private final PaymentFailureReason failureReason;
    private final boolean retryable;
    
    public PaymentProcessingException(String message, PaymentFailureReason failureReason) {
        super(message);
        this.failureReason = failureReason;
        this.retryable = failureReason.isRetryable();
    }
    
    public PaymentProcessingException(String message, PaymentFailureReason failureReason, Throwable cause) {
        super(message, cause);
        this.failureReason = failureReason;
        this.retryable = failureReason.isRetryable();
    }
    
    public static PaymentProcessingException insufficientFunds(String details) {
        return new PaymentProcessingException(
            "Insufficient funds: " + details, 
            PaymentFailureReason.INSUFFICIENT_FUNDS
        );
    }
    
    public static PaymentProcessingException invalidCard(String details) {
        return new PaymentProcessingException(
            "Invalid card: " + details, 
            PaymentFailureReason.INVALID_CARD
        );
    }
    
    public static PaymentProcessingException expiredCard(String details) {
        return new PaymentProcessingException(
            "Expired card: " + details, 
            PaymentFailureReason.EXPIRED_CARD
        );
    }
    
    public static PaymentProcessingException cardDeclined(String details) {
        return new PaymentProcessingException(
            "Card declined: " + details, 
            PaymentFailureReason.CARD_DECLINED
        );
    }
    
    public static PaymentProcessingException invalidCvv(String details) {
        return new PaymentProcessingException(
            "Invalid CVV: " + details, 
            PaymentFailureReason.INVALID_CVV
        );
    }
    
    public static PaymentProcessingException gatewayError(String details) {
        return new PaymentProcessingException(
            "Gateway error: " + details, 
            PaymentFailureReason.GATEWAY_ERROR
        );
    }
    
    public static PaymentProcessingException networkError(String details) {
        return new PaymentProcessingException(
            "Network error: " + details, 
            PaymentFailureReason.NETWORK_ERROR
        );
    }
    
    public static PaymentProcessingException timeout(String details) {
        return new PaymentProcessingException(
            "Payment timeout: " + details, 
            PaymentFailureReason.TIMEOUT
        );
    }
    
    public static PaymentProcessingException fraudDetected(String details) {
        return new PaymentProcessingException(
            "Fraud detected: " + details, 
            PaymentFailureReason.FRAUD_DETECTED
        );
    }
    
    public static PaymentProcessingException cardBlocked(String details) {
        return new PaymentProcessingException(
            "Card blocked: " + details, 
            PaymentFailureReason.CARD_BLOCKED
        );
    }
    
    public static PaymentProcessingException limitExceeded(String details) {
        return new PaymentProcessingException(
            "Limit exceeded: " + details, 
            PaymentFailureReason.LIMIT_EXCEEDED
        );
    }
    
    public static PaymentProcessingException systemError(String details) {
        return new PaymentProcessingException(
            "System error: " + details, 
            PaymentFailureReason.SYSTEM_ERROR
        );
    }
    
    public static PaymentProcessingException unknownError(String details) {
        return new PaymentProcessingException(
            "Unknown error: " + details, 
            PaymentFailureReason.UNKNOWN_ERROR
        );
    }
    
    // Getters
    public PaymentFailureReason getFailureReason() {
        return failureReason;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
}