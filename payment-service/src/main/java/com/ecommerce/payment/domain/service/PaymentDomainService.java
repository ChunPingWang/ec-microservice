package com.ecommerce.payment.domain.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.payment.domain.exception.PaymentNotFoundException;
import com.ecommerce.payment.domain.exception.InvalidPaymentStateException;
import com.ecommerce.payment.domain.model.PaymentTransaction;
import com.ecommerce.payment.domain.model.PaymentStatus;
import com.ecommerce.payment.domain.model.PaymentMethod;
import com.ecommerce.payment.domain.model.CreditCard;
import com.ecommerce.payment.domain.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 付款領域服務
 * 處理複雜的付款業務邏輯和規則
 */
@DomainService
public class PaymentDomainService {
    
    private final PaymentRepository paymentRepository;
    
    // 付款限額配置
    private static final BigDecimal MAX_SINGLE_PAYMENT = new BigDecimal("100000"); // 10萬元
    private static final BigDecimal MAX_DAILY_PAYMENT = new BigDecimal("500000");  // 50萬元
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;
    
    public PaymentDomainService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * 驗證付款請求
     */
    public void validatePaymentRequest(String orderId, String customerId, BigDecimal amount, 
                                     PaymentMethod paymentMethod, CreditCard creditCard) {
        // 檢查訂單是否已有成功付款
        if (paymentRepository.hasSuccessfulPaymentForOrder(orderId)) {
            throw new InvalidPaymentStateException("Order already has a successful payment: " + orderId);
        }
        
        // 驗證付款金額
        validatePaymentAmount(amount);
        
        // 驗證每日付款限額
        validateDailyPaymentLimit(customerId, amount);
        
        // 驗證信用卡（如果是信用卡付款）
        if (paymentMethod == PaymentMethod.CREDIT_CARD) {
            validateCreditCard(creditCard);
        }
    }
    
    /**
     * 檢查是否可以重試付款
     */
    public boolean canRetryPayment(String orderId) {
        List<PaymentTransaction> transactions = paymentRepository.findAllByOrderId(orderId);
        
        long failedAttempts = transactions.stream()
            .filter(t -> t.isFailed())
            .count();
        
        return failedAttempts < MAX_RETRY_ATTEMPTS;
    }
    
    /**
     * 計算退款手續費
     */
    public BigDecimal calculateRefundFee(PaymentTransaction transaction, BigDecimal refundAmount) {
        if (transaction.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            // 信用卡退款手續費：退款金額的 1%，最低 10 元，最高 100 元
            BigDecimal feeRate = new BigDecimal("0.01");
            BigDecimal fee = refundAmount.multiply(feeRate);
            BigDecimal minFee = new BigDecimal("10");
            BigDecimal maxFee = new BigDecimal("100");
            
            if (fee.compareTo(minFee) < 0) {
                return minFee;
            } else if (fee.compareTo(maxFee) > 0) {
                return maxFee;
            }
            return fee;
        }
        
        // 其他付款方式暫不收取手續費
        return BigDecimal.ZERO;
    }
    
    /**
     * 檢查付款是否超時
     */
    public boolean isPaymentTimeout(PaymentTransaction transaction) {
        if (transaction.getStatus() != PaymentStatus.PROCESSING) {
            return false;
        }
        
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        return transaction.getCreatedAt().isBefore(timeoutThreshold);
    }
    
    /**
     * 自動取消超時的付款交易
     */
    public List<PaymentTransaction> cancelTimeoutTransactions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        List<PaymentTransaction> timeoutTransactions = paymentRepository.findTimeoutTransactions(cutoffTime);
        
        for (PaymentTransaction transaction : timeoutTransactions) {
            if (transaction.getStatus() == PaymentStatus.PROCESSING) {
                transaction.markAsFailed("Payment timeout", "Transaction timed out after " + PAYMENT_TIMEOUT_MINUTES + " minutes");
                paymentRepository.save(transaction);
            }
        }
        
