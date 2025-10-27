package com.ecommerce.payment.application.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.payment.application.dto.GatewayPaymentRequest;
import com.ecommerce.payment.application.dto.GatewayPaymentResponse;
import com.ecommerce.payment.application.strategy.PaymentStrategy;
import com.ecommerce.payment.application.strategy.PaymentStrategyFactory;
import com.ecommerce.payment.domain.exception.PaymentProcessingException;
import com.ecommerce.payment.domain.model.PaymentFailureReason;
import com.ecommerce.payment.domain.model.PaymentMethod;
import com.ecommerce.payment.domain.model.PaymentTransaction;
import com.ecommerce.payment.domain.service.PaymentDomainService;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 付款重試服務
 * 處理付款失敗的重試邏輯
 */
@DomainService
public class PaymentRetryService {
    
    private final PaymentStrategyFactory strategyFactory;
    private final PaymentDomainService paymentDomainService;
    private final ScheduledExecutorService scheduler;
    
    // 重試配置
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_SECONDS = 30;
    private static final long MAX_RETRY_DELAY_SECONDS = 300; // 5 minutes
    private static final double BACKOFF_MULTIPLIER = 2.0;
    
    public PaymentRetryService(PaymentStrategyFactory strategyFactory,
                             PaymentDomainService paymentDomainService) {
        this.strategyFactory = strategyFactory;
        this.paymentDomainService = paymentDomainService;
        this.scheduler = Executors.newScheduledThreadPool(5);
    }
    
    /**
     * 同步重試付款
     */
    public GatewayPaymentResponse retryPayment(PaymentTransaction transaction, GatewayPaymentRequest request) {
        if (!canRetryPayment(transaction)) {
            throw new PaymentProcessingException(
                "Payment cannot be retried: maximum attempts reached or not retryable",
                PaymentFailureReason.LIMIT_EXCEEDED
            );
        }
        
        PaymentStrategy strategy = strategyFactory.getStrategy(transaction.getPaymentMethod());
        
        // 檢查策略是否可用
        if (!strategy.isAvailable()) {
            throw new PaymentProcessingException(
                "Payment gateway is not available for retry",
                PaymentFailureReason.GATEWAY_ERROR
            );
        }
        
        try {
            // 執行重試
            GatewayPaymentResponse response = strategy.processPayment(request);
            
            // 記錄重試結果
            logRetryAttempt(transaction, response.isSuccessful(), response.getFailureReason());
            
            return response;
            
        } catch (Exception e) {
            logRetryAttempt(transaction, false, e.getMessage());
            throw new PaymentProcessingException(
                "Payment retry failed: " + e.getMessage(),
                PaymentFailureReason.SYSTEM_ERROR,
                e
            );
        }
    }
    
    /**
     * 非同步重試付款
     */
    public CompletableFuture<GatewayPaymentResponse> retryPaymentAsync(PaymentTransaction transaction,
                                                                      GatewayPaymentRequest request) {
        return CompletableFuture.supplyAsync(() -> retryPayment(transaction, request));
    }
    
