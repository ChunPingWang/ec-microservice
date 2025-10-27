package com.ecommerce.product.application.usecase;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.product.application.dto.StockDto;
import com.ecommerce.product.application.dto.StockReservationRequest;
import com.ecommerce.product.application.dto.StockUpdateRequest;
import com.ecommerce.product.application.mapper.StockMapper;
import com.ecommerce.product.application.port.in.StockManagementUseCase;
import com.ecommerce.product.application.port.out.NotificationPort;
import com.ecommerce.product.application.port.out.ProductPersistencePort;
import com.ecommerce.product.application.port.out.StockPersistencePort;
import com.ecommerce.product.domain.exception.StockNotFoundException;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.domain.service.StockDomainService;

import java.util.List;
import java.util.Optional;

/**
 * Stock management use case implementation
 * Follows SRP principle by handling only stock management operations
 */
@UseCase
public class StockManagementService implements StockManagementUseCase {
    
    private final StockPersistencePort stockPersistencePort;
    private final ProductPersistencePort productPersistencePort;
    private final StockDomainService stockDomainService;
    private final NotificationPort notificationPort;
    
    public StockManagementService(StockPersistencePort stockPersistencePort,
                                ProductPersistencePort productPersistencePort,
                                StockDomainService stockDomainService,
                                NotificationPort notificationPort) {
        this.stockPersistencePort = stockPersistencePort;
        this.productPersistencePort = productPersistencePort;
        this.stockDomainService = stockDomainService;
        this.notificationPort = notificationPort;
    }
    
    @Override
    public StockDto reserveStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Stock updatedStock = stockDomainService.reserveStock(productId, quantity);
        
