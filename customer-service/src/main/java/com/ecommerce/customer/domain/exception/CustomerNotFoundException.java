package com.ecommerce.customer.domain.exception;

import com.ecommerce.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a customer is not found
 */
public class CustomerNotFoundException extends ResourceNotFoundException {
    
    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId);
    }
    
    public CustomerNotFoundException(String field, String value) {
        super("Customer not found with " + field + ": " + value);
    }
}