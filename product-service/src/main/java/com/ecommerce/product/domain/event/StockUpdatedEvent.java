package com.ecommerce.product.domain.event;

import com.ecommerce.common.architecture.DomainEvent;

import java.time.LocalDateTime;

/**
 * Domain event fired when stock levels are updated
 * Follows the DDD event-driven architecture pattern
 */
public class StockUpdatedEvent extends DomainEvent {
    
    private final String productId;
    private final String stockId;
    private final Integer previousQuantity;
    private final Integer newQuantity;
    private final Integer availableQuantity;
    private final StockUpdateType updateType;
    private final String reason;
    private final LocalDateTime updatedAt;
    
    public StockUpdatedEvent(String productId, String stockId, Integer previousQuantity, 
                           Integer newQuantity, Integer availableQuantity, 
                           StockUpdateType updateType, String reason, LocalDateTime updatedAt) {
        super("StockUpdated");
        this.productId = productId;
        this.stockId = stockId;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.availableQuantity = availableQuantity;
        this.updateType = updateType;
        this.reason = reason;
        this.updatedAt = updatedAt;
    }
    
    public String getProductId() { return productId; }
    public String getStockId() { return stockId; }
    public Integer getPreviousQuantity() { return previousQuantity; }
    public Integer getNewQuantity() { return newQuantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public StockUpdateType getUpdateType() { return updateType; }
    public String getReason() { return reason; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public boolean isStockIncrease() {
        return newQuantity > previousQuantity;
    }
    
    public boolean isStockDecrease() {
        return newQuantity < previousQuantity;
    }
    
    public Integer getQuantityChange() {
        return newQuantity - previousQuantity;
    }
    
    public enum StockUpdateType {
        RESTOCK,
        SALE,
        RESERVATION,
        RESERVATION_RELEASE,
        RESERVATION_CONFIRMATION,
        ADJUSTMENT,
        TRANSFER
    }
    
    @Override
    public String toString() {
        return "StockUpdatedEvent{" +
                "productId='" + productId + '\'' +
                ", stockId='" + stockId + '\'' +
                ", previousQuantity=" + previousQuantity +
                ", newQuantity=" + newQuantity +
                ", availableQuantity=" + availableQuantity +
                ", updateType=" + updateType +
                ", reason='" + reason + '\'' +
                ", updatedAt=" + updatedAt +
                ", eventId=" + getEventId() +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}