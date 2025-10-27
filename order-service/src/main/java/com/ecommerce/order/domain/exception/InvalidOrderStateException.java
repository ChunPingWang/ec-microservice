package com.ecommerce.order.domain.exception;

import com.ecommerce.common.exception.DomainException;
import com.ecommerce.order.domain.model.OrderStatus;

/**
 * 無效訂單狀態異常
 */
public class InvalidOrderStateException extends DomainException {
    
    public InvalidOrderStateException(String message) {
        super(message);
    }
    
    public static InvalidOrderStateException cannotTransition(OrderStatus from, OrderStatus to) {
        return new InvalidOrderStateException(
            String.format("Cannot transition order status from %s to %s", from, to)
        );
    }
    
    public static InvalidOrderStateException cannotModifyInStatus(OrderStatus status) {
        return new InvalidOrderStateException(
            String.format("Cannot modify order in status: %s", status)
        );
    }
    
    public static InvalidOrderStateException cannotCancelInStatus(OrderStatus status) {
        return new InvalidOrderStateException(
            String.format("Cannot cancel order in status: %s", status)
        );
    }
}