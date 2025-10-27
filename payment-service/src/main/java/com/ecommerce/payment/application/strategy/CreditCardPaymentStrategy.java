package com.ecommerce.payment.application.strategy;


import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.payment.application.dto.GatewayPaymentRequest;
import com.ecommerce.payment.application.dto.GatewayPaymentResponse;
import com.ecommerce.payment.application.dto.GatewayRefundRequest;
import com.ecommerce.payment.application.dto.GatewayRefundResponse;
import com.ecommerce.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.payment.domain.exception.PaymentProcessingException;
import com.ecommerce.payment.domain.model.PaymentFailureReason;
import com.ecommerce.payment.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * 信用卡付款策略實作
 * 處理信用卡付款的具體邏輯
 */

public class CreditCardPaymentStrategy implements PaymentStrategy {
    
    private final PaymentGatewayPort paymentGatewayPort;
    
    // 信用卡付款限制
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    
    public CreditCardPaymentStrategy(PaymentGatewayPort paymentGatewayPort) {
        this.paymentGatewayPort = paymentGatewayPort;
    }
    
    @Override
    public PaymentMethod getSupportedPaymentMethod() {
        return PaymentMethod.CREDIT_CARD;
    }
    
    @Override
    public GatewayPaymentResponse processPayment(GatewayPaymentRequest request) {
        try {
            // 驗證請求
            validatePaymentRequest(request);
            
            // 檢查閘道可用性
            if (!isAvailable()) {
                return GatewayPaymentResponse.networkError(request.getMerchantReference(), request.getAmount());
            }
            
            // 處理信用卡付款
            return paymentGatewayPort.processCreditCardPayment(request);
            
        } catch (ValidationException e) {
            return GatewayPaymentResponse.failure(
                null, 
                request.getMerchantReference(), 
                request.getAmount(),
                "VALIDATION_ERROR", 
                e.getMessage(),
                "Validation failed: " + e.getMessage(),
                false
            );
        } catch (PaymentProcessingException e) {
            return GatewayPaymentResponse.failure(
                null,
                request.getMerchantReference(),
                request.getAmount(),
                e.getFailureReason().name(),
                e.getMessage(),
                e.getFailureReason().getDescription(),
                e.isRetryable()
            );
        } catch (Exception e) {
            return GatewayPaymentResponse.failure(
                null,
                request.getMerchantReference(),
                request.getAmount(),
                "SYSTEM_ERROR",
                "System error occurred",
                "Unexpected error: " + e.getMessage(),
                true
            );
        }
    }
    
    @Override
    public GatewayRefundResponse processRefund(GatewayRefundRequest request) {
        try {
            // 驗證退款請求
            validateRefundRequest(request);
            
            // 檢查閘道可用性
            if (!isAvailable()) {
                return GatewayRefundResponse.failure(
                    request.getRefundTransactionId(),
                    request.getOriginalGatewayTransactionId(),
                    request.getMerchantReference(),
                    request.getRefundAmount(),
                    "GATEWAY_UNAVAILABLE",
                    "Payment gateway is not available",
                    "Gateway connection failed"
                );
            }
            
            // 處理退款
            return paymentGatewayPort.processRefund(request);
            
        } catch (ValidationException e) {
            return GatewayRefundResponse.failure(
                request.getRefundTransactionId(),
                request.getOriginalGatewayTransactionId(),
                request.getMerchantReference(),
                request.getRefundAmount(),
                "VALIDATION_ERROR",
                e.getMessage(),
                "Validation failed: " + e.getMessage()
            );
        } catch (Exception e) {
            return GatewayRefundResponse.failure(
                request.getRefundTransactionId(),
                request.getOriginalGatewayTransactionId(),
                request.getMerchantReference(),
                request.getRefundAmount(),
                "SYSTEM_ERROR",
                "System error occurred",
                "Unexpected error: " + e.getMessage()
            );
        }
    }
    
