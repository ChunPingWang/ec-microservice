package com.ecommerce.order.infrastructure.adapter.persistence.repository;

import com.ecommerce.order.infrastructure.adapter.persistence.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 購物車 JPA Repository
 */
@Repository
public interface CartJpaRepository extends JpaRepository<CartJpaEntity, String> {
    
    /**
     * 根據客戶ID查找購物車
     */
    Optional<CartJpaEntity> findByCustomerId(String customerId);
    
    /**
     * 查找所有過期的購物車
     */
    @Query("SELECT c FROM CartJpaEntity c WHERE c.expiryDate < :now")
    List<CartJpaEntity> findExpiredCarts(@Param("now") LocalDateTime now);
    
    /**
     * 查找指定時間之前的過期購物車
     */
    List<CartJpaEntity> findByExpiryDateBefore(LocalDateTime cutoffTime);
    
    /**
     * 查找長時間未更新的購物車
     */
    List<CartJpaEntity> findByLastUpdatedBefore(LocalDateTime cutoffTime);
    
    /**
     * 檢查客戶是否有購物車
     */
    boolean existsByCustomerId(String customerId);
    
    /**
     * 根據客戶ID刪除購物車
     */
    void deleteByCustomerId(String customerId);
    
    /**
     * 刪除所有過期的購物車
     */
    @Modifying
    @Query("DELETE FROM CartJpaEntity c WHERE c.expiryDate < :now")
    void deleteExpiredCarts(@Param("now") LocalDateTime now);
    
    /**
     * 統計客戶的購物車數量
     */
    long countByCustomerId(String customerId);
    
    /**
     * 統計包含指定商品的購物車數量
     */
    @Query("SELECT COUNT(DISTINCT c) FROM CartJpaEntity c JOIN c.cartItems ci WHERE ci.productId = :productId")
    long countCartsContainingProduct(@Param("productId") String productId);
    
    /**
     * 查找包含指定商品的購物車
     */
    @Query("SELECT DISTINCT c FROM CartJpaEntity c JOIN c.cartItems ci WHERE ci.productId = :productId")
    List<CartJpaEntity> findCartsContainingProduct(@Param("productId") String productId);
    
    /**
     * 查找活躍的購物車（未過期且有商品）
     */
    @Query("SELECT c FROM CartJpaEntity c WHERE c.expiryDate > :now AND SIZE(c.cartItems) > 0")
    List<CartJpaEntity> findActiveCarts(@Param("now") LocalDateTime now);
    
    /**
     * 統計活躍購物車的數量
     */
    @Query("SELECT COUNT(c) FROM CartJpaEntity c WHERE c.expiryDate > :now AND SIZE(c.cartItems) > 0")
    long countActiveCarts(@Param("now") LocalDateTime now);
    
    /**
     * 查找空的購物車
     */
    @Query("SELECT c FROM CartJpaEntity c WHERE SIZE(c.cartItems) = 0")
    List<CartJpaEntity> findEmptyCarts();
}