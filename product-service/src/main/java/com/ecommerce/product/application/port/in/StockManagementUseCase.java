package com.ecommerce.product.application.port.in;

import com.ecommerce.product.application.dto.StockDto;
import com.ecommerce.product.application.dto.StockReservationRequest;
import com.ecommerce.product.application.dto.StockUpdateRequest;

import java.util.List;

/**
 * Input port for stock management operations
 * Follows ISP principle by defining specific stock-related operations
 */
public interface StockManagementUseCase {
    
    /**
     * Reserve stock for a product
     * @param productId the product ID
     * @param quantity the quantity to reserve
     * @return the updated stock information
     */
    StockDto reserveStock(String productId, Integer quantity);
    
    /**
     * Confirm a stock reservation (convert to sale)
     * @param productId the product ID
     * @param quantity the quantity to confirm
     * @return the updated stock information
     */
    StockDto confirmReservation(String productId, Integer quantity);
    
    /**
     * Release a stock reservation (cancel reservation)
     * @param productId the product ID
     * @param quantity the quantity to release
     * @return the updated stock information
     */
    StockDto releaseReservation(String productId, Integer quantity);
    
    /**
     * Add stock to a product (restock)
     * @param productId the product ID
     * @param quantity the quantity to add
     * @return the updated stock information
     */
    StockDto addStock(String productId, Integer quantity);
    
    /**
     * Reduce stock for a product (direct sale)
     * @param productId the product ID
     * @param quantity the quantity to reduce
     * @return the updated stock information
     */
    StockDto reduceStock(String productId, Integer quantity);
    
    /**
     * Get stock information for a product
     * @param productId the product ID
     * @return the stock information
     */
    StockDto getStockByProductId(String productId);
    
    /**
     * Check if product has sufficient stock
     * @param productId the product ID
     * @param requiredQuantity the required quantity
     * @return true if sufficient stock is available
     */
    boolean hasAvailableStock(String productId, Integer requiredQuantity);
    
    /**
     * Get available quantity for a product
     * @param productId the product ID
     * @return the available quantity
     */
    Integer getAvailableQuantity(String productId);
    
    /**
     * Get products with low stock
     * @return list of products needing restock
     */
    List<StockDto> getLowStockProducts();
    
    /**
     * Get out of stock products
     * @return list of out of stock products
     */
    List<StockDto> getOutOfStockProducts();
    
    /**
     * Update stock thresholds
     * @param productId the product ID
     * @param minimumThreshold the minimum stock threshold
     * @param maximumCapacity the maximum stock capacity
     * @return the updated stock information
     */
    StockDto updateStockThresholds(String productId, Integer minimumThreshold, Integer maximumCapacity);
    
    /**
     * Bulk reserve stock for multiple products
     * @param reservationRequests list of reservation requests
     * @return list of updated stock information
     */
    List<StockDto> bulkReserveStock(List<StockReservationRequest> reservationRequests);
    
    /**
     * Update stock information
     * @param stockUpdateRequest the stock update request
     * @return the updated stock information
     */
    StockDto updateStock(StockUpdateRequest stockUpdateRequest);
    
    /**
     * Get stock by warehouse location
     * @param warehouseLocation the warehouse location
     * @return list of stocks in the warehouse
     */
    List<StockDto> getStockByWarehouse(String warehouseLocation);
}