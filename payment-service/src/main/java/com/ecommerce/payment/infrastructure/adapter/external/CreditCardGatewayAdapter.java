package com.ecommerce.payment.infrastructure.adapter.external;

import com.ecommerce.common.architecture.ExternalAdapter;
import com.ecommerce.payment.application.dto.GatewayPaymentRequest;
import com.ecommerce.payment.application.dto.GatewayPaymentResponse;
import com.ecommerce.payment.application.dto.GatewayRefundRequest;
import com.ecommerce.payment.application.dto.GatewayRefundResponse;
import com.ecommerce.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.payment.domain.model.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * 信用卡閘道適配器
 * 模擬外部信用卡付款閘道的整合
 */
@Component
@ExternalAdapter
public class CreditCardGatewayAdapter implements PaymentGatewayPort {
    
    private static final Logger logger = LoggerFactory.getLogger(CreditCardGatewayAdapter.class);
    private final Random random = new Random();
    
    // 模擬的成功率配置
    private static final double SUCCESS_RATE = 0.85; // 85% 成功率
    private static final double TIMEOUT_RATE = 0.05; // 5% 超時率
    private static final double NETWORK_ERROR_RATE = 0.03; // 3% 網路錯誤率
    
    @Override
    public GatewayPaymentResponse processCreditCardPayment(GatewayPaymentRequest request) {
        logger.info("Processing credit card payment for transaction: {}", request.getTransactionId());
        
        // 模擬處理延遲
        simulateProcessingDelay();
        
        // 模擬不同的回應情況
        double randomValue = random.nextDouble();
        
        if (randomValue < TIMEOUT_RATE) {
            logger.warn("Simulating timeout for transaction: {}", request.getTransactionId());
            return GatewayPaymentResponse.timeout(request.getMerchantReference(), request.getAmount());
        }
        
        if (randomValue < TIMEOUT_RATE + NETWORK_ERROR_RATE) {
            logger.warn("Simulating network error for transaction: {}", request.getTransactionId());
            return GatewayPaymentResponse.networkError(request.getMerchantReference(), request.getAmount());
        }
        
        if (randomValue < SUCCESS_RATE) {
            return processSuccessfulPayment(request);
        } else {
            return processFailedPayment(request);
        }
    }
    
    @Override
    public GatewayPaymentResponse processDebitCardPayment(GatewayPaymentRequest request) {
        logger.info("Processing debit card payment for transaction: {}", request.getTransactionId());
        
        // 金融卡付款邏輯類似信用卡，但可能有不同的驗證規則
        simulateProcessingDelay();
        
        // 金融卡通常有較低的成功率（需要即時餘額驗證）
        double randomValue = random.nextDouble();
        
        if (randomValue < 0.75) { // 75% 成功率
            return processSuccessfulPayment(request);
        } else {
            return GatewayPaymentResponse.failure(
                generateGatewayTransactionId(),
                request.getMerchantReference(),
                request.getAmount(),
                "51",
                "Insufficient funds",
                "餘額不足",
                false
            );
        }
    }
    
    @Override
    public GatewayPaymentResponse processBankTransferPayment(GatewayPaymentRequest request) {
        logger.info("Processing bank transfer payment for transaction: {}", request.getTransactionId());
        
        // 銀行轉帳通常需要較長的處理時間
        simulateProcessingDelay(2000, 5000);
        
        // 銀行轉帳通常返回 pending 狀態
        return GatewayPaymentResponse.pending(
            generateGatewayTransactionId(),
            request.getMerchantReference(),
            request.getAmount()
        );
    }
    
