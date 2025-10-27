package com.ecommerce.order.infrastructure.adapter.persistence;

import com.ecommerce.common.architecture.PersistenceAdapter;
import com.ecommerce.order.application.port.out.CartPersistencePort;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.infrastructure.adapter.persistence.entity.CartJpaEntity;
import com.ecommerce.order.infrastructure.adapter.persistence.mapper.CartJpaMapper;
import com.ecommerce.order.infrastructure.adapter.persistence.repository.CartJpaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 購物車 JPA 適配器
 * 實作購物車持久化輸出埠
 */
@PersistenceAdapter
@Transactional
public class CartJpaAdapter implements CartPersistencePort {
    
    private final CartJpaRepository cartJpaRepository;
    
    public CartJpaAdapter(CartJpaRepository cartJpaRepository) {
        this.cartJpaRepository = cartJpaRepository;
    }
    
    @Override
    @CacheEvict(value = {"carts", "customerCarts"}, allEntries = true)
    public Cart save(Cart cart) {
        CartJpaEntity entity = CartJpaMapper.toJpaEntity(cart);
        CartJpaEntity savedEntity = cartJpaRepository.save(entity);
        return CartJpaMapper.toDomainObject(savedEntity);
    }
    
    @Override
    @Cacheable(value = "carts", key = "#cartId")
    public Optional<Cart> findById(String cartId) {
        return cartJpaRepository.findById(cartId)
            .map(CartJpaMapper::toDomainObject);
    }
    
    @Override
    @Cacheable(value = "customerCarts", key = "#customerId")
    public Optional<Cart> findByCustomerId(String customerId) {
        return cartJpaRepository.findByCustomerId(customerId)
            .map(CartJpaMapper::toDomainObject);
    }
    
    public List<Cart> findExpiredCarts() {
        List<CartJpaEntity> entities = cartJpaRepository.findExpiredCarts(LocalDateTime.now());
        return CartJpaMapper.toDomainObjectList(entities);
    }
    
    public List<Cart> findCartsExpiredBefore(LocalDateTime cutoffTime) {
        List<CartJpaEntity> entities = cartJpaRepository.findByExpiryDateBefore(cutoffTime);
        return CartJpaMapper.toDomainObjectList(entities);
    }
    
    public List<Cart> findCartsNotUpdatedSince(LocalDateTime cutoffTime) {
        List<CartJpaEntity> entities = cartJpaRepository.findByLastUpdatedBefore(cutoffTime);
        return CartJpaMapper.toDomainObjectList(entities);
    }
    
    @Override
    public boolean existsById(String cartId) {
        return cartJpaRepository.existsById(cartId);
    }
    
    @Override
    public boolean existsByCustomerId(String customerId) {
        return cartJpaRepository.existsByCustomerId(customerId);
    }
    
    @Override
    @CacheEvict(value = {"carts", "customerCarts"}, allEntries = true)
    public void delete(Cart cart) {
        cartJpaRepository.deleteById(cart.getCartId());
    }
    
    @Override
    @CacheEvict(value = {"carts", "customerCarts"}, allEntries = true)
    public void deleteById(String cartId) {
        cartJpaRepository.deleteById(cartId);
    }
    
    @CacheEvict(value = {"carts", "customerCarts"}, allEntries = true)
    public void deleteByCustomerId(String customerId) {
        cartJpaRepository.deleteByCustomerId(customerId);
    }
    
    @CacheEvict(value = {"carts", "customerCarts"}, allEntries = true)
    public void deleteExpiredCarts() {
        cartJpaRepository.deleteExpiredCarts(LocalDateTime.now());
    }
    
    public long countByCustomerId(String customerId) {
        return cartJpaRepository.countByCustomerId(customerId);
    }
    
    public long countCartsContainingProduct(String productId) {
        return cartJpaRepository.countCartsContainingProduct(productId);
    }
    
    public List<Cart> findCartsContainingProduct(String productId) {
        List<CartJpaEntity> entities = cartJpaRepository.findCartsContainingProduct(productId);
        return CartJpaMapper.toDomainObjectList(entities);
    }
    
    /**
     * 查找活躍的購物車
     */
    public List<Cart> findActiveCarts() {
        List<CartJpaEntity> entities = cartJpaRepository.findActiveCarts(LocalDateTime.now());
        return CartJpaMapper.toDomainObjectList(entities);
    }
    
    /**
     * 統計活躍購物車的數量
     */
    public long countActiveCarts() {
        return cartJpaRepository.countActiveCarts(LocalDateTime.now());
    }
    
    /**
     * 查找空的購物車
     */
    public List<Cart> findEmptyCarts() {
        List<CartJpaEntity> entities = cartJpaRepository.findEmptyCarts();
        return CartJpaMapper.toDomainObjectList(entities);
    }
    
    /**
     * 獲取購物車統計資訊
     */
    public CartStats getCartStats() {
        long totalCarts = cartJpaRepository.count();
        long activeCarts = countActiveCarts();
        
        // 計算平均購物車價值
        List<CartJpaEntity> activeCartEntities = cartJpaRepository.findActiveCarts(LocalDateTime.now());
        java.math.BigDecimal totalValue = activeCartEntities.stream()
            .map(CartJpaEntity::getTotalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal averageValue = activeCarts > 0 ? 
            totalValue.divide(java.math.BigDecimal.valueOf(activeCarts), 2, java.math.RoundingMode.HALF_UP) : 
            java.math.BigDecimal.ZERO;
        
        return new CartStats(totalCarts, activeCarts, averageValue);
    }
    
    /**
     * 購物車統計資訊
     */
    public static class CartStats {
        private final long totalCarts;
        private final long activeCarts;
        private final java.math.BigDecimal averageCartValue;
        
        public CartStats(long totalCarts, long activeCarts, java.math.BigDecimal averageCartValue) {
            this.totalCarts = totalCarts;
            this.activeCarts = activeCarts;
            this.averageCartValue = averageCartValue;
        }
        
        public long getTotalCarts() { return totalCarts; }
        public long getActiveCarts() { return activeCarts; }
        public java.math.BigDecimal getAverageCartValue() { return averageCartValue; }
    }
}