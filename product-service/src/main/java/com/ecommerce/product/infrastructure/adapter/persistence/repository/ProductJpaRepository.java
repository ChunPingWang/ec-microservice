package com.ecommerce.product.infrastructure.adapter.persistence.repository;

import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.infrastructure.adapter.persistence.entity.ProductJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Product JPA Repository
 * Follows ISP principle by defining specific data access operations for products
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, String> {
    
    /**
     * Find product by product ID
     */
    Optional<ProductJpaEntity> findByProductId(String productId);
    
    /**
     * Find products by name containing keyword (case-insensitive)
     */
    List<ProductJpaEntity> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find products by name containing keyword with pagination
     */
    Page<ProductJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find products by category
     */
    List<ProductJpaEntity> findByCategory(String category);
    
    /**
     * Find products by category with pagination
     */
    Page<ProductJpaEntity> findByCategory(String category, Pageable pageable);
    
    /**
     * Find products by brand
     */
    List<ProductJpaEntity> findByBrand(String brand);
    
    /**
     * Find products by status
     */
    List<ProductJpaEntity> findByStatus(ProductStatus status);
    
    /**
     * Find products by status with pagination
     */
    Page<ProductJpaEntity> findByStatus(ProductStatus status, Pageable pageable);
    
    /**
     * Find products by price range
     */
    List<ProductJpaEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find products by brand and model
     */
    List<ProductJpaEntity> findByBrandAndModel(String brand, String model);
    
    /**
     * Check if product exists by brand and model
     */
    boolean existsByBrandAndModel(String brand, String model);
    
    /**
     * Find products by category and status
     */
    List<ProductJpaEntity> findByCategoryAndStatus(String category, ProductStatus status);
    
    /**
     * Find products launched within date range
     */
    List<ProductJpaEntity> findByLaunchDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count products by category
     */
    long countByCategory(String category);
    
    /**
     * Count products by status
     */
    long countByStatus(ProductStatus status);
    
    /**
     * Search products by keyword in multiple fields
     */
    @Query("SELECT p FROM ProductJpaEntity p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ProductJpaEntity> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Search products by keyword with pagination
     */
    @Query("SELECT p FROM ProductJpaEntity p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ProductJpaEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Count search results by keyword
     */
    @Query("SELECT COUNT(p) FROM ProductJpaEntity p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    long countSearchResults(@Param("keyword") String keyword);
    
    /**
     * Find products by multiple categories
     */
    List<ProductJpaEntity> findByCategoryIn(List<String> categories);
    
    /**
     * Find products by multiple brands
     */
    List<ProductJpaEntity> findByBrandIn(List<String> brands);
    
    /**
     * Find products by multiple statuses
     */
    List<ProductJpaEntity> findByStatusIn(List<ProductStatus> statuses);
    
    /**
     * Find products with price greater than specified amount
     */
    List<ProductJpaEntity> findByPriceGreaterThan(BigDecimal price);
    
    /**
     * Find products with price less than specified amount
     */
    List<ProductJpaEntity> findByPriceLessThan(BigDecimal price);
    
    /**
     * Find recently launched products
     */
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.launchDate >= :sinceDate ORDER BY p.launchDate DESC")
    List<ProductJpaEntity> findRecentlyLaunched(@Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Find products by brand ordered by launch date
     */
    List<ProductJpaEntity> findByBrandOrderByLaunchDateDesc(String brand);
    
    /**
     * Find products by category ordered by price
     */
    List<ProductJpaEntity> findByCategoryOrderByPriceAsc(String category);
    
    /**
     * Delete product by product ID
     */
    void deleteByProductId(String productId);
}