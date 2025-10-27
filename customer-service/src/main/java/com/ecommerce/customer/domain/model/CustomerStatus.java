package com.ecommerce.customer.domain.model;

/**
 * Customer status enumeration
 * Represents the current state of a customer account
 */
public enum CustomerStatus {
    
    /**
     * Customer account is active and can perform operations
     */
    ACTIVE("Active"),
    
    /**
     * Customer account is temporarily inactive
     */
    INACTIVE("Inactive"),
    
    /**
     * Customer account is suspended due to policy violations
     */
    SUSPENDED("Suspended"),
    
    /**
     * Customer account is permanently closed
     */
    CLOSED("Closed");
    
    private final String displayName;
    
    CustomerStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    public boolean canPlaceOrders() {
        return this == ACTIVE;
    }
    
    public boolean canLogin() {
        return this == ACTIVE || this == INACTIVE;
    }
}