    /**
     * 排程重試付款
     */
    public CompletableFuture<GatewayPaymentResponse> scheduleRetryPayment(PaymentTransaction transaction,
                                                                         GatewayPaymentRequest request,
                                                                         int attemptNumber) {
        if (!canRetryPayment(transaction)) {
            return CompletableFuture.failedFuture(
                new PaymentProcessingException(
                    "Payment cannot be retried",
                    PaymentFailureReason.LIMIT_EXCEEDED
                )
            );
        }
        
        long delaySeconds = calculateRetryDelay(attemptNumber);
        
        CompletableFuture<GatewayPaymentResponse> future = new CompletableFuture<>();
        
        scheduler.schedule(() -> {
            try {
                GatewayPaymentResponse response = retryPayment(transaction, request);
                future.complete(response);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, delaySeconds, TimeUnit.SECONDS);
        
        return future;
    }
    
    /**
     * 自動重試付款（使用指數退避）
     */
    public CompletableFuture<GatewayPaymentResponse> autoRetryPayment(PaymentTransaction transaction,
                                                                     GatewayPaymentRequest request) {
        return autoRetryPayment(transaction, request, 1);
    }
    
    private CompletableFuture<GatewayPaymentResponse> autoRetryPayment(PaymentTransaction transaction,
                                                                      GatewayPaymentRequest request,
                                                                      int attemptNumber) {
        if (attemptNumber > MAX_RETRY_ATTEMPTS || !canRetryPayment(transaction)) {
            return CompletableFuture.failedFuture(
                new PaymentProcessingException(
                    "Maximum retry attempts reached",
                    PaymentFailureReason.LIMIT_EXCEEDED
                )
            );
        }
        
        return scheduleRetryPayment(transaction, request, attemptNumber)
            .thenCompose(response -> {
                if (response.isSuccessful()) {
                    return CompletableFuture.completedFuture(response);
                } else if (response.isRetryable() && attemptNumber < MAX_RETRY_ATTEMPTS) {
                    // 繼續重試
                    return autoRetryPayment(transaction, request, attemptNumber + 1);
                } else {
                    // 不可重試或達到最大重試次數
                    return CompletableFuture.completedFuture(response);
                }
            });
    }
    
    /**
     * 檢查是否可以重試付款
     */
    public boolean canRetryPayment(PaymentTransaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        // 檢查交易狀態
        if (!transaction.isFailed() && !transaction.isPending()) {
            return false;
        }
        
        // 檢查重試次數限制
        if (!paymentDomainService.canRetryPayment(transaction.getOrderId())) {
            return false;
        }
        
        // 檢查付款方式是否支援重試
        if (!strategyFactory.isSupported(transaction.getPaymentMethod())) {
            return false;
        }
        
        // 檢查策略是否可用
        return strategyFactory.isStrategyAvailable(transaction.getPaymentMethod());
    }
    
    /**
     * 檢查失敗原因是否可重試
     */
    public boolean isRetryableFailure(String failureReason) {
        if (failureReason == null) {
            return false;
        }
        
        // 網路相關錯誤可重試
        if (failureReason.contains("NETWORK_ERROR") || 
            failureReason.contains("TIMEOUT") ||
            failureReason.contains("GATEWAY_ERROR")) {
            return true;
        }
        
        // 系統錯誤可重試
        if (failureReason.contains("SYSTEM_ERROR")) {
            return true;
        }
        
        // 暫時性錯誤可重試
        if (failureReason.contains("TEMPORARY_ERROR") ||
            failureReason.contains("SERVICE_UNAVAILABLE")) {
            return true;
        }
        
        // 其他錯誤不可重試（如卡片問題、餘額不足等）
        return false;
    }
    
    /**
     * 取得建議的重試時間
     */
    public LocalDateTime getNextRetryTime(int attemptNumber) {
        long delaySeconds = calculateRetryDelay(attemptNumber);
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
    
    /**
     * 關閉重試服務
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // Private helper methods
    private long calculateRetryDelay(int attemptNumber) {
        if (attemptNumber <= 1) {
            return INITIAL_RETRY_DELAY_SECONDS;
        }
        
        // 指數退避算法
        long delay = (long) (INITIAL_RETRY_DELAY_SECONDS * Math.pow(BACKOFF_MULTIPLIER, attemptNumber - 1));
        
        // 限制最大延遲時間
        return Math.min(delay, MAX_RETRY_DELAY_SECONDS);
    }
    
    private void logRetryAttempt(PaymentTransaction transaction, boolean success, String details) {
        String logMessage = String.format(
            "Payment retry attempt for transaction %s: %s. Details: %s",
            transaction.getTransactionId(),
            success ? "SUCCESS" : "FAILED",
            details != null ? details : "N/A"
        );
        
        // 這裡可以整合實際的日誌系統
        System.out.println("[RETRY] " + logMessage);
    }
}