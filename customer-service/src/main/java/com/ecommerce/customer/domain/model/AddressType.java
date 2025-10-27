package com.ecommerce.customer.domain.model;

/**
 * Address type enumeration
 * Represents different types of addresses a customer can have
 */
public enum AddressType {
    
    /**
     * Home/residential address
     */
    HOME("Home"),
    
    /**
     * Work/office address
     */
    WORK("Work"),
    
    /**
     * Billing address for payments
     */
    BILLING("Billing"),
    
    /**
     * Shipping/delivery address
     */
    SHIPPING("Shipping"),
    
    /**
     * Other type of address
     */
    OTHER("Other");
    
    private final String displayName;
    
    AddressType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isResidential() {
        return this == HOME;
    }
    
    public boolean isCommercial() {
        return this == WORK;
    }
    
    public boolean canBeUsedForShipping() {
        return this == HOME || this == WORK || this == SHIPPING;
    }
    
    public boolean canBeUsedForBilling() {
        return this == HOME || this == WORK || this == BILLING;
    }
}