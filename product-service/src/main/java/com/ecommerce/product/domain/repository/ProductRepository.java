package com.ecommerce.product.domain.repository;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Product repository interface following DDD repository pattern
 * Defines the contract for product data access operations
 */
public interface ProductRepository {
    
    /**
     * Save a product entity
     * @param product the product to save
     * @return the saved product
     */
    Product save(Product product);
    
    /**
     * Find product by ID
     * @param productId the product ID
     * @return optional product
     */
    Optional<Product> findById(String productId);
    
    /**
     * Find products by name (case-insensitive partial match)
     * @param name the product name or partial name
     * @return list of matching products
     */
    List<Product> findByNameContaining(String name);
    
    /**
     * Find products by category
     * @param category the product category
     * @return list of products in the specified category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find products by brand
     * @param brand the product brand
     * @return list of products from the specified brand
     */
    List<Product> findByBrand(String brand);
    
    /**
     * Find products by status
     * @param status the product status
     * @return list of products with the specified status
     */
    List<Product> findByStatus(ProductStatus status);
    
    /**
     * Find products by price range
     * @param minPrice the minimum price (inclusive)
     * @param maxPrice the maximum price (inclusive)
     * @return list of products within the price range
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find products by brand and model
     * @param brand the product brand
     * @param model the product model
     * @return list of products matching brand and model
     */
    List<Product> findByBrandAndModel(String brand, String model);
    
    /**
     * Search products by keyword (searches name, description, brand, model)
     * @param keyword the search keyword
     * @return list of products matching the keyword
     */
    List<Product> searchByKeyword(String keyword);
    
    /**
     * Find products launched within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of products launched within the date range
     */
    List<Product> findByLaunchDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find available products (status = AVAILABLE)
     * @return list of available products
     */
    List<Product> findAvailableProducts();
    
    /**
     * Find out of stock products (status = OUT_OF_STOCK)
     * @return list of out of stock products
     */
    List<Product> findOutOfStockProducts();
    
    /**
     * Find discontinued products (status = DISCONTINUED)
     * @return list of discontinued products
     */
    List<Product> findDiscontinuedProducts();
    
    /**
     * Find products by category and status
     * @param category the product category
     * @param status the product status
     * @return list of products matching category and status
     */
    List<Product> findByCategoryAndStatus(String category, ProductStatus status);
    
    /**
     * Check if a product exists by brand and model
     * @param brand the product brand
     * @param model the product model
     * @return true if product exists, false otherwise
     */
    boolean existsByBrandAndModel(String brand, String model);
    
    /**
     * Delete a product by ID
     * @param productId the product ID
     */
    void deleteById(String productId);
    
    /**
     * Count total number of products
     * @return total product count
     */
    long count();
    
    /**
     * Count products by status
     * @param status the product status
     * @return count of products with the specified status
     */
    long countByStatus(ProductStatus status);
    
    /**
     * Count products by category
     * @param category the product category
     * @return count of products in the specified category
     */
    long countByCategory(String category);
    
    /**
     * Find all products with pagination
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of products for the specified page
     */
    List<Product> findAll(int page, int size);
    
    /**
     * Find products by category with pagination
     * @param category the product category
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of products for the specified category and page
     */
    List<Product> findByCategory(String category, int page, int size);
    
    /**
     * Search products with pagination
     * @param keyword the search keyword
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of products matching the keyword for the specified page
     */
    List<Product> searchByKeyword(String keyword, int page, int size);
}