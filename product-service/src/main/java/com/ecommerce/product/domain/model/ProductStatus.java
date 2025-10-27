package com.ecommerce.product.domain.model;

/**
 * Product status enumeration
 * Defines the possible states of a product in the system
 */
public enum ProductStatus {
    
    /**
     * Product is available for purchase
     */
    AVAILABLE("Available"),
    
    /**
     * Product is temporarily out of stock
     */
    OUT_OF_STOCK("Out of Stock"),
    
    /**
     * Product has been discontinued and is no longer available
     */
    DISCONTINUED("Discontinued");
    
    private final String displayName;
    
    ProductStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isAvailable() {
        return this == AVAILABLE;
    }
    
    public boolean isOutOfStock() {
        return this == OUT_OF_STOCK;
    }
    
    public boolean isDiscontinued() {
        return this == DISCONTINUED;
    }
}