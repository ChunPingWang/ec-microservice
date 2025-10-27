package com.ecommerce.product.domain.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.product.domain.exception.ProductAlreadyExistsException;
import com.ecommerce.product.domain.exception.ProductNotFoundException;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.product.domain.repository.StockRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product domain service following DDD principles
 * Encapsulates complex product management business logic that spans multiple entities
 */
@DomainService
public class ProductDomainService {
    
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    
    public ProductDomainService(ProductRepository productRepository, StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }
    
    /**
     * Create a new product with initial stock
     * @param product the product to create
     * @param initialStock the initial stock quantity
     * @param minimumThreshold the minimum stock threshold
     * @param warehouseLocation the warehouse location
     * @return the created product
     * @throws ProductAlreadyExistsException if product already exists
     */
    public Product createProductWithStock(Product product, Integer initialStock, 
                                        Integer minimumThreshold, String warehouseLocation) {
        // Check if product already exists
        if (productRepository.existsByBrandAndModel(product.getBrand(), product.getModel())) {
            throw new ProductAlreadyExistsException(product.getBrand(), product.getModel());
        }
        
        // Save the product first
        Product savedProduct = productRepository.save(product);
        
        // Create initial stock record
        Stock stock = Stock.create(savedProduct.getProductId(), initialStock, 
                                 minimumThreshold, warehouseLocation);
        stockRepository.save(stock);
        
        return savedProduct;
    }
    
    /**
     * Update product status based on stock availability
     * @param productId the product ID
     * @return the updated product
     * @throws ProductNotFoundException if product not found
     */
    public Product updateProductStatusByStock(String productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        Optional<Stock> stockOpt = stockRepository.findByProductId(productId);
        
        if (stockOpt.isEmpty()) {
            // No stock record means out of stock
            product.markOutOfStock();
        } else {
            Stock stock = stockOpt.get();
            if (stock.isOutOfStock()) {
                product.markOutOfStock();
            } else {
                product.makeAvailable();
            }
        }
        
        return productRepository.save(product);
    }
    
    /**
     * Search products with stock information
     * @param keyword the search keyword
     * @return list of products with their stock status
     */
    public List<ProductWithStock> searchProductsWithStock(String keyword) {
        List<Product> products = productRepository.searchByKeyword(keyword);
        
        return products.stream()
            .map(this::enrichProductWithStock)
            .toList();
    }
    
    /**
     * Get products by category with stock information
     * @param category the product category
     * @return list of products with their stock status
     */
    public List<ProductWithStock> getProductsByCategoryWithStock(String category) {
        List<Product> products = productRepository.findByCategory(category);
        
        return products.stream()
            .map(this::enrichProductWithStock)
            .toList();
    }
    
    /**
     * Get available products (in stock and status = AVAILABLE)
     * @return list of available products with stock information
     */
    public List<ProductWithStock> getAvailableProductsWithStock() {
        List<Product> availableProducts = productRepository.findByStatus(ProductStatus.AVAILABLE);
        
        return availableProducts.stream()
            .map(this::enrichProductWithStock)
            .filter(pws -> pws.getStock() != null && !pws.getStock().isOutOfStock())
            .toList();
    }
    
    /**
     * Discontinue a product and handle its stock
     * @param productId the product ID
     * @return the discontinued product
     * @throws ProductNotFoundException if product not found
     */
    public Product discontinueProduct(String productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        // Discontinue the product
        product.discontinue();
        Product savedProduct = productRepository.save(product);
        
        // Handle remaining stock - in a real scenario, you might want to:
        // 1. Transfer stock to another warehouse
        // 2. Mark for clearance sale
        // 3. Return to supplier
        // For now, we'll just leave the stock as is for potential clearance
        
        return savedProduct;
    }
    
    /**
     * Get products that need attention (low stock, out of stock, etc.)
     * @return list of products needing attention
     */
    public List<ProductWithStock> getProductsNeedingAttention() {
        List<Stock> problematicStocks = stockRepository.findLowStockItems();
        problematicStocks.addAll(stockRepository.findOutOfStockItems());
        
        return problematicStocks.stream()
            .map(stock -> {
                Optional<Product> productOpt = productRepository.findById(stock.getProductId());
                return productOpt.map(product -> new ProductWithStock(product, stock))
                                .orElse(null);
            })
            .filter(pws -> pws != null)
            .toList();
    }
    
    /**
     * Update product pricing with business rules
     * @param productId the product ID
     * @param newPrice the new price
     * @return the updated product
     * @throws ProductNotFoundException if product not found
     */
    public Product updateProductPrice(String productId, BigDecimal newPrice) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        // Business rule: Cannot increase price by more than 20% at once
        BigDecimal currentPrice = product.getPrice();
        BigDecimal maxAllowedPrice = currentPrice.multiply(new BigDecimal("1.20"));
        
        if (newPrice.compareTo(maxAllowedPrice) > 0) {
            throw new IllegalArgumentException(
                String.format("Price increase cannot exceed 20%%. Current: %s, Requested: %s, Max allowed: %s",
                            currentPrice, newPrice, maxAllowedPrice));
        }
        
        product.updateProductInfo(product.getName(), product.getDescription(), newPrice);
        return productRepository.save(product);
    }
    
    /**
     * Check if a product can be ordered (available and in stock)
     * @param productId the product ID
     * @param requestedQuantity the requested quantity
     * @return true if product can be ordered, false otherwise
     */
    public boolean canOrderProduct(String productId, Integer requestedQuantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty() || !productOpt.get().isAvailable()) {
            return false;
        }
        
        Optional<Stock> stockOpt = stockRepository.findByProductId(productId);
        return stockOpt.map(stock -> stock.hasAvailableStock(requestedQuantity))
                      .orElse(false);
    }
    
    /**
     * Helper method to enrich product with stock information
     */
    private ProductWithStock enrichProductWithStock(Product product) {
        Optional<Stock> stockOpt = stockRepository.findByProductId(product.getProductId());
        return new ProductWithStock(product, stockOpt.orElse(null));
    }
    
    /**
     * Value object combining Product and Stock information
     */
    public static class ProductWithStock {
        private final Product product;
        private final Stock stock;
        
        public ProductWithStock(Product product, Stock stock) {
            this.product = product;
            this.stock = stock;
        }
        
        public Product getProduct() { return product; }
        public Stock getStock() { return stock; }
        
        public boolean isInStock() {
            return stock != null && !stock.isOutOfStock();
        }
        
        public boolean isLowStock() {
            return stock != null && stock.isLowStock();
        }
        
        public Integer getAvailableQuantity() {
            return stock != null ? stock.getAvailableQuantity() : 0;
        }
        
        public boolean canFulfillOrder(Integer requestedQuantity) {
            return product.isAvailable() && isInStock() && 
                   stock.hasAvailableStock(requestedQuantity);
        }
    }
}