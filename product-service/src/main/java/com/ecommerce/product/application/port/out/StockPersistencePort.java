package com.ecommerce.product.application.port.out;

import com.ecommerce.product.domain.model.Stock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output port for stock persistence operations
 * Follows DIP principle by defining abstraction for stock data access
 */
public interface StockPersistencePort {
    
    /**
     * Save a stock record
     * @param stock the stock to save
     * @return the saved stock
     */
    Stock save(Stock stock);
    
    /**
     * Find stock by ID
     * @param stockId the stock ID
     * @return optional stock
     */
    Optional<Stock> findById(String stockId);
    
    /**
     * Find stock by product ID
     * @param productId the product ID
     * @return optional stock for the product
     */
    Optional<Stock> findByProductId(String productId);
    
    /**
     * Find stocks by warehouse location
     * @param warehouseLocation the warehouse location
     * @return list of stocks in the warehouse
     */
    List<Stock> findByWarehouseLocation(String warehouseLocation);
    
    /**
     * Find stocks with low inventory
     * @return list of stocks below minimum threshold
     */
    List<Stock> findLowStockItems();
    
    /**
     * Find out of stock items
     * @return list of stocks with zero or negative quantity
     */
    List<Stock> findOutOfStockItems();
    
    /**
     * Find stocks with available quantity greater than specified
     * @param minimumAvailable the minimum available quantity
     * @return list of stocks with sufficient availability
     */
    List<Stock> findByAvailableQuantityGreaterThan(Integer minimumAvailable);
    
    /**
     * Find stocks with reserved inventory
     * @return list of stocks that have reserved quantities
     */
    List<Stock> findStocksWithReservations();
    
    /**
     * Find stocks by quantity range
     * @param minQuantity the minimum quantity
     * @param maxQuantity the maximum quantity
     * @return list of stocks within the quantity range
     */
    List<Stock> findByQuantityBetween(Integer minQuantity, Integer maxQuantity);
    
    /**
     * Find stocks needing restock
     * @param lastRestockBefore the cutoff date for last restock
     * @return list of stocks that haven't been restocked recently
     */
    List<Stock> findStocksNeedingRestock(LocalDateTime lastRestockBefore);
    
    /**
     * Find slow-moving stock
     * @param lastSaleBefore the cutoff date for last sale
     * @return list of stocks with no recent sales
     */
    List<Stock> findSlowMovingStock(LocalDateTime lastSaleBefore);
    
    /**
     * Find stocks near capacity
     * @param utilizationThreshold the utilization threshold percentage
     * @return list of stocks at or near capacity
     */
    List<Stock> findStocksNearCapacity(Integer utilizationThreshold);
    
    /**
     * Find stocks for multiple products
     * @param productIds the list of product IDs
     * @return list of stocks for the specified products
     */
    List<Stock> findByProductIdIn(List<String> productIds);
    
    /**
     * Check if stock exists for a product
     * @param productId the product ID
     * @return true if stock exists
     */
    boolean existsByProductId(String productId);
    
    /**
     * Delete stock by ID
     * @param stockId the stock ID
     */
    void deleteById(String stockId);
    
    /**
     * Delete stock by product ID
     * @param productId the product ID
     */
    void deleteByProductId(String productId);
    
    /**
     * Count stocks by warehouse
     * @param warehouseLocation the warehouse location
     * @return count of stocks in the warehouse
     */
    long countByWarehouseLocation(String warehouseLocation);
    
    /**
     * Count low stock items
     * @return count of items with low stock
     */
    long countLowStockItems();
    
    /**
     * Count out of stock items
     * @return count of out of stock items
     */
    long countOutOfStockItems();
}