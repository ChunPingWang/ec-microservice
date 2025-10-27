package com.ecommerce.product.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.common.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Product domain entity following DDD principles
 * Encapsulates product business rules and validation logic
 */
public class Product extends BaseEntity {
    
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
    
    // Private constructor for JPA
    protected Product() {}
    
    // Factory method for creating new products
    public static Product create(String name, String description, String category, 
                               BigDecimal price, String brand, String model, String specifications) {
        Product product = new Product();
        product.productId = generateProductId();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(price);
        product.setBrand(brand);
        product.setModel(model);
        product.setSpecifications(specifications);
        product.status = ProductStatus.AVAILABLE;
        product.launchDate = LocalDateTime.now();
        return product;
    }
    
    // Factory method for iPhone 17 Pro
    public static Product createIPhone17Pro() {
        return create(
            "iPhone 17 Pro",
            "Apple iPhone 17 Pro with advanced A18 Pro chip, ProRAW camera system, and titanium design",
            "Smartphones",
            new BigDecimal("39900.00"),
            "Apple",
            "iPhone 17 Pro",
            "Display: 6.3-inch Super Retina XDR OLED, 2556×1179 pixels, 460 ppi; " +
            "Chip: A18 Pro chip with 6-core CPU, 6-core GPU, 16-core Neural Engine; " +
            "Camera: Pro camera system with 48MP Main, 12MP Ultra Wide, 12MP Telephoto; " +
            "Storage: 128GB, 256GB, 512GB, 1TB; " +
            "Battery: Up to 23 hours video playback; " +
            "Material: Titanium with textured matte glass back; " +
            "Colors: Natural Titanium, Blue Titanium, White Titanium, Black Titanium; " +
            "Operating System: iOS 18"
        );
    }
    
    // Business methods
    public void updateProductInfo(String name, String description, BigDecimal price) {
        setName(name);
        setDescription(description);
        setPrice(price);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateSpecifications(String specifications) {
        setSpecifications(specifications);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (!isValidUrl(imageUrl)) {
                throw new ValidationException("Invalid image URL format");
            }
        }
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void makeAvailable() {
        this.status = ProductStatus.AVAILABLE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markOutOfStock() {
        this.status = ProductStatus.OUT_OF_STOCK;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isAvailable() {
        return ProductStatus.AVAILABLE.equals(this.status);
    }
    
    public boolean isOutOfStock() {
        return ProductStatus.OUT_OF_STOCK.equals(this.status);
    }
    
    public boolean isDiscontinued() {
        return ProductStatus.DISCONTINUED.equals(this.status);
    }
    
    public String getFullName() {
        return brand + " " + name;
    }
    
    // Private helper methods
    private static String generateProductId() {
        return "PROD-" + System.currentTimeMillis();
    }
    
    private boolean isValidUrl(String url) {
        return url.matches("^https?://.*\\.(jpg|jpeg|png|gif|webp)$");
    }
    
    // Validation methods
    private void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Product name is required");
        }
        if (name.length() > 100) {
            throw new ValidationException("Product name cannot exceed 100 characters");
        }
        this.name = name.trim();
    }
    
    private void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Product description is required");
        }
        if (description.length() > 1000) {
            throw new ValidationException("Product description cannot exceed 1000 characters");
        }
        this.description = description.trim();
    }
    
    private void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Product category is required");
        }
        if (category.length() > 50) {
            throw new ValidationException("Product category cannot exceed 50 characters");
        }
        this.category = category.trim();
    }
    
    private void setPrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("Product price is required");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Product price must be greater than zero");
        }
        if (price.scale() > 2) {
            throw new ValidationException("Product price cannot have more than 2 decimal places");
        }
        this.price = price;
    }
    
    private void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new ValidationException("Product brand is required");
        }
        if (brand.length() > 50) {
            throw new ValidationException("Product brand cannot exceed 50 characters");
        }
        this.brand = brand.trim();
    }
    
    private void setModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new ValidationException("Product model is required");
        }
        if (model.length() > 50) {
            throw new ValidationException("Product model cannot exceed 50 characters");
        }
        this.model = model.trim();
    }
    
    private void setSpecifications(String specifications) {
        if (specifications == null || specifications.trim().isEmpty()) {
            throw new ValidationException("Product specifications are required");
        }
        if (specifications.length() > 2000) {
            throw new ValidationException("Product specifications cannot exceed 2000 characters");
        }
        this.specifications = specifications.trim();
    }
    
    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public ProductStatus getStatus() { return status; }
    public String getSpecifications() { return specifications; }
    public String getImageUrl() { return imageUrl; }
    public LocalDateTime getLaunchDate() { return launchDate; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", price=" + price +
                ", status=" + status +
                '}';
    }
}