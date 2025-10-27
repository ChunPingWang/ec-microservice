package com.ecommerce.order.domain.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.order.domain.exception.CartExpiredException;
import com.ecommerce.order.domain.exception.CartNotFoundException;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.repository.CartRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 購物車領域服務
 * 處理複雜的購物車業務邏輯和規則
 */
@DomainService
public class CartDomainService {
    
    private final CartRepository cartRepository;
    
    public CartDomainService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    /**
     * 獲取或創建客戶的購物車
     */
    public Cart getOrCreateCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
            .orElseGet(() -> {
                Cart newCart = Cart.create(customerId);
                return cartRepository.save(newCart);
            });
    }
    
    /**
     * 驗證購物車是否有效（存在且未過期）
     */
    public void validateCartAccess(String cartId, String customerId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> CartNotFoundException.byCartId(cartId));
        
        if (!cart.getCustomerId().equals(customerId)) {
            throw new CartNotFoundException("Cart does not belong to customer: " + customerId);
        }
        
        if (cart.isExpired()) {
            throw CartExpiredException.byCartId(cartId);
        }
    }
    
    /**
     * 合併兩個購物車（通常用於用戶登入後合併匿名購物車）
     */
    public Cart mergeCarts(String targetCustomerId, Cart sourceCart) {
        Cart targetCart = getOrCreateCart(targetCustomerId);
        
        // 將來源購物車的商品加入目標購物車
        for (Cart.CartItem sourceItem : sourceCart.getCartItems()) {
            targetCart.addItem(
                sourceItem.getProductId(),
                sourceItem.getProductName(),
                sourceItem.getUnitPrice(),
                sourceItem.getQuantity(),
                sourceItem.getProductSpecifications()
            );
        }
        
        // 儲存合併後的購物車
        Cart mergedCart = cartRepository.save(targetCart);
        
        // 刪除來源購物車
        cartRepository.deleteById(sourceCart.getCartId());
        
        return mergedCart;
    }
    
    /**
     * 清理過期的購物車
     */
    public int cleanupExpiredCarts() {
        List<Cart> expiredCarts = cartRepository.findExpiredCarts();
        
        for (Cart cart : expiredCarts) {
            cartRepository.deleteById(cart.getCartId());
        }
        
        return expiredCarts.size();
    }
    
    /**
     * 清理長時間未更新的購物車
     */
    public int cleanupAbandonedCarts(int daysThreshold) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysThreshold);
        List<Cart> abandonedCarts = cartRepository.findCartsNotUpdatedSince(cutoffTime);
        
        for (Cart cart : abandonedCarts) {
            cartRepository.deleteById(cart.getCartId());
        }
        
        return abandonedCarts.size();
    }
    
    /**
     * 延長購物車的有效期
     */
    public Cart extendCartExpiry(String cartId, int days) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> CartNotFoundException.byCartId(cartId));
        
        cart.extendExpiry(days);
        return cartRepository.save(cart);
    }
    
    /**
     * 檢查購物車中是否包含指定商品
     */
    public boolean cartContainsProduct(String customerId, String productId) {
        return cartRepository.findByCustomerId(customerId)
            .map(cart -> cart.containsProduct(productId))
            .orElse(false);
    }
    
    /**
     * 獲取包含指定商品的購物車數量（用於商品熱度分析）
     */
    public long getCartCountForProduct(String productId) {
        return cartRepository.countCartsContainingProduct(productId);
    }
    
    /**
     * 驗證購物車是否可以轉換為訂單
     */
    public void validateCartForOrder(Cart cart) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }
        
        if (cart.isExpired()) {
            throw CartExpiredException.byCartId(cart.getCartId());
        }
        
        // 檢查購物車項目數量限制
        if (cart.getTotalItemCount() > 100) {
            throw new IllegalStateException("Cart contains too many items (max: 100)");
        }
        
        // 檢查購物車總金額
        if (cart.getTotalAmount().compareTo(new java.math.BigDecimal("1000000")) > 0) {
            throw new IllegalStateException("Cart total amount exceeds maximum limit");
        }
    }
    
    /**
     * 獲取購物車統計資訊
     */
    public CartStats getCartStats() {
        // 這裡可以實作更複雜的統計邏輯
        // 目前簡化實作
        return new CartStats(0, 0, java.math.BigDecimal.ZERO);
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
        
        // Getters
        public long getTotalCarts() { return totalCarts; }
        public long getActiveCarts() { return activeCarts; }
        public java.math.BigDecimal getAverageCartValue() { return averageCartValue; }
        
        public double getActiveCartRate() {
            return totalCarts > 0 ? (double) activeCarts / totalCarts : 0.0;
        }
    }
}