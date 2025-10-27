package com.ecommerce.product.infrastructure.config;

import com.ecommerce.product.application.port.in.ProductSearchUseCase;
import com.ecommerce.product.application.port.in.StockManagementUseCase;
import com.ecommerce.product.application.port.out.NotificationPort;
import com.ecommerce.product.application.port.out.ProductPersistencePort;
import com.ecommerce.product.application.port.out.StockPersistencePort;
import com.ecommerce.product.application.usecase.ProductSearchService;
import com.ecommerce.product.application.usecase.StockManagementService;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.product.domain.repository.StockRepository;
import com.ecommerce.product.domain.service.ProductDomainService;
import com.ecommerce.product.domain.service.StockDomainService;
import com.ecommerce.product.infrastructure.adapter.persistence.ProductJpaAdapter;
import com.ecommerce.product.infrastructure.adapter.persistence.StockJpaAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Product Service Configuration
 * Configures dependency injection for the Product Service
 * Follows DIP principle by wiring interfaces to implementations
 */
@Configuration
public class ProductConfig {
    
    /**
     * Configure ProductRepository using JPA adapter
     */
    @Bean
    public ProductRepository productRepository(ProductPersistencePort productPersistencePort) {
        // Create an adapter that implements ProductRepository using ProductPersistencePort
        return new ProductRepositoryAdapter(productPersistencePort);
    }
    
    /**
     * Configure StockRepository using JPA adapter
     */
    @Bean
    public StockRepository stockRepository(StockPersistencePort stockPersistencePort) {
        // Create an adapter that implements StockRepository using StockPersistencePort
        return new StockRepositoryAdapter(stockPersistencePort);
    }
    
    /**
     * Configure ProductDomainService
     */
    @Bean
    public ProductDomainService productDomainService(ProductRepository productRepository, 
                                                   StockRepository stockRepository) {
        return new ProductDomainService(productRepository, stockRepository);
    }
    
    /**
     * Configure StockDomainService
     */
    @Bean
    public StockDomainService stockDomainService(StockRepository stockRepository) {
        return new StockDomainService(stockRepository);
    }
    
    /**
     * Configure ProductSearchUseCase
     */
    @Bean
    public ProductSearchUseCase productSearchUseCase(ProductPersistencePort productPersistencePort,
                                                   StockPersistencePort stockPersistencePort,
                                                   ProductDomainService productDomainService) {
        return new ProductSearchService(productPersistencePort, stockPersistencePort, productDomainService);
    }
    
    /**
     * Configure StockManagementUseCase
     */
    @Bean
    public StockManagementUseCase stockManagementUseCase(StockPersistencePort stockPersistencePort,
                                                       ProductPersistencePort productPersistencePort,
                                                       StockDomainService stockDomainService,
                                                       NotificationPort notificationPort) {
        return new StockManagementService(stockPersistencePort, productPersistencePort, 
                                        stockDomainService, notificationPort);
    }
    
    /**
     * Adapter class to bridge ProductRepository domain interface with ProductPersistencePort
     */
    private static class ProductRepositoryAdapter implements ProductRepository {
        private final ProductPersistencePort persistencePort;
        
        public ProductRepositoryAdapter(ProductPersistencePort persistencePort) {
            this.persistencePort = persistencePort;
        }
        
        @Override
        public com.ecommerce.product.domain.model.Product save(com.ecommerce.product.domain.model.Product product) {
            return persistencePort.save(product);
        }
        
