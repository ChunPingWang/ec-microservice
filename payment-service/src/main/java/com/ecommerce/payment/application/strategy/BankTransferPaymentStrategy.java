package com.ecommerce.payment.application.strategy;


import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.payment.application.dto.GatewayPaymentRequest;
import com.ecommerce.payment.application.dto.GatewayPaymentResponse;
import com.ecommerce.payment.application.dto.GatewayRefundRequest;
import com.ecommerce.payment.application.dto.GatewayRefundResponse;
import com.ecommerce.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.payment.domain.model.PaymentMethod;

import java.math.BigDecimal;

/**
 * 銀行轉帳付款策略實作
 * 處理銀行轉帳付款的具體邏輯
 */

public class BankTransferPaymentStrategy implements PaymentStrategy {
    
    private final PaymentGatewayPort paymentGatewayPort;
    
    // 銀行轉帳付款限制
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("10");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");
    
    public BankTransferPaymentStrategy(PaymentGatewayPort paymentGatewayPort) {
        this.paymentGatewayPort = paymentGatewayPort;
    }
    
    @Override
    public PaymentMethod getSupportedPaymentMethod() {
        return PaymentMethod.BANK_TRANSFER;
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
            
            // 處理銀行轉帳付款
            return paymentGatewayPort.processBankTransferPayment(request);
            
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
        
        // 驗證銀行轉帳特定欄位
        validateBankTransferFields(request);
        
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
        
        if (request.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new ValidationException("Invalid payment method for bank transfer strategy");
        }
    }
    
    private void validateBankTransferFields(GatewayPaymentRequest request) {
        // 驗證銀行帳號
        if (request.getBankAccount() == null || request.getBankAccount().trim().isEmpty()) {
            throw new ValidationException("Bank account is required");
        }
        
        String bankAccount = request.getBankAccount().trim();
        if (bankAccount.length() < 10 || bankAccount.length() > 20) {
            throw new ValidationException("Bank account must be between 10 and 20 characters");
        }
        
        if (!bankAccount.matches("^[0-9]+$")) {
            throw new ValidationException("Bank account must contain only numbers");
        }
        
        // 驗證銀行代碼
        if (request.getBankCode() == null || request.getBankCode().trim().isEmpty()) {
            throw new ValidationException("Bank code is required");
        }
        
        String bankCode = request.getBankCode().trim();
        if (!bankCode.matches("^[0-9]{3,4}$")) {
            throw new ValidationException("Bank code must be 3 or 4 digits");
        }
        
        // 驗證台灣銀行代碼
        if (!isValidTaiwanBankCode(bankCode)) {
            throw new ValidationException("Invalid Taiwan bank code");
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
     * 驗證台灣銀行代碼
     */
    private boolean isValidTaiwanBankCode(String bankCode) {
        // 常見的台灣銀行代碼
        String[] validBankCodes = {
            "004", // 台灣銀行
            "005", // 土地銀行
            "006", // 合作金庫
            "007", // 第一銀行
            "008", // 華南銀行
            "009", // 彰化銀行
            "011", // 上海銀行
            "012", // 台北富邦
            "013", // 國泰世華
            "017", // 兆豐銀行
            "021", // 花旗銀行
            "050", // 台灣企銀
            "103", // 台新銀行
            "108", // 陽信銀行
            "147", // 三信銀行
            "700", // 中華郵政
            "803", // 聯邦銀行
            "805", // 遠東銀行
            "806", // 元大銀行
            "807", // 永豐銀行
            "808", // 玉山銀行
            "809", // 凱基銀行
            "812", // 台新銀行
            "816", // 安泰銀行
            "822", // 中國信託
        };
        
        for (String validCode : validBankCodes) {
            if (validCode.equals(bankCode)) {
                return true;
            }
        }
        
        return false;
    }
}