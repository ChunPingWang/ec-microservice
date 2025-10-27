package com.ecommerce.product.application.dto;

import com.ecommerce.product.domain.model.ProductStatus;

import java.math.BigDecimal;

/**
 * Product search request DTO
 * Encapsulates search criteria for product queries
 */
public class ProductSearchRequest {
    
    private String keyword;
    private String category;
    private String brand;
    private ProductStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStockOnly;
    private String sortBy;
    private String sortDirection;
    private int page;
    private int size;
    
    // Constructors
    public ProductSearchRequest() {
        this.page = 0;
        this.size = 20;
        this.sortBy = "name";
        this.sortDirection = "ASC";
        this.inStockOnly = false;
    }
    
    public ProductSearchRequest(String keyword) {
        this();
        this.keyword = keyword;
    }
    
    public ProductSearchRequest(String keyword, String category, int page, int size) {
        this();
        this.keyword = keyword;
        this.category = category;
        this.page = page;
        this.size = size;
    }
    
    // Validation methods
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty();
    }
    
    public boolean hasBrand() {
        return brand != null && !brand.trim().isEmpty();
    }
    
    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }
    
    public boolean isValidPriceRange() {
        if (minPrice != null && maxPrice != null) {
            return minPrice.compareTo(maxPrice) <= 0;
        }
        return true;
    }
    
    public boolean isValidPageSize() {
        return size > 0 && size <= 100; // Max 100 items per page
    }
    
    public boolean isValidPage() {
        return page >= 0;
    }
    
    // Getters and Setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    
    public Boolean getInStockOnly() { return inStockOnly; }
    public void setInStockOnly(Boolean inStockOnly) { this.inStockOnly = inStockOnly; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    
    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    @Override
    public String toString() {
        return "ProductSearchRequest{" +
                "keyword='" + keyword + '\'' +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", status=" + status +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", inStockOnly=" + inStockOnly +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}