        @Override
        public java.util.Optional<com.ecommerce.product.domain.model.Product> findById(String productId) {
            return persistencePort.findById(productId);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByNameContaining(String name) {
            return persistencePort.searchByKeyword(name);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByCategory(String category) {
            return persistencePort.findByCategory(category);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByBrand(String brand) {
            return persistencePort.findByBrand(brand);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByStatus(com.ecommerce.product.domain.model.ProductStatus status) {
            return persistencePort.findByStatus(status);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByPriceBetween(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
            return persistencePort.findByPriceBetween(minPrice, maxPrice);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByBrandAndModel(String brand, String model) {
            return persistencePort.findByBrandAndModel(brand, model);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> searchByKeyword(String keyword) {
            return persistencePort.searchByKeyword(keyword);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByLaunchDateBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
            // This method is not available in persistence port, would need to be added
            return java.util.List.of();
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findAvailableProducts() {
            return persistencePort.findByStatus(com.ecommerce.product.domain.model.ProductStatus.AVAILABLE);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findOutOfStockProducts() {
            return persistencePort.findByStatus(com.ecommerce.product.domain.model.ProductStatus.OUT_OF_STOCK);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findDiscontinuedProducts() {
            return persistencePort.findByStatus(com.ecommerce.product.domain.model.ProductStatus.DISCONTINUED);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByCategoryAndStatus(String category, com.ecommerce.product.domain.model.ProductStatus status) {
            // This would need to be implemented in persistence port
            return java.util.List.of();
        }
        
        @Override
        public boolean existsByBrandAndModel(String brand, String model) {
            return persistencePort.existsByBrandAndModel(brand, model);
        }
        
        @Override
        public void deleteById(String productId) {
            persistencePort.deleteById(productId);
        }
        
        @Override
        public long count() {
            // This would need to be implemented in persistence port
            return 0;
        }
        
        @Override
        public long countByStatus(com.ecommerce.product.domain.model.ProductStatus status) {
            return persistencePort.countByStatus(status);
        }
        
        @Override
        public long countByCategory(String category) {
            return persistencePort.countByCategory(category);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findAll(int page, int size) {
            // This would need to be implemented in persistence port
            return java.util.List.of();
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> findByCategory(String category, int page, int size) {
            return persistencePort.findByCategory(category, page, size);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Product> searchByKeyword(String keyword, int page, int size) {
            return persistencePort.searchByKeyword(keyword, page, size);
        }
    }
    
    /**
     * Adapter class to bridge StockRepository domain interface with StockPersistencePort
     */
    private static class StockRepositoryAdapter implements StockRepository {
        private final StockPersistencePort persistencePort;
        
        public StockRepositoryAdapter(StockPersistencePort persistencePort) {
            this.persistencePort = persistencePort;
        }
        
        @Override
        public com.ecommerce.product.domain.model.Stock save(com.ecommerce.product.domain.model.Stock stock) {
            return persistencePort.save(stock);
        }
        
        @Override
        public java.util.Optional<com.ecommerce.product.domain.model.Stock> findById(String stockId) {
            return persistencePort.findById(stockId);
        }
        
        @Override
        public java.util.Optional<com.ecommerce.product.domain.model.Stock> findByProductId(String productId) {
            return persistencePort.findByProductId(productId);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findByWarehouseLocation(String warehouseLocation) {
            return persistencePort.findByWarehouseLocation(warehouseLocation);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findLowStockItems() {
            return persistencePort.findLowStockItems();
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findOutOfStockItems() {
            return persistencePort.findOutOfStockItems();
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findByAvailableQuantityGreaterThan(Integer minimumAvailable) {
            return persistencePort.findByAvailableQuantityGreaterThan(minimumAvailable);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findStocksWithReservations() {
            return persistencePort.findStocksWithReservations();
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findByQuantityBetween(Integer minQuantity, Integer maxQuantity) {
            return persistencePort.findByQuantityBetween(minQuantity, maxQuantity);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findStocksNeedingRestock(java.time.LocalDateTime lastRestockBefore) {
            return persistencePort.findStocksNeedingRestock(lastRestockBefore);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findSlowMovingStock(java.time.LocalDateTime lastSaleBefore) {
            return persistencePort.findSlowMovingStock(lastSaleBefore);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findStocksNearCapacity(Integer utilizationThreshold) {
            return persistencePort.findStocksNearCapacity(utilizationThreshold);
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findByProductIdIn(java.util.List<String> productIds) {
            return persistencePort.findByProductIdIn(productIds);
        }
        
        @Override
        public boolean existsByProductId(String productId) {
            return persistencePort.existsByProductId(productId);
        }
        
        @Override
        public void deleteById(String stockId) {
            persistencePort.deleteById(stockId);
        }
        
        @Override
        public void deleteByProductId(String productId) {
            persistencePort.deleteByProductId(productId);
        }
        
        @Override
        public long count() {
            // This would need to be implemented in persistence port
            return 0;
        }
        
        @Override
        public long countByWarehouseLocation(String warehouseLocation) {
            return persistencePort.countByWarehouseLocation(warehouseLocation);
        }
        
        @Override
        public long countLowStockItems() {
            return persistencePort.countLowStockItems();
        }
        
        @Override
        public long countOutOfStockItems() {
            return persistencePort.countOutOfStockItems();
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findAll(int page, int size) {
            // This would need to be implemented in persistence port
            return java.util.List.of();
        }
        
        @Override
        public java.util.List<com.ecommerce.product.domain.model.Stock> findByWarehouseLocation(String warehouseLocation, int page, int size) {
            // This would need to be implemented in persistence port
            return java.util.List.of();
        }
    }
}