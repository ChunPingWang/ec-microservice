package com.ecommerce.product.infrastructure.adapter.persistence.entity;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.product.domain.model.ProductStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product JPA Entity
 * Follows SRP principle by handling only product data persistence
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_brand_model", columnList = "brand, model"),
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_status", columnList = "status"),
    @Index(name = "idx_product_name", columnList = "name")
})
public class ProductJpaEntity extends BaseEntity {
    
    @Id
    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "brand", nullable = false, length = 50)
    private String brand;
    
    @Column(name = "model", nullable = false, length = 50)
    private String model;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;
    
    @Column(name = "specifications", nullable = false, length = 2000)
    private String specifications;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "launch_date")
    private LocalDateTime launchDate;
    
    // Constructors
    public ProductJpaEntity() {}
    
    public ProductJpaEntity(String productId, String name, String description, String category,
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
    
    @Override
    public String toString() {
        return "ProductJpaEntity{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", price=" + price +
                ", status=" + status +
                '}';
    }
}