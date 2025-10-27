package com.ecommerce.customer.domain.exception;

import com.ecommerce.common.exception.BusinessException;

/**
 * Exception thrown when attempting to create a customer with duplicate email or phone
 */
public class DuplicateCustomerException extends BusinessException {
    
    public DuplicateCustomerException(String field, String value) {
        super("Customer already exists with " + field + ": " + value);
    }
    
    public DuplicateCustomerException(String message) {
        super(message);
    }
}