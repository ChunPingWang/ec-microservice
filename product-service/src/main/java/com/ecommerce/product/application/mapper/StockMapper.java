package com.ecommerce.product.application.mapper;

import com.ecommerce.product.application.dto.StockDto;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.Stock;

import java.util.List;

/**
 * Stock mapper for converting between domain entities and DTOs
 * Follows SRP principle by handling only stock mapping operations
 */
public class StockMapper {
    
    /**
     * Convert Stock entity to StockDto
     * @param stock the stock entity
     * @return the stock DTO
     */
    public static StockDto toDto(Stock stock) {
        if (stock == null) {
            return null;
        }
        
        StockDto dto = new StockDto();
        dto.setStockId(stock.getStockId());
        dto.setProductId(stock.getProductId());
        dto.setQuantity(stock.getQuantity());
        dto.setReservedQuantity(stock.getReservedQuantity());
        dto.setAvailableQuantity(stock.getAvailableQuantity());
        dto.setMinimumThreshold(stock.getMinimumThreshold());
        dto.setMaximumCapacity(stock.getMaximumCapacity());
        dto.setWarehouseLocation(stock.getWarehouseLocation());
        dto.setLastRestockDate(stock.getLastRestockDate());
        dto.setLastSaleDate(stock.getLastSaleDate());
        dto.setLowStock(stock.isLowStock());
        dto.setOutOfStock(stock.isOutOfStock());
        dto.setUtilizationPercentage(stock.getStockUtilizationPercentage());
        dto.setCreatedAt(stock.getCreatedAt());
        dto.setUpdatedAt(stock.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * Convert Stock entity with Product information to StockDto
     * @param stock the stock entity
     * @param product the product entity (can be null)
     * @return the stock DTO with product information
     */
    public static StockDto toDto(Stock stock, Product product) {
        StockDto dto = toDto(stock);
        
        if (dto != null && product != null) {
            dto.setProductName(product.getName());
            dto.setProductBrand(product.getBrand());
        }
        
        return dto;
    }
    
    /**
     * Convert list of Stock entities to list of StockDtos
     * @param stocks the list of stock entities
     * @return the list of stock DTOs
     */
    public static List<StockDto> toDtoList(List<Stock> stocks) {
        if (stocks == null) {
            return null;
        }
        
        return stocks.stream()
                .map(StockMapper::toDto)
                .toList();
    }
    
    /**
     * Convert StockDto to Stock entity (for creation operations)
     * @param dto the stock DTO
     * @return the stock entity
     */
    public static Stock toEntity(StockDto dto) {
        if (dto == null) {
            return null;
        }
        
        return Stock.create(
            dto.getProductId(),
            dto.getQuantity(),
            dto.getMinimumThreshold(),
            dto.getWarehouseLocation()
        );
    }
    
    /**
     * Update existing Stock entity with data from StockDto
     * @param stock the existing stock entity
     * @param dto the stock DTO with updated data
     */
    public static void updateEntity(Stock stock, StockDto dto) {
        if (stock == null || dto == null) {
            return;
        }
        
        // Update thresholds if provided
        if (dto.getMinimumThreshold() != null && dto.getMaximumCapacity() != null) {
            stock.updateThresholds(dto.getMinimumThreshold(), dto.getMaximumCapacity());
        }
        
        // Update warehouse location if provided
        if (dto.getWarehouseLocation() != null && !dto.getWarehouseLocation().trim().isEmpty()) {
            stock.relocateStock(dto.getWarehouseLocation());
        }
    }
    
    /**
     * Create a minimal StockDto with basic information
     * @param stock the stock entity
     * @return the minimal stock DTO
     */
    public static StockDto toMinimalDto(Stock stock) {
        if (stock == null) {
            return null;
        }
        
        StockDto dto = new StockDto();
        dto.setStockId(stock.getStockId());
        dto.setProductId(stock.getProductId());
        dto.setQuantity(stock.getQuantity());
        dto.setAvailableQuantity(stock.getAvailableQuantity());
        dto.setOutOfStock(stock.isOutOfStock());
        dto.setLowStock(stock.isLowStock());
        
        return dto;
    }
    
    /**
     * Create a detailed StockDto with all information including product details
     * @param stock the stock entity
     * @param product the product entity
     * @return the detailed stock DTO
     */
    public static StockDto toDetailedDto(Stock stock, Product product) {
        StockDto dto = toDto(stock, product);
        
        // Add any additional detailed information if needed
        // This method can be extended for specific detailed view requirements
        
        return dto;
    }
    
    /**
     * Create StockDto for stock alert purposes
     * @param stock the stock entity
     * @param product the product entity
     * @return the stock DTO formatted for alerts
     */
    public static StockDto toAlertDto(Stock stock, Product product) {
        StockDto dto = toMinimalDto(stock);
        
        if (dto != null && product != null) {
            dto.setProductName(product.getName());
            dto.setProductBrand(product.getBrand());
        }
        
        return dto;
    }
}