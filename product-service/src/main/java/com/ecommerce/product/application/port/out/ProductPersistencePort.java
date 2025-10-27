package com.ecommerce.product.application.port.out;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Output port for product persistence operations
 * Follows DIP principle by defining abstraction for data access
 */
public interface ProductPersistencePort {
    
    /**
     * Save a product
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
     * Search products by keyword
     * @param keyword the search keyword
     * @return list of matching products
     */
    List<Product> searchByKeyword(String keyword);
    
    /**
     * Search products by keyword with pagination
     * @param keyword the search keyword
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of matching products for the page
     */
    List<Product> searchByKeyword(String keyword, int page, int size);
    
    /**
     * Find products by category
     * @param category the product category
     * @return list of products in the category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find products by category with pagination
     * @param category the product category
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of products in the category for the page
     */
    List<Product> findByCategory(String category, int page, int size);
    
    /**
     * Find products by brand
     * @param brand the product brand
     * @return list of products from the brand
     */
    List<Product> findByBrand(String brand);
    
    /**
     * Find products by status
     * @param status the product status
     * @return list of products with the status
     */
    List<Product> findByStatus(ProductStatus status);
    
    /**
     * Find products by status with pagination
     * @param status the product status
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of products with the status for the page
     */
    List<Product> findByStatus(ProductStatus status, int page, int size);
    
    /**
     * Find products by price range
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
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
     * Check if product exists by brand and model
     * @param brand the product brand
     * @param model the product model
     * @return true if product exists
     */
    boolean existsByBrandAndModel(String brand, String model);
    
    /**
     * Delete product by ID
     * @param productId the product ID
     */
    void deleteById(String productId);
    
    /**
     * Count products by category
     * @param category the product category
     * @return count of products in the category
     */
    long countByCategory(String category);
    
    /**
     * Count products by status
     * @param status the product status
     * @return count of products with the status
     */
    long countByStatus(ProductStatus status);
    
    /**
     * Count search results
     * @param keyword the search keyword
     * @return count of products matching the keyword
     */
    long countSearchResults(String keyword);
}