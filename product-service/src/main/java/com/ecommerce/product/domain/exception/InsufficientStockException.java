package com.ecommerce.product.domain.exception;

import com.ecommerce.common.exception.BusinessException;

/**
 * Exception thrown when there is insufficient stock for an operation
 * Follows SRP principle by handling only stock insufficiency scenarios
 */
public class InsufficientStockException extends BusinessException {
    
    private final String productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;
    
    public InsufficientStockException(String productId, Integer requestedQuantity, Integer availableQuantity) {
        super("INSUFFICIENT_STOCK", 
              String.format("Insufficient stock for product %s. Requested: %d, Available: %d", 
                          productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public InsufficientStockException(String productId, Integer requestedQuantity) {
        super("INSUFFICIENT_STOCK", 
              String.format("Insufficient stock for product %s. Requested: %d", 
                          productId, requestedQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = 0;
    }
    
    public InsufficientStockException(String message) {
        super("INSUFFICIENT_STOCK", message);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public static InsufficientStockException forReservation(String productId, Integer requestedQuantity, Integer availableQuantity) {
        return new InsufficientStockException(productId, requestedQuantity, availableQuantity);
    }
    
    public static InsufficientStockException forSale(String productId, Integer requestedQuantity, Integer availableQuantity) {
        return new InsufficientStockException(productId, requestedQuantity, availableQuantity);
    }
    
    public static InsufficientStockException outOfStock(String productId) {
        return new InsufficientStockException(productId + " is out of stock");
    }
}