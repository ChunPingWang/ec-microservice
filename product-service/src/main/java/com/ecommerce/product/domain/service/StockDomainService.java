package com.ecommerce.product.domain.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.product.domain.exception.InsufficientStockException;
import com.ecommerce.product.domain.exception.StockNotFoundException;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.domain.repository.StockRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Stock domain service following DDD principles
 * Encapsulates complex stock management business logic that doesn't belong to a single entity
 */
@DomainService
public class StockDomainService {
    
    private final StockRepository stockRepository;
    
    public StockDomainService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }
    
    /**
     * Reserve stock for a product with validation
     * @param productId the product ID
     * @param quantityToReserve the quantity to reserve
     * @return the updated stock
     * @throws StockNotFoundException if stock not found for product
     * @throws InsufficientStockException if insufficient stock available
     */
    public Stock reserveStock(String productId, Integer quantityToReserve) {
        Stock stock = stockRepository.findByProductId(productId)
            .orElseThrow(() -> StockNotFoundException.byProductId(productId));
        
        if (!stock.hasAvailableStock(quantityToReserve)) {
            throw InsufficientStockException.forReservation(
                productId, quantityToReserve, stock.getAvailableQuantity());
        }
        
        stock.reserveStock(quantityToReserve);
        return stockRepository.save(stock);
    }
    
    /**
     * Confirm a stock reservation (convert reservation to actual sale)
     * @param productId the product ID
     * @param quantityToConfirm the quantity to confirm
     * @return the updated stock
     * @throws StockNotFoundException if stock not found for product
     * @throws InsufficientStockException if insufficient reserved stock
     */
    public Stock confirmReservation(String productId, Integer quantityToConfirm) {
        Stock stock = stockRepository.findByProductId(productId)
            .orElseThrow(() -> StockNotFoundException.byProductId(productId));
        
        if (quantityToConfirm > stock.getReservedQuantity()) {
            throw new InsufficientStockException(
                String.format("Cannot confirm %d units for product %s. Only %d units are reserved.", 
                            quantityToConfirm, productId, stock.getReservedQuantity()));
        }
        
        stock.confirmReservation(quantityToConfirm);
        return stockRepository.save(stock);
    }
    
    /**
     * Release a stock reservation (cancel reservation)
     * @param productId the product ID
     * @param quantityToRelease the quantity to release
     * @return the updated stock
     * @throws StockNotFoundException if stock not found for product
     */
    public Stock releaseReservation(String productId, Integer quantityToRelease) {
        Stock stock = stockRepository.findByProductId(productId)
            .orElseThrow(() -> StockNotFoundException.byProductId(productId));
        
        stock.releaseReservation(quantityToRelease);
        return stockRepository.save(stock);
    }
    
    /**
     * Restock a product with validation
     * @param productId the product ID
     * @param quantityToAdd the quantity to add
     * @return the updated stock
     * @throws StockNotFoundException if stock not found for product
     */
    public Stock restockProduct(String productId, Integer quantityToAdd) {
        Stock stock = stockRepository.findByProductId(productId)
            .orElseThrow(() -> StockNotFoundException.byProductId(productId));
        
        stock.addStock(quantityToAdd);
        return stockRepository.save(stock);
    }
    
    /**
     * Perform direct stock reduction (for immediate sales without reservation)
     * @param productId the product ID
     * @param quantityToReduce the quantity to reduce
     * @return the updated stock
     * @throws StockNotFoundException if stock not found for product
     * @throws InsufficientStockException if insufficient stock available
     */
    public Stock reduceStock(String productId, Integer quantityToReduce) {
        Stock stock = stockRepository.findByProductId(productId)
            .orElseThrow(() -> StockNotFoundException.byProductId(productId));
        
        if (!stock.hasAvailableStock(quantityToReduce)) {
            throw InsufficientStockException.forSale(
                productId, quantityToReduce, stock.getAvailableQuantity());
        }
        
        stock.reduceStock(quantityToReduce);
        return stockRepository.save(stock);
    }
    
    /**
     * Check if a product has sufficient stock for a given quantity
     * @param productId the product ID
     * @param requiredQuantity the required quantity
     * @return true if sufficient stock is available, false otherwise
     */
    public boolean hasAvailableStock(String productId, Integer requiredQuantity) {
        return stockRepository.findByProductId(productId)
            .map(stock -> stock.hasAvailableStock(requiredQuantity))
            .orElse(false);
    }
    
    /**
     * Get available quantity for a product
     * @param productId the product ID
     * @return the available quantity, or 0 if stock not found
     */
    public Integer getAvailableQuantity(String productId) {
        return stockRepository.findByProductId(productId)
            .map(Stock::getAvailableQuantity)
            .orElse(0);
    }
    
    /**
     * Check if a product is out of stock
     * @param productId the product ID
     * @return true if out of stock, false otherwise
     */
    public boolean isOutOfStock(String productId) {
        return stockRepository.findByProductId(productId)
            .map(Stock::isOutOfStock)
            .orElse(true); // Consider as out of stock if no stock record exists
    }
    
    /**
     * Check if a product has low stock
     * @param productId the product ID
     * @return true if low stock, false otherwise
     */
    public boolean isLowStock(String productId) {
        return stockRepository.findByProductId(productId)
            .map(Stock::isLowStock)
            .orElse(false);
    }
    
    /**
     * Get all products that need restocking
     * @return list of stocks that are below minimum threshold
     */
    public List<Stock> getProductsNeedingRestock() {
        return stockRepository.findLowStockItems();
    }
    
    /**
     * Get all products that are out of stock
     * @return list of stocks that are out of stock
     */
    public List<Stock> getOutOfStockProducts() {
        return stockRepository.findOutOfStockItems();
    }
    
    /**
     * Get slow-moving stock (products with no sales in the specified period)
     * @param daysSinceLastSale number of days since last sale
     * @return list of slow-moving stocks
     */
    public List<Stock> getSlowMovingStock(int daysSinceLastSale) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceLastSale);
        return stockRepository.findSlowMovingStock(cutoffDate);
    }
    
    /**
     * Bulk reserve stock for multiple products
     * @param reservationRequests list of product IDs and quantities to reserve
     * @return list of updated stocks
     * @throws InsufficientStockException if any product has insufficient stock
     */
    public List<Stock> bulkReserveStock(List<StockReservationRequest> reservationRequests) {
        // First, validate all reservations are possible
        for (StockReservationRequest request : reservationRequests) {
            if (!hasAvailableStock(request.getProductId(), request.getQuantity())) {
                Integer available = getAvailableQuantity(request.getProductId());
                throw InsufficientStockException.forReservation(
                    request.getProductId(), request.getQuantity(), available);
            }
        }
        
        // If all validations pass, perform the reservations
        return reservationRequests.stream()
            .map(request -> reserveStock(request.getProductId(), request.getQuantity()))
            .toList();
    }
    
    /**
     * Inner class for bulk reservation requests
     */
    public static class StockReservationRequest {
        private final String productId;
        private final Integer quantity;
        
        public StockReservationRequest(String productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        public String getProductId() { return productId; }
        public Integer getQuantity() { return quantity; }
    }
}