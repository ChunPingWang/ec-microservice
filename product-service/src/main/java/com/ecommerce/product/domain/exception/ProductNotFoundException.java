package com.ecommerce.product.domain.exception;

import com.ecommerce.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a product is not found
 * Follows SRP principle by handling only product not found scenarios
 */
public class ProductNotFoundException extends ResourceNotFoundException {
    
    public ProductNotFoundException(String productId) {
        super("Product not found with ID: " + productId);
    }
    
    public ProductNotFoundException(String field, String value) {
        super("Product not found with " + field + ": " + value);
    }
    
    public static ProductNotFoundException byBrandAndModel(String brand, String model) {
        return new ProductNotFoundException("brand and model", brand + " " + model);
    }
    
    public static ProductNotFoundException byCategory(String category) {
        return new ProductNotFoundException("category", category);
    }
}