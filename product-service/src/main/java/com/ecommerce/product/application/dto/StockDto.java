package com.ecommerce.product.application.dto;

import com.ecommerce.common.dto.BaseDto;

import java.time.LocalDateTime;

/**
 * Stock Data Transfer Object
 * Follows SRP principle by encapsulating stock data for API communication
 */
public class StockDto extends BaseDto {
    
    private String stockId;
    private String productId;
    private String productName;
    private String productBrand;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer minimumThreshold;
    private Integer maximumCapacity;
    private String warehouseLocation;
    private LocalDateTime lastRestockDate;
    private LocalDateTime lastSaleDate;
    private boolean lowStock;
    private boolean outOfStock;
    private Integer utilizationPercentage;
    
    // Constructors
    public StockDto() {}
    
    public StockDto(String stockId, String productId, Integer quantity, Integer reservedQuantity,
                   Integer minimumThreshold, Integer maximumCapacity, String warehouseLocation) {
        this.stockId = stockId;
        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = quantity - reservedQuantity;
        this.minimumThreshold = minimumThreshold;
        this.maximumCapacity = maximumCapacity;
        this.warehouseLocation = warehouseLocation;
        this.lowStock = quantity <= minimumThreshold;
        this.outOfStock = quantity <= 0;
        this.utilizationPercentage = maximumCapacity > 0 ? (quantity * 100) / maximumCapacity : 0;
    }
    
    // Business methods
    public boolean hasAvailableStock(Integer requestedQuantity) {
        return availableQuantity >= requestedQuantity;
    }
    
    public boolean canReserve(Integer requestedQuantity) {
        return hasAvailableStock(requestedQuantity);
    }
    
    public boolean isAtCapacity() {
        return quantity >= maximumCapacity;
    }
    
    public boolean hasReservedStock() {
        return reservedQuantity > 0;
    }
    
    public String getStockStatus() {
        if (outOfStock) return "Out of Stock";
        if (lowStock) return "Low Stock";
        if (isAtCapacity()) return "At Capacity";
        return "In Stock";
    }
    
    // Getters and Setters
    public String getStockId() { return stockId; }
    public void setStockId(String stockId) { this.stockId = stockId; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductBrand() { return productBrand; }
    public void setProductBrand(String productBrand) { this.productBrand = productBrand; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        updateCalculatedFields();
    }
    
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { 
        this.reservedQuantity = reservedQuantity;
        updateCalculatedFields();
    }
    
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    
    public Integer getMinimumThreshold() { return minimumThreshold; }
    public void setMinimumThreshold(Integer minimumThreshold) { 
        this.minimumThreshold = minimumThreshold;
        updateCalculatedFields();
    }
    
    public Integer getMaximumCapacity() { return maximumCapacity; }
    public void setMaximumCapacity(Integer maximumCapacity) { 
        this.maximumCapacity = maximumCapacity;
        updateCalculatedFields();
    }
    
    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }
    
    public LocalDateTime getLastRestockDate() { return lastRestockDate; }
    public void setLastRestockDate(LocalDateTime lastRestockDate) { this.lastRestockDate = lastRestockDate; }
    
    public LocalDateTime getLastSaleDate() { return lastSaleDate; }
    public void setLastSaleDate(LocalDateTime lastSaleDate) { this.lastSaleDate = lastSaleDate; }
    
    public boolean isLowStock() { return lowStock; }
    public void setLowStock(boolean lowStock) { this.lowStock = lowStock; }
    
    public boolean isOutOfStock() { return outOfStock; }
    public void setOutOfStock(boolean outOfStock) { this.outOfStock = outOfStock; }
    
    public Integer getUtilizationPercentage() { return utilizationPercentage; }
    public void setUtilizationPercentage(Integer utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }
    
    // Helper method to update calculated fields
    private void updateCalculatedFields() {
        if (quantity != null && reservedQuantity != null) {
            this.availableQuantity = quantity - reservedQuantity;
            this.outOfStock = quantity <= 0;
        }
        if (quantity != null && minimumThreshold != null) {
            this.lowStock = quantity <= minimumThreshold;
        }
        if (quantity != null && maximumCapacity != null && maximumCapacity > 0) {
            this.utilizationPercentage = (quantity * 100) / maximumCapacity;
        }
    }
    
    @Override
    public String toString() {
        return "StockDto{" +
                "stockId='" + stockId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", reservedQuantity=" + reservedQuantity +
                ", availableQuantity=" + availableQuantity +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                ", lowStock=" + lowStock +
                ", outOfStock=" + outOfStock +
                '}';
    }
}