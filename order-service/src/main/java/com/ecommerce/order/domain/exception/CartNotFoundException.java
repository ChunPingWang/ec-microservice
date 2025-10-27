package com.ecommerce.order.domain.exception;

import com.ecommerce.common.exception.DomainException;

/**
 * 購物車不存在異常
 */
public class CartNotFoundException extends DomainException {
    
    public CartNotFoundException(String message) {
        super(message);
    }
    
    public static CartNotFoundException byCustomerId(String customerId) {
        return new CartNotFoundException("Cart not found for customer: " + customerId);
    }
    
    public static CartNotFoundException byCartId(String cartId) {
        return new CartNotFoundException("Cart not found: " + cartId);
    }
}