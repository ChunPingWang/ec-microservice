package com.ecommerce.product.domain.repository;

import com.ecommerce.product.domain.model.Stock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Stock repository interface following DDD repository pattern
 * Defines the contract for stock data access operations
 */
public interface StockRepository {
    
    /**
     * Save a stock entity
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
     * @return list of stocks in the specified warehouse
     */
    List<Stock> findByWarehouseLocation(String warehouseLocation);
    
    /**
     * Find stocks with low inventory (quantity <= minimum threshold)
     * @return list of stocks with low inventory
     */
    List<Stock> findLowStockItems();
    
    /**
     * Find stocks that are out of stock (quantity <= 0)
     * @return list of out of stock items
     */
    List<Stock> findOutOfStockItems();
    
    /**
     * Find stocks with available quantity greater than specified amount
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
     * @param minQuantity the minimum quantity (inclusive)
     * @param maxQuantity the maximum quantity (inclusive)
     * @return list of stocks within the quantity range
     */
    List<Stock> findByQuantityBetween(Integer minQuantity, Integer maxQuantity);
    
    /**
     * Find stocks that haven't been restocked since the specified date
     * @param lastRestockBefore the cutoff date for last restock
     * @return list of stocks that need restocking
     */
    List<Stock> findStocksNeedingRestock(LocalDateTime lastRestockBefore);
    
    /**
     * Find stocks that haven't had sales since the specified date
     * @param lastSaleBefore the cutoff date for last sale
     * @return list of stocks with no recent sales
     */
    List<Stock> findSlowMovingStock(LocalDateTime lastSaleBefore);
    
    /**
     * Find stocks at or near capacity (utilization >= threshold percentage)
     * @param utilizationThreshold the utilization threshold percentage (0-100)
     * @return list of stocks at or near capacity
     */
    List<Stock> findStocksNearCapacity(Integer utilizationThreshold);
    
    /**
     * Find all stocks for multiple products
     * @param productIds the list of product IDs
     * @return list of stocks for the specified products
     */
    List<Stock> findByProductIdIn(List<String> productIds);
    
    /**
     * Check if stock exists for a product
     * @param productId the product ID
     * @return true if stock exists, false otherwise
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
     * Count total number of stock records
     * @return total stock record count
     */
    long count();
    
    /**
     * Count stocks by warehouse location
     * @param warehouseLocation the warehouse location
     * @return count of stocks in the specified warehouse
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
    
    /**
     * Calculate total inventory value for all stocks
     * This method should be implemented to join with product data for pricing
     * @return total inventory value across all warehouses
     */
    // Note: This might require a custom implementation that joins with Product data
    
    /**
     * Find all stocks with pagination
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of stocks for the specified page
     */
    List<Stock> findAll(int page, int size);
    
    /**
     * Find stocks by warehouse with pagination
     * @param warehouseLocation the warehouse location
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of stocks for the specified warehouse and page
     */
    List<Stock> findByWarehouseLocation(String warehouseLocation, int page, int size);
}