package com.ecommerce.product.application.mapper;

import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.domain.service.ProductDomainService.ProductWithStock;

import java.util.List;

/**
 * Product mapper for converting between domain entities and DTOs
 * Follows SRP principle by handling only product mapping operations
 */
public class ProductMapper {
    
    /**
     * Convert Product entity to ProductDto
     * @param product the product entity
     * @return the product DTO
     */
    public static ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setBrand(product.getBrand());
        dto.setModel(product.getModel());
        dto.setStatus(product.getStatus());
        dto.setSpecifications(product.getSpecifications());
        dto.setImageUrl(product.getImageUrl());
        dto.setLaunchDate(product.getLaunchDate());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Default stock information (will be overridden if stock is provided)
        dto.setInStock(false);
        dto.setLowStock(false);
        dto.setAvailableQuantity(0);
        
        return dto;
    }
    
    /**
     * Convert Product entity with Stock to ProductDto
     * @param product the product entity
     * @param stock the stock entity (can be null)
     * @return the product DTO with stock information
     */
    public static ProductDto toDto(Product product, Stock stock) {
        ProductDto dto = toDto(product);
        
        if (dto != null && stock != null) {
            dto.setAvailableQuantity(stock.getAvailableQuantity());
            dto.setInStock(!stock.isOutOfStock());
            dto.setLowStock(stock.isLowStock());
        }
        
        return dto;
    }
    
    /**
     * Convert ProductWithStock to ProductDto
     * @param productWithStock the product with stock information
     * @return the product DTO
     */
    public static ProductDto toDto(ProductWithStock productWithStock) {
        if (productWithStock == null) {
            return null;
        }
        
        return toDto(productWithStock.getProduct(), productWithStock.getStock());
    }
    
    /**
     * Convert list of Product entities to list of ProductDtos
     * @param products the list of product entities
     * @return the list of product DTOs
     */
    public static List<ProductDto> toDtoList(List<Product> products) {
        if (products == null) {
            return null;
        }
        
        return products.stream()
                .map(ProductMapper::toDto)
                .toList();
    }
    
    /**
     * Convert list of ProductWithStock to list of ProductDtos
     * @param productsWithStock the list of products with stock information
     * @return the list of product DTOs
     */
    public static List<ProductDto> toDtoListFromProductWithStock(List<ProductWithStock> productsWithStock) {
        if (productsWithStock == null) {
            return null;
        }
        
        return productsWithStock.stream()
                .map(ProductMapper::toDto)
                .toList();
    }
    
    /**
     * Convert ProductDto to Product entity (for creation/update operations)
     * @param dto the product DTO
     * @return the product entity
     */
    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        
        return Product.create(
            dto.getName(),
            dto.getDescription(),
            dto.getCategory(),
            dto.getPrice(),
            dto.getBrand(),
            dto.getModel(),
            dto.getSpecifications()
        );
    }
    
    /**
     * Update existing Product entity with data from ProductDto
     * @param product the existing product entity
     * @param dto the product DTO with updated data
     */
    public static void updateEntity(Product product, ProductDto dto) {
        if (product == null || dto == null) {
            return;
        }
        
        product.updateProductInfo(dto.getName(), dto.getDescription(), dto.getPrice());
        product.updateSpecifications(dto.getSpecifications());
        
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
    }
    
    /**
     * Create a minimal ProductDto with basic information
     * @param product the product entity
     * @return the minimal product DTO
     */
    public static ProductDto toMinimalDto(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setModel(product.getModel());
        dto.setPrice(product.getPrice());
        dto.setStatus(product.getStatus());
        dto.setImageUrl(product.getImageUrl());
        
        return dto;
    }
    
    /**
     * Create a detailed ProductDto with all information
     * @param product the product entity
     * @param stock the stock entity
     * @return the detailed product DTO
     */
    public static ProductDto toDetailedDto(Product product, Stock stock) {
        ProductDto dto = toDto(product, stock);
        
        // Add any additional detailed information if needed
        // This method can be extended for specific detailed view requirements
        
        return dto;
    }
}