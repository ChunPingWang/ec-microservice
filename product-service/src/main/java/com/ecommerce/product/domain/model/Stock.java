package com.ecommerce.product.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.common.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Stock domain entity following DDD principles
 * Manages inventory levels and stock operations for products
 */
public class Stock extends BaseEntity {
    
    private String stockId;
    private String productId;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer minimumThreshold;
    private Integer maximumCapacity;
    private String warehouseLocation;
    private LocalDateTime lastRestockDate;
    private LocalDateTime lastSaleDate;
    
    // Private constructor for JPA
    protected Stock() {}
    
    // Factory method for creating new stock records
    public static Stock create(String productId, Integer initialQuantity, 
                              Integer minimumThreshold, String warehouseLocation) {
        Stock stock = new Stock();
        stock.stockId = generateStockId();
        stock.setProductId(productId);
        stock.setQuantity(initialQuantity);
        stock.reservedQuantity = 0;
        stock.setMinimumThreshold(minimumThreshold);
        stock.maximumCapacity = 10000; // Default maximum capacity
        stock.setWarehouseLocation(warehouseLocation);
        stock.lastRestockDate = LocalDateTime.now();
        return stock;
    }
    
    // Business methods for stock management
    public void addStock(Integer additionalQuantity) {
        if (additionalQuantity == null || additionalQuantity <= 0) {
            throw new ValidationException("Additional quantity must be positive");
        }
        
        Integer newQuantity = this.quantity + additionalQuantity;
        if (newQuantity > maximumCapacity) {
            throw new ValidationException("Cannot exceed maximum capacity of " + maximumCapacity);
        }
        
        this.quantity = newQuantity;
        this.lastRestockDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reduceStock(Integer quantityToReduce) {
        if (quantityToReduce == null || quantityToReduce <= 0) {
            throw new ValidationException("Quantity to reduce must be positive");
        }
        
        if (!hasAvailableStock(quantityToReduce)) {
            throw new ValidationException("Insufficient available stock. Available: " + getAvailableQuantity() + 
                                        ", Requested: " + quantityToReduce);
        }
        
        this.quantity -= quantityToReduce;
        this.lastSaleDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reserveStock(Integer quantityToReserve) {
        if (quantityToReserve == null || quantityToReserve <= 0) {
            throw new ValidationException("Quantity to reserve must be positive");
        }
        
        if (!hasAvailableStock(quantityToReserve)) {
            throw new ValidationException("Insufficient available stock for reservation. Available: " + 
                                        getAvailableQuantity() + ", Requested: " + quantityToReserve);
        }
        
        this.reservedQuantity += quantityToReserve;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void releaseReservation(Integer quantityToRelease) {
        if (quantityToRelease == null || quantityToRelease <= 0) {
            throw new ValidationException("Quantity to release must be positive");
        }
        
        if (quantityToRelease > this.reservedQuantity) {
            throw new ValidationException("Cannot release more than reserved quantity. Reserved: " + 
                                        this.reservedQuantity + ", Requested: " + quantityToRelease);
        }
        
        this.reservedQuantity -= quantityToRelease;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void confirmReservation(Integer quantityToConfirm) {
        if (quantityToConfirm == null || quantityToConfirm <= 0) {
            throw new ValidationException("Quantity to confirm must be positive");
        }
        
        if (quantityToConfirm > this.reservedQuantity) {
            throw new ValidationException("Cannot confirm more than reserved quantity. Reserved: " + 
                                        this.reservedQuantity + ", Requested: " + quantityToConfirm);
        }
        
        this.reservedQuantity -= quantityToConfirm;
        this.quantity -= quantityToConfirm;
        this.lastSaleDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateThresholds(Integer minimumThreshold, Integer maximumCapacity) {
        setMinimumThreshold(minimumThreshold);
        setMaximumCapacity(maximumCapacity);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void relocateStock(String newWarehouseLocation) {
        setWarehouseLocation(newWarehouseLocation);
        this.updatedAt = LocalDateTime.now();
    }
    
    // Query methods
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
    
    public boolean hasAvailableStock(Integer requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }
    
    public boolean isLowStock() {
        return quantity <= minimumThreshold;
    }
    
    public boolean isOutOfStock() {
        return quantity <= 0;
    }
    
    public boolean hasReservedStock() {
        return reservedQuantity > 0;
    }
    
    public boolean isAtCapacity() {
        return quantity >= maximumCapacity;
    }
    
    public Integer getStockUtilizationPercentage() {
        if (maximumCapacity == 0) return 0;
        return (quantity * 100) / maximumCapacity;
    }
    
    // Private helper methods
    private static String generateStockId() {
        return "STOCK-" + System.currentTimeMillis();
    }
    
    // Validation methods
    private void setProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new ValidationException("Product ID is required");
        }
        this.productId = productId.trim();
    }
    
    private void setQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new ValidationException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }
    
    private void setMinimumThreshold(Integer minimumThreshold) {
        if (minimumThreshold == null || minimumThreshold < 0) {
            throw new ValidationException("Minimum threshold cannot be negative");
        }
        this.minimumThreshold = minimumThreshold;
    }
    
    private void setMaximumCapacity(Integer maximumCapacity) {
        if (maximumCapacity == null || maximumCapacity <= 0) {
            throw new ValidationException("Maximum capacity must be positive");
        }
        if (minimumThreshold != null && maximumCapacity < minimumThreshold) {
            throw new ValidationException("Maximum capacity cannot be less than minimum threshold");
        }
        this.maximumCapacity = maximumCapacity;
    }
    
    private void setWarehouseLocation(String warehouseLocation) {
        if (warehouseLocation == null || warehouseLocation.trim().isEmpty()) {
            throw new ValidationException("Warehouse location is required");
        }
        if (warehouseLocation.length() > 100) {
            throw new ValidationException("Warehouse location cannot exceed 100 characters");
        }
        this.warehouseLocation = warehouseLocation.trim();
    }
    
    // Getters
    public String getStockId() { return stockId; }
    public String getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public Integer getMinimumThreshold() { return minimumThreshold; }
    public Integer getMaximumCapacity() { return maximumCapacity; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public LocalDateTime getLastRestockDate() { return lastRestockDate; }
    public LocalDateTime getLastSaleDate() { return lastSaleDate; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return Objects.equals(stockId, stock.stockId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(stockId);
    }
    
    @Override
    public String toString() {
        return "Stock{" +
                "stockId='" + stockId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", reservedQuantity=" + reservedQuantity +
                ", availableQuantity=" + getAvailableQuantity() +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                '}';
    }
}