    @Override
    public GatewayPaymentResponse queryPaymentStatus(String gatewayTransactionId) {
        try {
            if (gatewayTransactionId == null || gatewayTransactionId.trim().isEmpty()) {
                throw new ValidationException("Gateway transaction ID is required");
            }
            
            if (!isAvailable()) {
                return GatewayPaymentResponse.networkError(null, BigDecimal.ZERO);
            }
            
            return paymentGatewayPort.queryPaymentStatus(gatewayTransactionId);
            
        } catch (Exception e) {
            return GatewayPaymentResponse.failure(
                gatewayTransactionId,
                null,
                BigDecimal.ZERO,
                "QUERY_ERROR",
                "Failed to query payment status",
                e.getMessage(),
                true
            );
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return paymentGatewayPort.isGatewayHealthy();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void validatePaymentRequest(GatewayPaymentRequest request) {
        if (request == null) {
            throw new ValidationException("Payment request is required");
        }
        
        // 驗證基本欄位
        validateBasicFields(request);
        
        // 驗證信用卡特定欄位
        validateCreditCardFields(request);
        
        // 驗證金額
        validateAmount(request.getAmount());
    }
    
    @Override
    public void validateRefundRequest(GatewayRefundRequest request) {
        if (request == null) {
            throw new ValidationException("Refund request is required");
        }
        
        if (request.getRefundTransactionId() == null || request.getRefundTransactionId().trim().isEmpty()) {
            throw new ValidationException("Refund transaction ID is required");
        }
        
        if (request.getOriginalGatewayTransactionId() == null || request.getOriginalGatewayTransactionId().trim().isEmpty()) {
            throw new ValidationException("Original gateway transaction ID is required");
        }
        
        if (request.getRefundAmount() == null || request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Refund amount must be greater than zero");
        }
        
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new ValidationException("Refund reason is required");
        }
    }
    
    // Private helper methods
    private void validateBasicFields(GatewayPaymentRequest request) {
        if (request.getTransactionId() == null || request.getTransactionId().trim().isEmpty()) {
            throw new ValidationException("Transaction ID is required");
        }
        
        if (request.getMerchantReference() == null || request.getMerchantReference().trim().isEmpty()) {
            throw new ValidationException("Merchant reference is required");
        }
        
        if (request.getPaymentMethod() != PaymentMethod.CREDIT_CARD) {
            throw new ValidationException("Invalid payment method for credit card strategy");
        }
    }
    
    private void validateCreditCardFields(GatewayPaymentRequest request) {
        // 驗證卡號
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            throw PaymentProcessingException.invalidCard("Card number is required");
        }
        
        String cardNumber = request.getCardNumber().replaceAll("[\\s-]", "");
        if (!cardNumber.matches("^[0-9]{13,19}$")) {
            throw PaymentProcessingException.invalidCard("Invalid card number format");
        }
        
        if (!isValidLuhn(cardNumber)) {
            throw PaymentProcessingException.invalidCard("Invalid card number");
        }
        
        // 驗證持卡人姓名
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().isEmpty()) {
            throw PaymentProcessingException.invalidCard("Card holder name is required");
        }
        
        if (request.getCardHolderName().trim().length() < 2 || request.getCardHolderName().trim().length() > 50) {
            throw PaymentProcessingException.invalidCard("Card holder name must be between 2 and 50 characters");
        }
        
        // 驗證到期日
        if (request.getExpiryDate() == null) {
            throw PaymentProcessingException.expiredCard("Expiry date is required");
        }
        
        if (request.getExpiryDate().isBefore(YearMonth.now())) {
            throw PaymentProcessingException.expiredCard("Card has expired");
        }
        
        // 驗證 CVV
        if (request.getCvv() == null || request.getCvv().trim().isEmpty()) {
            throw PaymentProcessingException.invalidCvv("CVV is required");
        }
        
        if (!request.getCvv().trim().matches("^[0-9]{3,4}$")) {
            throw PaymentProcessingException.invalidCvv("Invalid CVV format");
        }
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException("Amount is required");
        }
        
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new ValidationException("Amount must be at least " + MIN_AMOUNT);
        }
        
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Amount exceeds maximum limit " + MAX_AMOUNT);
        }
        
        if (amount.scale() > 2) {
            throw new ValidationException("Amount cannot have more than 2 decimal places");
        }
    }
    
    /**
     * Luhn 演算法驗證卡號
     */
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }
}