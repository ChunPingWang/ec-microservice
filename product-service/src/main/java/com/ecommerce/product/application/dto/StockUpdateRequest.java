package com.ecommerce.product.application.dto;

/**
 * Stock update request DTO
 * Encapsulates stock update operations and metadata
 */
public class StockUpdateRequest {
    
    private String productId;
    private Integer quantityChange;
    private StockUpdateType updateType;
    private String reason;
    private String warehouseLocation;
    private Integer minimumThreshold;
    private Integer maximumCapacity;
    
    // Constructors
    public StockUpdateRequest() {}
    
    public StockUpdateRequest(String productId, Integer quantityChange, StockUpdateType updateType) {
        this.productId = productId;
        this.quantityChange = quantityChange;
        this.updateType = updateType;
    }
    
    public StockUpdateRequest(String productId, Integer quantityChange, StockUpdateType updateType, String reason) {
        this.productId = productId;
        this.quantityChange = quantityChange;
        this.updateType = updateType;
        this.reason = reason;
    }
    
    // Validation methods
    public boolean isValid() {
        return productId != null && !productId.trim().isEmpty() && 
               updateType != null;
    }
    
    public boolean isQuantityUpdate() {
        return quantityChange != null && quantityChange != 0;
    }
    
    public boolean isThresholdUpdate() {
        return minimumThreshold != null || maximumCapacity != null;
    }
    
    public boolean isLocationUpdate() {
        return warehouseLocation != null && !warehouseLocation.trim().isEmpty();
    }
    
    public boolean isIncrease() {
        return quantityChange != null && quantityChange > 0;
    }
    
    public boolean isDecrease() {
        return quantityChange != null && quantityChange < 0;
    }
    
    // Stock update types
    public enum StockUpdateType {
        RESTOCK,
        SALE,
        ADJUSTMENT,
        TRANSFER,
        RESERVATION,
        RESERVATION_RELEASE,
        RESERVATION_CONFIRMATION,
        THRESHOLD_UPDATE,
        LOCATION_UPDATE
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public Integer getQuantityChange() { return quantityChange; }
    public void setQuantityChange(Integer quantityChange) { this.quantityChange = quantityChange; }
    
    public StockUpdateType getUpdateType() { return updateType; }
    public void setUpdateType(StockUpdateType updateType) { this.updateType = updateType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }
    
    public Integer getMinimumThreshold() { return minimumThreshold; }
    public void setMinimumThreshold(Integer minimumThreshold) { this.minimumThreshold = minimumThreshold; }
    
    public Integer getMaximumCapacity() { return maximumCapacity; }
    public void setMaximumCapacity(Integer maximumCapacity) { this.maximumCapacity = maximumCapacity; }
    
    @Override
    public String toString() {
        return "StockUpdateRequest{" +
                "productId='" + productId + '\'' +
                ", quantityChange=" + quantityChange +
                ", updateType=" + updateType +
                ", reason='" + reason + '\'' +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                ", minimumThreshold=" + minimumThreshold +
                ", maximumCapacity=" + maximumCapacity +
                '}';
    }
}