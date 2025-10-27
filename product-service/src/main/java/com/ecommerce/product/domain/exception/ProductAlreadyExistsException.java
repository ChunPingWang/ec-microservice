package com.ecommerce.product.domain.exception;

import com.ecommerce.common.exception.BusinessException;

/**
 * Exception thrown when attempting to create a product that already exists
 * Follows SRP principle by handling only product duplication scenarios
 */
public class ProductAlreadyExistsException extends BusinessException {
    
    public ProductAlreadyExistsException(String brand, String model) {
        super("PRODUCT_ALREADY_EXISTS", 
              String.format("Product already exists with brand '%s' and model '%s'", brand, model));
    }
    
    public ProductAlreadyExistsException(String productId) {
        super("PRODUCT_ALREADY_EXISTS", 
              String.format("Product already exists with ID: %s", productId));
    }
    
    public ProductAlreadyExistsException(String message) {
        super("PRODUCT_ALREADY_EXISTS", message);
    }
}