        return enrichStockWithProductInfo(updatedStock);
    }
    
    @Override
    public StockDto confirmReservation(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Stock updatedStock = stockDomainService.confirmReservation(productId, quantity);
        
        // Check if product is now out of stock and send notification
        if (updatedStock.isOutOfStock()) {
            sendOutOfStockNotification(productId, updatedStock);
        }
        
        return enrichStockWithProductInfo(updatedStock);
    }
    
    @Override
    public StockDto releaseReservation(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Stock updatedStock = stockDomainService.releaseReservation(productId, quantity);
        
        return enrichStockWithProductInfo(updatedStock);
    }
    
    @Override
    public StockDto addStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        // Check if product was out of stock before restocking
        boolean wasOutOfStock = stockDomainService.isOutOfStock(productId);
        
        Stock updatedStock = stockDomainService.restockProduct(productId, quantity);
        
        // Send restock notification if product was out of stock
        if (wasOutOfStock && !updatedStock.isOutOfStock()) {
            sendRestockNotification(productId, updatedStock);
        }
        
        return enrichStockWithProductInfo(updatedStock);
    }
    
    @Override
    public StockDto reduceStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Stock updatedStock = stockDomainService.reduceStock(productId, quantity);
        
        // Check if product is now out of stock or low stock
        if (updatedStock.isOutOfStock()) {
            sendOutOfStockNotification(productId, updatedStock);
        } else if (updatedStock.isLowStock()) {
            sendLowStockAlert(productId, updatedStock);
        }
        
        return enrichStockWithProductInfo(updatedStock);
    }
    
    @Override
    public StockDto getStockByProductId(String productId) {
        validateProductId(productId);
        
        Stock stock = stockPersistencePort.findByProductId(productId)
            .orElseThrow(() -> StockNotFoundException.byProductId(productId));
        
        return enrichStockWithProductInfo(stock);
    }
    
    @Override
    public boolean hasAvailableStock(String productId, Integer requiredQuantity) {
        validateProductId(productId);
        validateQuantity(requiredQuantity);
        
        return stockDomainService.hasAvailableStock(productId, requiredQuantity);
    }
    
    @Override
    public Integer getAvailableQuantity(String productId) {
        validateProductId(productId);
        
        return stockDomainService.getAvailableQuantity(productId);
    }
    
    @Override
    public List<StockDto> getLowStockProducts() {
        List<Stock> lowStockItems = stockDomainService.getProductsNeedingRestock();
        
        return lowStockItems.stream()
            .map(this::enrichStockWithProductInfo)
            .toList();
    }
    
    @Override
    public List<StockDto> getOutOfStockProducts() {
        List<Stock> outOfStockItems = stockDomainService.getOutOfStockProducts();
        
        return outOfStockItems.stream()
            .map(this::enrichStockWithProductInfo)
            .toList();
    }
    
    @Override
    public StockDto updateStockThresholds(String productId, Integer minimumThreshold, Integer maximumCapacity) {
        validateProductId(productId);
        
        Stock stock = stockPersistencePort.findByProductId(productId)
            .orElseThrow(() -> StockNotFoundException.byProductId(productId));
        
        stock.updateThresholds(minimumThreshold, maximumCapacity);
        Stock updatedStock = stockPersistencePort.save(stock);
        
        return enrichStockWithProductInfo(updatedStock);
    }
    
    @Override
    public List<StockDto> bulkReserveStock(List<StockReservationRequest> reservationRequests) {
        validateReservationRequests(reservationRequests);
        
        // Convert to domain service request format
        List<StockDomainService.StockReservationRequest> domainRequests = reservationRequests.stream()
            .map(req -> new StockDomainService.StockReservationRequest(req.getProductId(), req.getQuantity()))
            .toList();
        
        List<Stock> updatedStocks = stockDomainService.bulkReserveStock(domainRequests);
        
        return updatedStocks.stream()
            .map(this::enrichStockWithProductInfo)
            .toList();
    }
    
    @Override
    public StockDto updateStock(StockUpdateRequest stockUpdateRequest) {
        validateStockUpdateRequest(stockUpdateRequest);
        
        Stock stock = stockPersistencePort.findByProductId(stockUpdateRequest.getProductId())
            .orElseThrow(() -> StockNotFoundException.byProductId(stockUpdateRequest.getProductId()));
        
        Stock updatedStock;
        
        switch (stockUpdateRequest.getUpdateType()) {
            case RESTOCK -> {
                if (stockUpdateRequest.isIncrease()) {
                    updatedStock = stockDomainService.restockProduct(
                        stockUpdateRequest.getProductId(), 
                        stockUpdateRequest.getQuantityChange()
                    );
                } else {
                    throw new IllegalArgumentException("Restock operation must have positive quantity change");
                }
            }
            case SALE -> {
                if (stockUpdateRequest.isDecrease()) {
                    updatedStock = stockDomainService.reduceStock(
                        stockUpdateRequest.getProductId(), 
                        Math.abs(stockUpdateRequest.getQuantityChange())
                    );
                } else {
                    throw new IllegalArgumentException("Sale operation must have negative quantity change");
                }
            }
            case THRESHOLD_UPDATE -> {
                stock.updateThresholds(
                    stockUpdateRequest.getMinimumThreshold(), 
                    stockUpdateRequest.getMaximumCapacity()
                );
                updatedStock = stockPersistencePort.save(stock);
            }
            case LOCATION_UPDATE -> {
                stock.relocateStock(stockUpdateRequest.getWarehouseLocation());
                updatedStock = stockPersistencePort.save(stock);
            }
            default -> throw new IllegalArgumentException("Unsupported update type: " + stockUpdateRequest.getUpdateType());
        }
        
        return enrichStockWithProductInfo(updatedStock);
    }
    
    @Override
    public List<StockDto> getStockByWarehouse(String warehouseLocation) {
        if (warehouseLocation == null || warehouseLocation.trim().isEmpty()) {
            return List.of();
        }
        
        List<Stock> stocks = stockPersistencePort.findByWarehouseLocation(warehouseLocation.trim());
        
        return stocks.stream()
            .map(this::enrichStockWithProductInfo)
            .toList();
    }
    
    // Private helper methods
    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
    }
    
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    private void validateReservationRequests(List<StockReservationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Reservation requests cannot be null or empty");
        }
        
        for (StockReservationRequest request : requests) {
            if (!request.isValid()) {
                throw new IllegalArgumentException("Invalid reservation request: " + request);
            }
        }
    }
    
    private void validateStockUpdateRequest(StockUpdateRequest request) {
        if (request == null || !request.isValid()) {
            throw new IllegalArgumentException("Invalid stock update request");
        }
    }
    
    private StockDto enrichStockWithProductInfo(Stock stock) {
        Optional<Product> product = productPersistencePort.findById(stock.getProductId());
        return StockMapper.toDto(stock, product.orElse(null));
    }
    
    private void sendOutOfStockNotification(String productId, Stock stock) {
        try {
            Optional<Product> productOpt = productPersistencePort.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                notificationPort.sendOutOfStockNotification(productId, product.getFullName());
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
            // In a real implementation, you might want to use a proper logging framework
            System.err.println("Failed to send out of stock notification for product: " + productId + ", error: " + e.getMessage());
        }
    }
    
    private void sendRestockNotification(String productId, Stock stock) {
        try {
            Optional<Product> productOpt = productPersistencePort.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                notificationPort.sendRestockNotification(productId, product.getFullName(), stock.getAvailableQuantity());
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to send restock notification for product: " + productId + ", error: " + e.getMessage());
        }
    }
    
    private void sendLowStockAlert(String productId, Stock stock) {
        try {
            Optional<Product> productOpt = productPersistencePort.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                notificationPort.sendLowStockAlert(
                    productId, 
                    product.getFullName(), 
                    stock.getQuantity(), 
                    stock.getMinimumThreshold()
                );
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to send low stock alert for product: " + productId + ", error: " + e.getMessage());
        }
    }
}