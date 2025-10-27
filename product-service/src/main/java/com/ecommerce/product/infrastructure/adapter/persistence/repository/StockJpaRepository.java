package com.ecommerce.product.infrastructure.adapter.persistence.repository;

import com.ecommerce.product.infrastructure.adapter.persistence.entity.StockJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Stock JPA Repository
 * Follows ISP principle by defining specific data access operations for stock
 */
@Repository
public interface StockJpaRepository extends JpaRepository<StockJpaEntity, String> {
    
    /**
     * Find stock by stock ID
     */
    Optional<StockJpaEntity> findByStockId(String stockId);
    
    /**
     * Find stock by product ID
     */
    Optional<StockJpaEntity> findByProductId(String productId);
    
    /**
     * Find stocks by warehouse location
     */
    List<StockJpaEntity> findByWarehouseLocation(String warehouseLocation);
    
    /**
     * Find stocks by warehouse location with pagination
     */
    Page<StockJpaEntity> findByWarehouseLocation(String warehouseLocation, Pageable pageable);
    
    /**
     * Find stocks with low inventory (quantity <= minimum threshold)
     */
    @Query("SELECT s FROM StockJpaEntity s WHERE s.quantity <= s.minimumThreshold")
    List<StockJpaEntity> findLowStockItems();
    
    /**
     * Find stocks that are out of stock (quantity <= 0)
     */
    @Query("SELECT s FROM StockJpaEntity s WHERE s.quantity <= 0")
    List<StockJpaEntity> findOutOfStockItems();
    
    /**
     * Find stocks with available quantity greater than specified amount
     */
    @Query("SELECT s FROM StockJpaEntity s WHERE (s.quantity - s.reservedQuantity) > :minimumAvailable")
    List<StockJpaEntity> findByAvailableQuantityGreaterThan(@Param("minimumAvailable") Integer minimumAvailable);
    
    /**
     * Find stocks with reserved inventory
     */
    @Query("SELECT s FROM StockJpaEntity s WHERE s.reservedQuantity > 0")
    List<StockJpaEntity> findStocksWithReservations();
    
    /**
     * Find stocks by quantity range
     */
    List<StockJpaEntity> findByQuantityBetween(Integer minQuantity, Integer maxQuantity);
    
    /**
     * Find stocks that haven't been restocked since specified date
     */
    @Query("SELECT s FROM StockJpaEntity s WHERE s.lastRestockDate < :lastRestockBefore OR s.lastRestockDate IS NULL")
    List<StockJpaEntity> findStocksNeedingRestock(@Param("lastRestockBefore") LocalDateTime lastRestockBefore);
    
    /**
     * Find slow-moving stock (no sales since specified date)
     */
    @Query("SELECT s FROM StockJpaEntity s WHERE s.lastSaleDate < :lastSaleBefore OR s.lastSaleDate IS NULL")
    List<StockJpaEntity> findSlowMovingStock(@Param("lastSaleBefore") LocalDateTime lastSaleBefore);
    
    /**
     * Find stocks near capacity (utilization >= threshold percentage)
     */
    @Query("SELECT s FROM StockJpaEntity s WHERE (s.quantity * 100 / s.maximumCapacity) >= :utilizationThreshold")
    List<StockJpaEntity> findStocksNearCapacity(@Param("utilizationThreshold") Integer utilizationThreshold);
    
    /**
     * Find stocks for multiple products
     */
    List<StockJpaEntity> findByProductIdIn(List<String> productIds);
    
    /**
     * Check if stock exists for a product
     */
    boolean existsByProductId(String productId);
    
    /**
     * Delete stock by product ID
     */
    void deleteByProductId(String productId);
    
    /**
     * Count stocks by warehouse location
     */
    long countByWarehouseLocation(String warehouseLocation);
    
    /**
     * Count low stock items
     */
    @Query("SELECT COUNT(s) FROM StockJpaEntity s WHERE s.quantity <= s.minimumThreshold")
    long countLowStockItems();
    
    /**
     * Count out of stock items
     */
    @Query("SELECT COUNT(s) FROM StockJpaEntity s WHERE s.quantity <= 0")
    long countOutOfStockItems();
    
    /**
     * Find stocks with quantity greater than specified amount
     */
    List<StockJpaEntity> findByQuantityGreaterThan(Integer quantity);
    
    /**
     * Find stocks with quantity less than specified amount
     */
    List<StockJpaEntity> findByQuantityLessThan(Integer quantity);
    
    /**
     * Find stocks with reserved quantity greater than specified amount
     */
    List<StockJpaEntity> findByReservedQuantityGreaterThan(Integer reservedQuantity);
    
    /**
     * Find stocks by minimum threshold range
     */
    List<StockJpaEntity> findByMinimumThresholdBetween(Integer minThreshold, Integer maxThreshold);
    
    /**
     * Find stocks by maximum capacity range
     */
    List<StockJpaEntity> findByMaximumCapacityBetween(Integer minCapacity, Integer maxCapacity);
    
    /**
     * Find stocks restocked after specified date
     */
    List<StockJpaEntity> findByLastRestockDateAfter(LocalDateTime date);
    
    /**
     * Find stocks with sales after specified date
     */
    List<StockJpaEntity> findByLastSaleDateAfter(LocalDateTime date);
    
    /**
     * Find stocks restocked between dates
     */
    List<StockJpaEntity> findByLastRestockDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find stocks with sales between dates
     */
    List<StockJpaEntity> findByLastSaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Calculate total inventory quantity across all warehouses
     */
    @Query("SELECT SUM(s.quantity) FROM StockJpaEntity s")
    Long calculateTotalInventoryQuantity();
    
    /**
     * Calculate total available quantity across all warehouses
     */
    @Query("SELECT SUM(s.quantity - s.reservedQuantity) FROM StockJpaEntity s")
    Long calculateTotalAvailableQuantity();
    
    /**
     * Calculate total reserved quantity across all warehouses
     */
    @Query("SELECT SUM(s.reservedQuantity) FROM StockJpaEntity s")
    Long calculateTotalReservedQuantity();
}