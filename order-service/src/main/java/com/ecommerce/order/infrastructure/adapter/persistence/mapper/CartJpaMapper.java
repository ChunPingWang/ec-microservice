package com.ecommerce.order.infrastructure.adapter.persistence.mapper;

import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.infrastructure.adapter.persistence.entity.CartJpaEntity;
import com.ecommerce.order.infrastructure.adapter.persistence.entity.CartItemJpaEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 購物車 JPA 映射器
 * 負責領域物件與 JPA 實體之間的轉換
 */
public class CartJpaMapper {
    
    /**
     * 將領域物件轉換為 JPA 實體
     */
    public static CartJpaEntity toJpaEntity(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        CartJpaEntity entity = new CartJpaEntity();
        entity.setCartId(cart.getCartId());
        entity.setCustomerId(cart.getCustomerId());
        entity.setTotalAmount(cart.getTotalAmount());
        entity.setLastUpdated(cart.getLastUpdated());
        entity.setExpiryDate(cart.getExpiryDate());
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setUpdatedAt(cart.getUpdatedAt());
        
        // 轉換購物車項目
        List<CartItemJpaEntity> cartItemEntities = cart.getCartItems().stream()
            .map(cartItem -> toCartItemJpaEntity(cartItem, entity))
            .collect(Collectors.toList());
        entity.setCartItems(cartItemEntities);
        
        return entity;
    }
    
    /**
     * 將 JPA 實體轉換為領域物件
     */
    public static Cart toDomainObject(CartJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // 創建購物車領域物件
        Cart cart = Cart.create(entity.getCustomerId());
        
        // 設定其他屬性
        setCartProperties(cart, entity);
        
        // 轉換購物車項目
        for (CartItemJpaEntity itemEntity : entity.getCartItems()) {
            cart.addItem(
                itemEntity.getProductId(),
                itemEntity.getProductName(),
                itemEntity.getUnitPrice(),
                itemEntity.getQuantity(),
                itemEntity.getProductSpecifications()
            );
        }
        
        return cart;
    }
    
    /**
     * 將購物車項目領域物件轉換為 JPA 實體
     */
    public static CartItemJpaEntity toCartItemJpaEntity(Cart.CartItem cartItem, CartJpaEntity cartEntity) {
        if (cartItem == null) {
            return null;
        }
        
        CartItemJpaEntity entity = new CartItemJpaEntity();
        entity.setCartItemId(cartItem.getCartItemId());
        entity.setProductId(cartItem.getProductId());
        entity.setProductName(cartItem.getProductName());
        entity.setUnitPrice(cartItem.getUnitPrice());
        entity.setQuantity(cartItem.getQuantity());
        entity.setTotalPrice(cartItem.getTotalPrice());
        entity.setProductSpecifications(cartItem.getProductSpecifications());
        entity.setCreatedAt(cartItem.getCreatedAt());
        entity.setUpdatedAt(cartItem.getUpdatedAt());
        entity.setCart(cartEntity);
        
        return entity;
    }
    
    /**
     * 將 JPA 實體列表轉換為領域物件列表
     */
    public static List<Cart> toDomainObjectList(List<CartJpaEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(CartJpaMapper::toDomainObject)
            .collect(Collectors.toList());
    }
    
    /**
     * 將領域物件列表轉換為 JPA 實體列表
     */
    public static List<CartJpaEntity> toJpaEntityList(List<Cart> carts) {
        if (carts == null) {
            return null;
        }
        
        return carts.stream()
            .map(CartJpaMapper::toJpaEntity)
            .collect(Collectors.toList());
    }
    
    // Private helper method to set cart properties
    private static void setCartProperties(Cart cart, CartJpaEntity entity) {
        try {
            // 使用反射設定私有屬性
            java.lang.reflect.Field cartIdField = Cart.class.getDeclaredField("cartId");
            cartIdField.setAccessible(true);
            cartIdField.set(cart, entity.getCartId());
            
            java.lang.reflect.Field totalAmountField = Cart.class.getDeclaredField("totalAmount");
            totalAmountField.setAccessible(true);
            totalAmountField.set(cart, entity.getTotalAmount());
            
            java.lang.reflect.Field lastUpdatedField = Cart.class.getDeclaredField("lastUpdated");
            lastUpdatedField.setAccessible(true);
            lastUpdatedField.set(cart, entity.getLastUpdated());
            
            java.lang.reflect.Field expiryDateField = Cart.class.getDeclaredField("expiryDate");
            expiryDateField.setAccessible(true);
            expiryDateField.set(cart, entity.getExpiryDate());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JPA entity to domain object", e);
        }
    }
}