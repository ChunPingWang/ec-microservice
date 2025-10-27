package com.ecommerce.payment.domain.repository;

import com.ecommerce.payment.domain.model.PaymentStatus;
import com.ecommerce.payment.domain.model.PaymentTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 付款倉儲介面
 * 定義付款資料存取的契約
 */
public interface PaymentRepository {
    
    /**
     * 儲存付款交易
     */
    PaymentTransaction save(PaymentTransaction transaction);
    
    /**
     * 根據交易ID查找付款交易
     */
    Optional<PaymentTransaction> findById(String transactionId);
    
    /**
     * 根據訂單ID查找付款交易
     */
    Optional<PaymentTransaction> findByOrderId(String orderId);
    
    /**
     * 根據訂單ID查找所有付款交易（包含退款）
     */
    List<PaymentTransaction> findAllByOrderId(String orderId);
    
    /**
     * 根據客戶ID查找付款交易
     */
    List<PaymentTransaction> findByCustomerId(String customerId);
    
    /**
     * 根據客戶ID和狀態查找付款交易
     */
    List<PaymentTransaction> findByCustomerIdAndStatus(String customerId, PaymentStatus status);
    
    /**
     * 根據狀態查找付款交易
     */
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    
    /**
     * 根據閘道交易ID查找付款交易
     */
    Optional<PaymentTransaction> findByGatewayTransactionId(String gatewayTransactionId);
    
    /**
     * 查找指定時間範圍內的付款交易
     */
    List<PaymentTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查找指定時間範圍內的成功付款交易
     */
    List<PaymentTransaction> findSuccessfulPaymentsBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查找超時未處理的付款交易
     */
    List<PaymentTransaction> findTimeoutTransactions(LocalDateTime cutoffTime);
    
    /**
     * 計算客戶的總付款金額
     */
    BigDecimal calculateTotalPaymentsByCustomer(String customerId);
    
    /**
     * 計算指定時間範圍內的總付款金額
     */
    BigDecimal calculateTotalPaymentsBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 檢查付款交易是否存在
     */
    boolean existsById(String transactionId);
    
    /**
     * 檢查訂單是否已有成功的付款
     */
    boolean hasSuccessfulPaymentForOrder(String orderId);
    
    /**
     * 刪除付款交易
     */
    void delete(PaymentTransaction transaction);
    
    /**
     * 根據交易ID刪除付款交易
     */
    void deleteById(String transactionId);
}