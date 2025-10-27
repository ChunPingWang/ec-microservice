package com.ecommerce.product.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.product.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Data Transfer Object
 * Follows SRP principle by encapsulating product data for API communication
 */
public class ProductDto extends BaseDto {
    
    private String productId;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String brand;
    private String model;
    private ProductStatus status;
    private String specifications;
    private String imageUrl;
    private LocalDateTime launchDate;
    
    // Stock information (when included)
    private Integer availableQuantity;
    private boolean inStock;
    private boolean lowStock;
    
    // Constructors
    public ProductDto() {}
    
    public ProductDto(String productId, String name, String description, String category,
                     BigDecimal price, String brand, String model, ProductStatus status,
                     String specifications, String imageUrl, LocalDateTime launchDate) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.brand = brand;
        this.model = model;
        this.status = status;
        this.specifications = specifications;
        this.imageUrl = imageUrl;
        this.launchDate = launchDate;
    }
    
    // Business methods
    public String getFullName() {
        return brand + " " + name;
    }
    
    public boolean isAvailable() {
        return ProductStatus.AVAILABLE.equals(status) && inStock;
    }
    
    public boolean isOutOfStock() {
        return ProductStatus.OUT_OF_STOCK.equals(status) || !inStock;
    }
    
    public boolean isDiscontinued() {
        return ProductStatus.DISCONTINUED.equals(status);
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public LocalDateTime getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDateTime launchDate) { this.launchDate = launchDate; }
    
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    
    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }
    
    public boolean isLowStock() { return lowStock; }
    public void setLowStock(boolean lowStock) { this.lowStock = lowStock; }
    
    @Override
    public String toString() {
        return "ProductDto{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", availableQuantity=" + availableQuantity +
                ", inStock=" + inStock +
                '}';
    }
}