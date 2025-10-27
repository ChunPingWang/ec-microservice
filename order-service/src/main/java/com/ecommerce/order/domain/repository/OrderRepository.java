package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 訂單倉儲介面
 * 定義訂單資料存取的契約
 */
public interface OrderRepository {
    
    /**
     * 儲存訂單
     */
    Order save(Order order);
    
    /**
     * 根據訂單ID查找訂單
     */
    Optional<Order> findById(String orderId);
    
    /**
     * 根據客戶ID查找所有訂單
     */
    List<Order> findByCustomerId(String customerId);
    
    /**
     * 根據客戶ID和狀態查找訂單
     */
    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
    
    /**
     * 根據客戶ID和日期範圍查找訂單
     */
    List<Order> findByCustomerIdAndOrderDateBetween(String customerId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查找客戶最新的訂單
     */
    Optional<Order> findLatestOrderByCustomerId(String customerId);
    
    /**
     * 查找指定時間之前的待處理訂單
     */
    List<Order> findPendingOrdersOlderThan(LocalDateTime cutoffTime);
    
    /**
     * 刪除訂單
     */
    void delete(Order order);
    
    /**
     * 根據訂單ID刪除訂單
     */
    void deleteById(String orderId);
    
    /**
     * 檢查訂單是否存在
     */
    boolean existsById(String orderId);
    
    /**
     * 計算客戶的訂單總數
     */
    long countByCustomerId(String customerId);
    
    /**
     * 計算指定狀態的訂單總數
     */
    long countByStatus(OrderStatus status);
}