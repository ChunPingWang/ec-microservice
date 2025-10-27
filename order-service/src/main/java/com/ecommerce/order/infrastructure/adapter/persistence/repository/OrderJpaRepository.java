package com.ecommerce.order.infrastructure.adapter.persistence.repository;

import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.order.infrastructure.adapter.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 訂單 JPA Repository
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {
    
    /**
     * 根據客戶ID查找訂單列表
     */
    List<OrderJpaEntity> findByCustomerIdOrderByOrderDateDesc(String customerId);
    
    /**
     * 根據客戶ID和狀態查找訂單
     */
    List<OrderJpaEntity> findByCustomerIdAndStatusOrderByOrderDateDesc(String customerId, OrderStatus status);
    
    /**
     * 根據狀態查找訂單
     */
    List<OrderJpaEntity> findByStatusOrderByOrderDateDesc(OrderStatus status);
    
    /**
     * 根據日期範圍查找訂單
     */
    List<OrderJpaEntity> findByOrderDateBetweenOrderByOrderDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根據客戶ID和日期範圍查找訂單
     */
    List<OrderJpaEntity> findByCustomerIdAndOrderDateBetweenOrderByOrderDateDesc(
        String customerId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查找需要自動取消的訂單（超過指定時間未付款）
     */
    @Query("SELECT o FROM OrderJpaEntity o WHERE o.status IN ('PENDING', 'CONFIRMED') AND o.orderDate < :cutoffTime")
    List<OrderJpaEntity> findPendingOrdersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 統計客戶的訂單數量
     */
    long countByCustomerId(String customerId);
    
    /**
     * 統計指定狀態的訂單數量
     */
    long countByStatus(OrderStatus status);
    
    /**
     * 根據商品ID查找包含該商品的訂單
     */
    @Query("SELECT DISTINCT o FROM OrderJpaEntity o JOIN o.orderItems oi WHERE oi.productId = :productId")
    List<OrderJpaEntity> findOrdersContainingProduct(@Param("productId") String productId);
    
    /**
     * 查找客戶的最近訂單
     */
    Optional<OrderJpaEntity> findFirstByCustomerIdOrderByOrderDateDesc(String customerId);
    
    /**
     * 檢查訂單是否存在
     */
    boolean existsByOrderId(String orderId);
}