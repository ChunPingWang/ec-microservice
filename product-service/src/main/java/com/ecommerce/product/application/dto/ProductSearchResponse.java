package com.ecommerce.product.application.dto;

import java.util.List;

/**
 * Product search response DTO
 * Encapsulates paginated search results with metadata
 */
public class ProductSearchResponse {
    
    private List<ProductDto> products;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private String sortBy;
    private String sortDirection;
    
    // Constructors
    public ProductSearchResponse() {}
    
    public ProductSearchResponse(List<ProductDto> products, int page, int size, long totalElements) {
        this.products = products;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }
    
    public ProductSearchResponse(List<ProductDto> products, int page, int size, long totalElements,
                               String sortBy, String sortDirection) {
        this(products, page, size, totalElements);
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }
    
    // Business methods
    public boolean isEmpty() {
        return products == null || products.isEmpty();
    }
    
    public int getNumberOfElements() {
        return products != null ? products.size() : 0;
    }
    
    public boolean isFirst() {
        return page == 0;
    }
    
    public boolean isLast() {
        return page >= totalPages - 1;
    }
    
    // Getters and Setters
    public List<ProductDto> getProducts() { return products; }
    public void setProducts(List<ProductDto> products) { this.products = products; }
    
    public int getPage() { return page; }
    public void setPage(int page) { 
        this.page = page;
        updatePaginationFlags();
    }
    
    public int getSize() { return size; }
    public void setSize(int size) { 
        this.size = size;
        updatePaginationFlags();
    }
    
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { 
        this.totalElements = totalElements;
        updatePaginationFlags();
    }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
    
    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    
    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
    
    // Helper method to update pagination flags
    private void updatePaginationFlags() {
        if (size > 0) {
            this.totalPages = (int) Math.ceil((double) totalElements / size);
            this.hasNext = page < totalPages - 1;
            this.hasPrevious = page > 0;
        }
    }
    
    @Override
    public String toString() {
        return "ProductSearchResponse{" +
                "productsCount=" + getNumberOfElements() +
                ", page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}