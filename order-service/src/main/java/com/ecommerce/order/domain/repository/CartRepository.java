package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.model.Cart;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 購物車倉儲介面
 * 定義購物車資料存取的契約
 */
public interface CartRepository {
    
    /**
     * 儲存購物車
     */
    Cart save(Cart cart);
    
    /**
     * 根據購物車ID查找購物車
     */
    Optional<Cart> findById(String cartId);
    
    /**
     * 根據客戶ID查找購物車
     */
    Optional<Cart> findByCustomerId(String customerId);
    
    /**
     * 查找所有過期的購物車
     */
    List<Cart> findExpiredCarts();
    
    /**
     * 查找指定時間之前的過期購物車
     */
    List<Cart> findCartsExpiredBefore(LocalDateTime cutoffTime);
    
    /**
     * 查找長時間未更新的購物車
     */
    List<Cart> findCartsNotUpdatedSince(LocalDateTime cutoffTime);
    
    /**
     * 檢查購物車是否存在
     */
    boolean existsById(String cartId);
    
    /**
     * 檢查客戶是否有購物車
     */
    boolean existsByCustomerId(String customerId);
    
    /**
     * 刪除購物車
     */
    void deleteById(String cartId);
    
    /**
     * 根據客戶ID刪除購物車
     */
    void deleteByCustomerId(String customerId);
    
    /**
     * 刪除所有過期的購物車
     */
    void deleteExpiredCarts();
    
    /**
     * 統計客戶的購物車數量
     */
    long countByCustomerId(String customerId);
    
    /**
     * 統計包含指定商品的購物車數量
     */
    long countCartsContainingProduct(String productId);
    
    /**
     * 查找包含指定商品的購物車
     */
    List<Cart> findCartsContainingProduct(String productId);
}