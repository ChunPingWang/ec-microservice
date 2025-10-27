package com.ecommerce.product.domain.event;

import com.ecommerce.common.architecture.DomainEvent;

import java.time.LocalDateTime;

/**
 * Domain event fired when a product is restocked
 * Triggers notification processes for customers waiting for stock availability
 */
public class ProductRestockedEvent extends DomainEvent {
    
    private final String productId;
    private final String productName;
    private final String brand;
    private final String model;
    private final String category;
    private final Integer previousQuantity;
    private final Integer newQuantity;
    private final Integer quantityAdded;
    private final String warehouseLocation;
    private final LocalDateTime restockedAt;
    
    public ProductRestockedEvent(String productId, String productName, String brand, String model,
                               String category, Integer previousQuantity, Integer newQuantity,
                               Integer quantityAdded, String warehouseLocation, LocalDateTime restockedAt) {
        super("ProductRestocked");
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.quantityAdded = quantityAdded;
        this.warehouseLocation = warehouseLocation;
        this.restockedAt = restockedAt;
    }
    
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getCategory() { return category; }
    public Integer getPreviousQuantity() { return previousQuantity; }
    public Integer getNewQuantity() { return newQuantity; }
    public Integer getQuantityAdded() { return quantityAdded; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public LocalDateTime getRestockedAt() { return restockedAt; }
    
    public String getFullProductName() {
        return brand + " " + productName;
    }
    
    public boolean wasOutOfStock() {
        return previousQuantity <= 0;
    }
    
    @Override
    public String toString() {
        return "ProductRestockedEvent{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", category='" + category + '\'' +
                ", previousQuantity=" + previousQuantity +
                ", newQuantity=" + newQuantity +
                ", quantityAdded=" + quantityAdded +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                ", restockedAt=" + restockedAt +
                ", eventId=" + getEventId() +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}