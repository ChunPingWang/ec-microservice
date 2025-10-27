package com.ecommerce.product.domain.event;

import com.ecommerce.common.architecture.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event fired when a new product is created
 * Follows the DDD event-driven architecture pattern
 */
public class ProductCreatedEvent extends DomainEvent {
    
    private final String productId;
    private final String name;
    private final String brand;
    private final String model;
    private final String category;
    private final BigDecimal price;
    private final LocalDateTime createdAt;
    
    public ProductCreatedEvent(String productId, String name, String brand, String model, 
                             String category, BigDecimal price, LocalDateTime createdAt) {
        super("ProductCreated");
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.price = price;
        this.createdAt = createdAt;
    }
    
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    @Override
    public String toString() {
        return "ProductCreatedEvent{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", createdAt=" + createdAt +
                ", eventId=" + getEventId() +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}