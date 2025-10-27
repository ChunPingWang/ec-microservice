package com.ecommerce.order.domain.exception;

import com.ecommerce.common.exception.DomainException;

/**
 * 購物車已過期異常
 */
public class CartExpiredException extends DomainException {
    
    public CartExpiredException(String cartId) {
        super("Cart has expired: " + cartId);
    }
    
    public static CartExpiredException byCartId(String cartId) {
        return new CartExpiredException(cartId);
    }
}