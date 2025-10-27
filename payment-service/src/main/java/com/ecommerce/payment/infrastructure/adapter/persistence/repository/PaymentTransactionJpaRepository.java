package com.ecommerce.payment.infrastructure.adapter.persistence.repository;

import com.ecommerce.payment.domain.model.PaymentStatus;
import com.ecommerce.payment.infrastructure.adapter.persistence.entity.PaymentTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 付款交易 JPA 倉儲
 * 提供資料庫存取的 Spring Data JPA 介面
 */
@Repository
public interface PaymentTransactionJpaRepository extends JpaRepository<PaymentTransactionJpaEntity, String> {
    
    /**
     * 根據訂單ID查找付款交易
     */
    Optional<PaymentTransactionJpaEntity> findByOrderId(String orderId);
    
    /**
     * 根據訂單ID查找所有付款交易（包含退款）
     */
    List<PaymentTransactionJpaEntity> findAllByOrderId(String orderId);
    
    /**
     * 根據客戶ID查找付款交易
     */
    List<PaymentTransactionJpaEntity> findByCustomerId(String customerId);
    
    /**
     * 根據客戶ID和狀態查找付款交易
     */
    List<PaymentTransactionJpaEntity> findByCustomerIdAndStatus(String customerId, PaymentStatus status);
    
    /**
     * 根據狀態查找付款交易
     */
    List<PaymentTransactionJpaEntity> findByStatus(PaymentStatus status);
    
    /**
     * 根據閘道交易ID查找付款交易
     */
    Optional<PaymentTransactionJpaEntity> findByGatewayTransactionId(String gatewayTransactionId);
    
    /**
     * 查找指定時間範圍內的付款交易
     */
    List<PaymentTransactionJpaEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查找指定時間範圍內的成功付款交易
     */
    @Query("SELECT p FROM PaymentTransactionJpaEntity p WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentTransactionJpaEntity> findSuccessfulPaymentsBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查找超時未處理的付款交易
     */
    @Query("SELECT p FROM PaymentTransactionJpaEntity p WHERE p.status IN ('PENDING', 'PROCESSING') AND p.createdAt < :cutoffTime")
    List<PaymentTransactionJpaEntity> findTimeoutTransactions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 計算客戶的總付款金額
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransactionJpaEntity p WHERE p.customerId = :customerId AND p.status = 'SUCCESS' AND p.amount > 0")
    BigDecimal calculateTotalPaymentsByCustomer(@Param("customerId") String customerId);
    
    /**
     * 計算指定時間範圍內的總付款金額
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransactionJpaEntity p WHERE p.status = 'SUCCESS' AND p.amount > 0 AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPaymentsBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 檢查訂單是否已有成功的付款
     */
    @Query("SELECT COUNT(p) > 0 FROM PaymentTransactionJpaEntity p WHERE p.orderId = :orderId AND p.status = 'SUCCESS' AND p.amount > 0")
    boolean hasSuccessfulPaymentForOrder(@Param("orderId") String orderId);
    
    /**
     * 根據客戶ID和時間範圍查找付款交易
     */
    @Query("SELECT p FROM PaymentTransactionJpaEntity p WHERE p.customerId = :customerId AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<PaymentTransactionJpaEntity> findByCustomerIdAndDateRange(
            @Param("customerId") String customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查找需要重試的失敗付款
     */
    @Query("SELECT p FROM PaymentTransactionJpaEntity p WHERE p.status = 'FAILED' AND p.createdAt > :since ORDER BY p.createdAt DESC")
    List<PaymentTransactionJpaEntity> findFailedPaymentsSince(@Param("since") LocalDateTime since);
    
    /**
     * 根據商戶參考號查找付款交易
     */
    Optional<PaymentTransactionJpaEntity> findByMerchantReference(String merchantReference);
    
    /**
     * 查找指定金額範圍內的付款交易
     */
    @Query("SELECT p FROM PaymentTransactionJpaEntity p WHERE p.amount BETWEEN :minAmount AND :maxAmount AND p.status = 'SUCCESS'")
    List<PaymentTransactionJpaEntity> findByAmountRange(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);
    
    /**
     * 統計各狀態的付款交易數量
     */
    @Query("SELECT p.status, COUNT(p) FROM PaymentTransactionJpaEntity p GROUP BY p.status")
    List<Object[]> countByStatus();
    
    /**
     * 查找最近的付款交易
     */
    @Query("SELECT p FROM PaymentTransactionJpaEntity p ORDER BY p.createdAt DESC")
    List<PaymentTransactionJpaEntity> findRecentTransactions();
}