    @Override
    public GatewayRefundResponse processRefund(GatewayRefundRequest request) {
        logger.info("Processing refund for original transaction: {}", request.getOriginalGatewayTransactionId());
        
        simulateProcessingDelay();
        
        // 退款通常有較高的成功率
        double randomValue = random.nextDouble();
        
        if (randomValue < 0.95) { // 95% 成功率
            GatewayRefundResponse response = new GatewayRefundResponse();
            response.setGatewayRefundId(generateGatewayTransactionId());
            response.setOriginalGatewayTransactionId(request.getOriginalGatewayTransactionId());
            response.setRefundAmount(request.getRefundAmount());
            response.setSuccess(true);
            response.setStatus("SUCCESS");
            response.setResponseCode("00");
            response.setResponseMessage("Refund processed successfully");
            response.setProcessedAt(LocalDateTime.now());
            
            logger.info("Refund processed successfully: {}", response.getGatewayRefundId());
            return response;
        } else {
            GatewayRefundResponse response = new GatewayRefundResponse();
            response.setOriginalGatewayTransactionId(request.getOriginalGatewayTransactionId());
            response.setRefundAmount(request.getRefundAmount());
            response.setSuccess(false);
            response.setStatus("FAILED");
            response.setResponseCode("96");
            response.setResponseMessage("Refund processing failed");
            response.setFailureReason("系統暫時無法處理退款");
            response.setProcessedAt(LocalDateTime.now());
            
            logger.warn("Refund processing failed for transaction: {}", request.getOriginalGatewayTransactionId());
            return response;
        }
    }
    
    @Override
    public GatewayPaymentResponse queryPaymentStatus(String gatewayTransactionId) {
        logger.info("Querying payment status for gateway transaction: {}", gatewayTransactionId);
        
        simulateProcessingDelay(100, 500);
        
        // 模擬查詢結果
        GatewayPaymentResponse response = new GatewayPaymentResponse();
        response.setGatewayTransactionId(gatewayTransactionId);
        response.setSuccess(true);
        response.setStatus("SUCCESS");
        response.setResponseCode("00");
        response.setResponseMessage("Transaction found");
        response.setProcessedAt(LocalDateTime.now());
        
        return response;
    }
    
    @Override
    public boolean isGatewayHealthy() {
        logger.debug("Checking gateway health");
        
        // 模擬健康檢查
        try {
            Thread.sleep(100);
            return random.nextDouble() > 0.05; // 95% 健康率
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private GatewayPaymentResponse processSuccessfulPayment(GatewayPaymentRequest request) {
        String gatewayTransactionId = generateGatewayTransactionId();
        String authorizationCode = generateAuthorizationCode();
        String receiptNumber = generateReceiptNumber();
        
        GatewayPaymentResponse response = GatewayPaymentResponse.success(
            gatewayTransactionId,
            request.getMerchantReference(),
            request.getAmount(),
            authorizationCode,
            receiptNumber
        );
        
        response.setCurrency("TWD");
        response.setGatewayName("MockGateway");
        response.setGatewayVersion("1.0");
        response.setRiskScore("LOW");
        response.setFraudStatus("PASS");
        
        logger.info("Payment processed successfully: {} with authorization: {}", 
                   gatewayTransactionId, authorizationCode);
        
        return response;
    }
    
    private GatewayPaymentResponse processFailedPayment(GatewayPaymentRequest request) {
        String gatewayTransactionId = generateGatewayTransactionId();
        
        // 模擬不同的失敗原因
        String[] failureReasons = {
            "Invalid card number",
            "Expired card",
            "Invalid CVV",
            "Insufficient funds",
            "Card blocked",
            "Issuer declined"
        };
        
        String[] responseCodes = {"14", "54", "82", "51", "43", "05"};
        String[] chineseReasons = {"卡號無效", "卡片過期", "CVV無效", "餘額不足", "卡片被鎖定", "發卡行拒絕"};
        
        int index = random.nextInt(failureReasons.length);
        
        GatewayPaymentResponse response = GatewayPaymentResponse.failure(
            gatewayTransactionId,
            request.getMerchantReference(),
            request.getAmount(),
            responseCodes[index],
            failureReasons[index],
            chineseReasons[index],
            index < 3 // 前三種錯誤不可重試
        );
        
        response.setCurrency("TWD");
        response.setGatewayName("MockGateway");
        response.setGatewayVersion("1.0");
        
        logger.warn("Payment failed: {} with reason: {}", gatewayTransactionId, failureReasons[index]);
        
        return response;
    }
    
    private void simulateProcessingDelay() {
        simulateProcessingDelay(500, 2000);
    }
    
    private void simulateProcessingDelay(int minMs, int maxMs) {
        try {
            int delay = random.nextInt(maxMs - minMs) + minMs;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Processing delay interrupted");
        }
    }
    
    private String generateGatewayTransactionId() {
        return "GTW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateAuthorizationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    private String generateReceiptNumber() {
        return "RCP-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
    }
}