package com.ecommerce.product.infrastructure.adapter.persistence.mapper;

import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.infrastructure.adapter.persistence.entity.StockJpaEntity;

import java.util.List;

/**
 * Stock JPA Mapper
 * Follows SRP principle by handling only stock entity mapping between domain and persistence layers
 */
public class StockJpaMapper {
    
    /**
     * Convert Stock domain entity to StockJpaEntity
     * @param stock the domain stock entity
     * @return the JPA stock entity
     */
    public static StockJpaEntity toJpaEntity(Stock stock) {
        if (stock == null) {
            return null;
        }
        
        StockJpaEntity jpaEntity = new StockJpaEntity();
        jpaEntity.setStockId(stock.getStockId());
        jpaEntity.setProductId(stock.getProductId());
        jpaEntity.setQuantity(stock.getQuantity());
        jpaEntity.setReservedQuantity(stock.getReservedQuantity());
        jpaEntity.setMinimumThreshold(stock.getMinimumThreshold());
        jpaEntity.setMaximumCapacity(stock.getMaximumCapacity());
        jpaEntity.setWarehouseLocation(stock.getWarehouseLocation());
        jpaEntity.setLastRestockDate(stock.getLastRestockDate());
        jpaEntity.setLastSaleDate(stock.getLastSaleDate());
        jpaEntity.setCreatedAt(stock.getCreatedAt());
        jpaEntity.setUpdatedAt(stock.getUpdatedAt());
        
        return jpaEntity;
    }
    
    /**
     * Convert StockJpaEntity to Stock domain entity
     * @param jpaEntity the JPA stock entity
     * @return the domain stock entity
     */
    public static Stock toDomainEntity(StockJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        Stock stock = Stock.create(
            jpaEntity.getProductId(),
            jpaEntity.getQuantity(),
            jpaEntity.getMinimumThreshold(),
            jpaEntity.getWarehouseLocation()
        );
        
        // Set additional fields that are not set by the factory method
        setDomainEntityFields(stock, jpaEntity);
        
        return stock;
    }
    
    /**
     * Convert list of StockJpaEntity to list of Stock domain entities
     * @param jpaEntities the list of JPA stock entities
     * @return the list of domain stock entities
     */
    public static List<Stock> toDomainEntityList(List<StockJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return null;
        }
        
        return jpaEntities.stream()
                .map(StockJpaMapper::toDomainEntity)
                .toList();
    }
    
    /**
     * Convert list of Stock domain entities to list of StockJpaEntity
     * @param stocks the list of domain stock entities
     * @return the list of JPA stock entities
     */
    public static List<StockJpaEntity> toJpaEntityList(List<Stock> stocks) {
        if (stocks == null) {
            return null;
        }
        
        return stocks.stream()
                .map(StockJpaMapper::toJpaEntity)
                .toList();
    }
    
    /**
     * Update existing StockJpaEntity with data from Stock domain entity
     * @param jpaEntity the existing JPA entity to update
     * @param stock the domain entity with updated data
     */
    public static void updateJpaEntity(StockJpaEntity jpaEntity, Stock stock) {
        if (jpaEntity == null || stock == null) {
            return;
        }
        
        jpaEntity.setQuantity(stock.getQuantity());
        jpaEntity.setReservedQuantity(stock.getReservedQuantity());
        jpaEntity.setMinimumThreshold(stock.getMinimumThreshold());
        jpaEntity.setMaximumCapacity(stock.getMaximumCapacity());
        jpaEntity.setWarehouseLocation(stock.getWarehouseLocation());
        jpaEntity.setLastRestockDate(stock.getLastRestockDate());
        jpaEntity.setLastSaleDate(stock.getLastSaleDate());
        jpaEntity.setUpdatedAt(stock.getUpdatedAt());
    }
    
    /**
     * Update existing Stock domain entity with data from StockJpaEntity
     * @param stock the existing domain entity to update
     * @param jpaEntity the JPA entity with updated data
     */
    public static void updateDomainEntity(Stock stock, StockJpaEntity jpaEntity) {
        if (stock == null || jpaEntity == null) {
            return;
        }
        
        // Update thresholds if they have changed
        if (!stock.getMinimumThreshold().equals(jpaEntity.getMinimumThreshold()) ||
            !stock.getMaximumCapacity().equals(jpaEntity.getMaximumCapacity())) {
            stock.updateThresholds(jpaEntity.getMinimumThreshold(), jpaEntity.getMaximumCapacity());
        }
        
        // Update warehouse location if it has changed
        if (!stock.getWarehouseLocation().equals(jpaEntity.getWarehouseLocation())) {
            stock.relocateStock(jpaEntity.getWarehouseLocation());
        }
        
        // Note: Quantity and reserved quantity updates should be done through business methods
        // rather than direct updates to maintain business rules and invariants
    }
    
    /**
     * Private helper method to set domain entity fields
     * Since domain entities may have private setters, we need to handle this carefully
     */
    private static void setDomainEntityFields(Stock stock, StockJpaEntity jpaEntity) {
        // Note: In a real implementation, you might need to use reflection or 
        // provide package-private setters in the domain entity for persistence purposes
        // For now, we'll assume the factory method creates the entity correctly
        
        // Update maximum capacity if different from default
        if (!stock.getMaximumCapacity().equals(jpaEntity.getMaximumCapacity())) {
            stock.updateThresholds(jpaEntity.getMinimumThreshold(), jpaEntity.getMaximumCapacity());
        }
        
        // Handle reserved quantity - this would typically be set through business operations
        // but for persistence reconstruction, we might need to set it directly
        // This is a compromise between domain purity and persistence requirements
        if (jpaEntity.getReservedQuantity() != null && jpaEntity.getReservedQuantity() > 0) {
            // In a real implementation, you might need a special method for persistence reconstruction
            // For now, we'll assume the reserved quantity is handled correctly by the persistence layer
        }
    }
    
    /**
     * Create a new Stock domain entity from JPA entity for persistence reconstruction
     * This method is specifically for reconstructing domain entities from persistence
     * @param jpaEntity the JPA entity
     * @return the reconstructed domain entity
     */
    public static Stock reconstructDomainEntity(StockJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        // Create the stock entity
        Stock stock = Stock.create(
            jpaEntity.getProductId(),
            0, // Start with 0, will be updated
            jpaEntity.getMinimumThreshold(),
            jpaEntity.getWarehouseLocation()
        );
        
        // Update to actual values - in a real implementation, you might need
        // special reconstruction methods or reflection
        // For now, we'll use the available business methods
        
        if (jpaEntity.getQuantity() > 0) {
            stock.addStock(jpaEntity.getQuantity());
        }
        
        // Handle reserved quantity through business logic if possible
        if (jpaEntity.getReservedQuantity() > 0 && jpaEntity.getQuantity() >= jpaEntity.getReservedQuantity()) {
            try {
                stock.reserveStock(jpaEntity.getReservedQuantity());
            } catch (Exception e) {
                // If reservation fails, log it but continue with reconstruction
                // In a real implementation, you'd use proper logging
                System.err.println("Warning: Could not reconstruct reserved quantity for stock: " + jpaEntity.getStockId());
            }
        }
        
        return stock;
    }
}