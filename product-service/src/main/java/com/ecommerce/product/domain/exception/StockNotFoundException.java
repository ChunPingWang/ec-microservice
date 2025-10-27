package com.ecommerce.product.domain.exception;

import com.ecommerce.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when stock information is not found
 * Follows SRP principle by handling only stock not found scenarios
 */
public class StockNotFoundException extends ResourceNotFoundException {
    
    public StockNotFoundException(String stockId) {
        super("Stock not found with ID: " + stockId);
    }
    
    public StockNotFoundException(String field, String value) {
        super("Stock not found with " + field + ": " + value);
    }
    
    public static StockNotFoundException byProductId(String productId) {
        return new StockNotFoundException("product ID", productId);
    }
    
    public static StockNotFoundException byWarehouse(String warehouseLocation) {
        return new StockNotFoundException("warehouse location", warehouseLocation);
    }
}