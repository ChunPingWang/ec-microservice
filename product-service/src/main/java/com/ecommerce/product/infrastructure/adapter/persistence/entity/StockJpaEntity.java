package com.ecommerce.product.infrastructure.adapter.persistence.entity;

import com.ecommerce.common.architecture.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Stock JPA Entity
 * Follows SRP principle by handling only stock data persistence
 */
@Entity
@Table(name = "stocks", indexes = {
    @Index(name = "idx_stock_product_id", columnList = "product_id", unique = true),
    @Index(name = "idx_stock_warehouse", columnList = "warehouse_location"),
    @Index(name = "idx_stock_quantity", columnList = "quantity"),
    @Index(name = "idx_stock_low_stock", columnList = "quantity, minimum_threshold")
})
public class StockJpaEntity extends BaseEntity {
    
    @Id
    @Column(name = "stock_id", nullable = false, length = 50)
    private String stockId;
    
    @Column(name = "product_id", nullable = false, length = 50, unique = true)
    private String productId;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;
    
    @Column(name = "minimum_threshold", nullable = false)
    private Integer minimumThreshold;
    
    @Column(name = "maximum_capacity", nullable = false)
    private Integer maximumCapacity;
    
    @Column(name = "warehouse_location", nullable = false, length = 100)
    private String warehouseLocation;
    
    @Column(name = "last_restock_date")
    private LocalDateTime lastRestockDate;
    
    @Column(name = "last_sale_date")
    private LocalDateTime lastSaleDate;
    
    // Constructors
    public StockJpaEntity() {}
    
    public StockJpaEntity(String stockId, String productId, Integer quantity, Integer reservedQuantity,
                         Integer minimumThreshold, Integer maximumCapacity, String warehouseLocation,
                         LocalDateTime lastRestockDate, LocalDateTime lastSaleDate) {
        this.stockId = stockId;
        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.minimumThreshold = minimumThreshold;
        this.maximumCapacity = maximumCapacity;
        this.warehouseLocation = warehouseLocation;
        this.lastRestockDate = lastRestockDate;
        this.lastSaleDate = lastSaleDate;
    }
    
    // Business methods for calculated fields
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
    
    public boolean isLowStock() {
        return quantity <= minimumThreshold;
    }
    
    public boolean isOutOfStock() {
        return quantity <= 0;
    }
    
    public Integer getUtilizationPercentage() {
        if (maximumCapacity == 0) return 0;
        return (quantity * 100) / maximumCapacity;
    }
    
    // Getters and Setters
    public String getStockId() { return stockId; }
    public void setStockId(String stockId) { this.stockId = stockId; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    
    public Integer getMinimumThreshold() { return minimumThreshold; }
    public void setMinimumThreshold(Integer minimumThreshold) { this.minimumThreshold = minimumThreshold; }
    
    public Integer getMaximumCapacity() { return maximumCapacity; }
    public void setMaximumCapacity(Integer maximumCapacity) { this.maximumCapacity = maximumCapacity; }
    
    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }
    
    public LocalDateTime getLastRestockDate() { return lastRestockDate; }
    public void setLastRestockDate(LocalDateTime lastRestockDate) { this.lastRestockDate = lastRestockDate; }
    
    public LocalDateTime getLastSaleDate() { return lastSaleDate; }
    public void setLastSaleDate(LocalDateTime lastSaleDate) { this.lastSaleDate = lastSaleDate; }
    
    @Override
    public String toString() {
        return "StockJpaEntity{" +
                "stockId='" + stockId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", reservedQuantity=" + reservedQuantity +
                ", availableQuantity=" + getAvailableQuantity() +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                ", lowStock=" + isLowStock() +
                ", outOfStock=" + isOutOfStock() +
                '}';
    }
}