        return timeoutTransactions;
    }
    
    /**
     * 驗證客戶付款權限
     */
    public void validateCustomerPaymentAccess(String transactionId, String customerId) {
        PaymentTransaction transaction = paymentRepository.findById(transactionId)
            .orElseThrow(() -> PaymentNotFoundException.byTransactionId(transactionId));
        
        if (!transaction.getCustomerId().equals(customerId)) {
            throw new InvalidPaymentStateException("Customer does not have access to this payment transaction");
        }
    }
    
    /**
     * 獲取客戶付款統計資訊
     */
    public CustomerPaymentStats getCustomerPaymentStats(String customerId) {
        List<PaymentTransaction> customerTransactions = paymentRepository.findByCustomerId(customerId);
        
        long totalTransactions = customerTransactions.size();
        long successfulTransactions = customerTransactions.stream()
            .mapToLong(t -> t.isSuccessful() ? 1 : 0)
            .sum();
        long failedTransactions = customerTransactions.stream()
            .mapToLong(t -> t.isFailed() ? 1 : 0)
            .sum();
        
        BigDecimal totalAmount = customerTransactions.stream()
            .filter(PaymentTransaction::isSuccessful)
            .map(PaymentTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new CustomerPaymentStats(customerId, totalTransactions, successfulTransactions, 
                                      failedTransactions, totalAmount);
    }
    
    /**
     * 檢查付款交易是否包含指定訂單
     */
    public boolean transactionContainsOrder(String transactionId, String orderId) {
        PaymentTransaction transaction = paymentRepository.findById(transactionId)
            .orElseThrow(() -> PaymentNotFoundException.byTransactionId(transactionId));
        
        return transaction.getOrderId().equals(orderId);
    }
    
    // Private helper methods
    private void validatePaymentAmount(BigDecimal amount) {
        if (amount.compareTo(MAX_SINGLE_PAYMENT) > 0) {
            throw new InvalidPaymentStateException("Payment amount exceeds maximum limit: " + MAX_SINGLE_PAYMENT);
        }
    }
    
    private void validateDailyPaymentLimit(String customerId, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        BigDecimal todayTotal = paymentRepository.findByCustomerIdAndStatus(customerId, PaymentStatus.SUCCESS)
            .stream()
            .filter(t -> t.getProcessedAt() != null && 
                        t.getProcessedAt().isAfter(startOfDay) && 
                        t.getProcessedAt().isBefore(endOfDay))
            .map(PaymentTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (todayTotal.add(amount).compareTo(MAX_DAILY_PAYMENT) > 0) {
            throw new InvalidPaymentStateException("Daily payment limit exceeded. Limit: " + MAX_DAILY_PAYMENT);
        }
    }
    
    private void validateCreditCard(CreditCard creditCard) {
        if (creditCard == null) {
            throw new InvalidPaymentStateException("Credit card information is required");
        }
        
        try {
            if (creditCard.isExpired()) {
                throw new InvalidPaymentStateException("Credit card has expired");
            }
            
            if (creditCard.isExpiringWithinMonths(1)) {
                // 警告但不阻止付款
                System.out.println("Warning: Credit card expires within 1 month");
            }
        } catch (ValidationException e) {
            // Convert ValidationException to InvalidPaymentStateException
            throw new InvalidPaymentStateException("Credit card validation failed: " + e.getMessage());
        }
    }
    
    /**
     * 客戶付款統計資訊
     */
    public static class CustomerPaymentStats {
        private final String customerId;
        private final long totalTransactions;
        private final long successfulTransactions;
        private final long failedTransactions;
        private final BigDecimal totalAmount;
        
        public CustomerPaymentStats(String customerId, long totalTransactions, long successfulTransactions,
                                  long failedTransactions, BigDecimal totalAmount) {
            this.customerId = customerId;
            this.totalTransactions = totalTransactions;
            this.successfulTransactions = successfulTransactions;
            this.failedTransactions = failedTransactions;
            this.totalAmount = totalAmount;
        }
        
        // Getters
        public String getCustomerId() { return customerId; }
        public long getTotalTransactions() { return totalTransactions; }
        public long getSuccessfulTransactions() { return successfulTransactions; }
        public long getFailedTransactions() { return failedTransactions; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        
        public double getSuccessRate() {
            return totalTransactions > 0 ? (double) successfulTransactions / totalTransactions : 0.0;
        }
        
        public double getFailureRate() {
            return totalTransactions > 0 ? (double) failedTransactions / totalTransactions : 0.0;
        }
    }
}