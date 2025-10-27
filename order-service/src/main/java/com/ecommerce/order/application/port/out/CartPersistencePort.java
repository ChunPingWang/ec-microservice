package com.ecommerce.order.application.port.out;

import com.ecommerce.order.domain.model.Cart;

import java.util.Optional;

/**
 * 購物車持久化端口
 * 定義購物車資料存取的介面
 */
public interface CartPersistencePort {
    
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
     * 刪除購物車
     */
    void delete(Cart cart);
    
    /**
     * 根據購物車ID刪除購物車
     */
    void deleteById(String cartId);
    
    /**
     * 檢查購物車是否存在
     */
    boolean existsById(String cartId);
    
    /**
     * 檢查客戶是否有購物車
     */
    boolean existsByCustomerId(String customerId);
}