package com.ecommerce.order.domain.exception;

import com.ecommerce.common.exception.DomainException;

/**
 * 訂單不存在異常
 */
public class OrderNotFoundException extends DomainException {
    
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
    
    public static OrderNotFoundException byOrderId(String orderId) {
        return new OrderNotFoundException(orderId);
    }
    
    public static OrderNotFoundException byCustomerId(String customerId) {
        return new OrderNotFoundException("No orders found for customer: " + customerId);
    }
}