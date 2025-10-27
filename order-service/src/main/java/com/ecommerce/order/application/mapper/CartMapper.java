package com.ecommerce.order.application.mapper;

import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.domain.model.Cart;

/**
 * 購物車映射器
 * 負責 Cart 實體與 CartDto 之間的轉換
 */
public class CartMapper {
    
    /**
     * 將 Cart 實體轉換為 CartDto
     */
    public static CartDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());
        dto.setCustomerId(cart.getCustomerId());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setTotalItemCount(cart.getTotalItemCount());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getLastUpdated());
        dto.setExpiresAt(cart.getExpiryDate());
        
        // 轉換購物車項目
        // 這裡需要根據實際的 Cart 實體結構來實作
        // dto.setCartItems(cart.getCartItems().stream()
        //     .map(CartMapper::toCartItemDto)
        //     .collect(Collectors.toList()));
        
        return dto;
    }
}