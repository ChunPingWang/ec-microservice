package com.ecommerce.order.application.port.in;

import com.ecommerce.order.application.dto.AddToCartRequest;
import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.dto.UpdateCartItemRequest;

/**
 * 購物車使用案例介面
 * 定義購物車相關的業務操作
 */
public interface CartUseCase {
    
    /**
     * 獲取或創建客戶的購物車
     */
    CartDto getOrCreateCart(String customerId);
    
    /**
     * 加入商品到購物車
     */
    CartDto addToCart(String customerId, AddToCartRequest request);
    
    /**
     * 更新購物車項目數量
     */
    CartDto updateCartItem(String customerId, String productId, UpdateCartItemRequest request);
    
    /**
     * 從購物車移除商品
     */
    CartDto removeFromCart(String customerId, String productId);
    
    /**
     * 清空購物車
     */
    CartDto clearCart(String customerId);
    
    /**
     * 獲取客戶的購物車
     */
    CartDto getCart(String customerId);
    
    /**
     * 檢查購物車是否包含指定商品
     */
    boolean cartContainsProduct(String customerId, String productId);
    
    /**
     * 延長購物車有效期
     */
    CartDto extendCartExpiry(String customerId, int days);
    
    /**
     * 合併購物車（用於用戶登入後合併匿名購物車）
     */
    CartDto mergeCarts(String targetCustomerId, String sourceCartId);
    
    /**
     * 驗證購物車是否可以建立訂單
     */
    void validateCartForOrder(String customerId);
    
    /**
     * 清理過期的購物車
     */
    int cleanupExpiredCarts();
    
    /**
     * 清理長時間未更新的購物車
     */
    int cleanupAbandonedCarts(int daysThreshold);
}