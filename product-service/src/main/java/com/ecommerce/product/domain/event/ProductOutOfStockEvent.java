package com.ecommerce.product.domain.event;

import com.ecommerce.common.architecture.DomainEvent;

import java.time.LocalDateTime;

/**
 * Domain event fired when a product goes out of stock
 * Triggers notification processes for stock alerts and customer notifications
 */
public class ProductOutOfStockEvent extends DomainEvent {
    
    private final String productId;
    private final String productName;
    private final String brand;
    private final String model;
    private final String category;
    private final Integer lastAvailableQuantity;
    private final String warehouseLocation;
    private final LocalDateTime outOfStockAt;
    
    public ProductOutOfStockEvent(String productId, String productName, String brand, String model,
                                String category, Integer lastAvailableQuantity, 
                                String warehouseLocation, LocalDateTime outOfStockAt) {
        super("ProductOutOfStock");
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.lastAvailableQuantity = lastAvailableQuantity;
        this.warehouseLocation = warehouseLocation;
        this.outOfStockAt = outOfStockAt;
    }
    
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getCategory() { return category; }
    public Integer getLastAvailableQuantity() { return lastAvailableQuantity; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public LocalDateTime getOutOfStockAt() { return outOfStockAt; }
    
    public String getFullProductName() {
        return brand + " " + productName;
    }
    
    @Override
    public String toString() {
        return "ProductOutOfStockEvent{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", category='" + category + '\'' +
                ", lastAvailableQuantity=" + lastAvailableQuantity +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                ", outOfStockAt=" + outOfStockAt +
                ", eventId=" + getEventId() +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}