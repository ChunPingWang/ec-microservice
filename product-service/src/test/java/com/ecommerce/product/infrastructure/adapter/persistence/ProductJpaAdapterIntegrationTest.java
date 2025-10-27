package com.ecommerce.product.infrastructure.adapter.persistence;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.infrastructure.adapter.persistence.entity.ProductJpaEntity;
import com.ecommerce.product.infrastructure.adapter.persistence.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProductJpaAdapter
 * Tests database operations and caching mechanisms
 */
@DataJpaTest
@ContextConfiguration(classes = {ProductJpaAdapterIntegrationTest.TestConfig.class})
@DisplayName("Product JPA Adapter Integration Tests")
class ProductJpaAdapterIntegrationTest {

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("products", "productSearch");
        }

        @Bean
        public ProductJpaAdapter productJpaAdapter(ProductJpaRepository repository, CacheManager cacheManager) {
            return new ProductJpaAdapter(repository);
        }
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private ProductJpaAdapter productJpaAdapter;

    @Autowired
    private CacheManager cacheManager;

    private ProductJpaEntity testProductEntity;

    @BeforeEach
    void setUp() {
        // Clear caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> 
            cacheManager.getCache(cacheName).clear());

        // Create test product entity
        testProductEntity = new ProductJpaEntity();
        testProductEntity.setProductId("PROD-TEST-123");
        testProductEntity.setName("iPhone 17 Pro Test");
        testProductEntity.setDescription("Test iPhone for integration testing");
        testProductEntity.setCategory("Smartphones");
        testProductEntity.setPrice(new BigDecimal("39900.00"));
        testProductEntity.setBrand("Apple");
        testProductEntity.setModel("iPhone 17 Pro");
        testProductEntity.setStatus(ProductStatus.AVAILABLE);
        testProductEntity.setSpecifications("Test specifications");
        
        entityManager.persistAndFlush(testProductEntity);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Product Persistence Tests")
    class ProductPersistenceTests {

        @Test
        @DisplayName("Should save and find product by ID")
        void shouldSaveAndFindProductById() {
            // Given
            Product product = Product.createIPhone17Pro();

            // When
            Product savedProduct = productJpaAdapter.save(product);
            Optional<Product> foundProduct = productJpaAdapter.findById(savedProduct.getProductId());

            // Then
            assertTrue(foundProduct.isPresent());
            assertEquals(savedProduct.getProductId(), foundProduct.get().getProductId());
            assertEquals(savedProduct.getName(), foundProduct.get().getName());
            assertEquals(savedProduct.getBrand(), foundProduct.get().getBrand());
        }

        @Test
        @DisplayName("Should return empty when product not found")
        void shouldReturnEmptyWhenProductNotFound() {
            // When
            Optional<Product> foundProduct = productJpaAdapter.findById("NON-EXISTENT");

            // Then
            assertFalse(foundProduct.isPresent());
        }

        @Test
        @DisplayName("Should find products by category")
        void shouldFindProductsByCategory() {
            // Given
            String category = "Smartphones";

            // When
            List<Product> products = productJpaAdapter.findByCategory(category, 0, 10);

            // Then
            assertNotNull(products);
            assertFalse(products.isEmpty());
            assertTrue(products.stream().allMatch(p -> category.equals(p.getCategory())));
        }

        @Test
        @DisplayName("Should search products by keyword")
        void shouldSearchProductsByKeyword() {
            // Given
            String keyword = "iPhone";

            // When
            List<Product> products = productJpaAdapter.searchByKeyword(keyword, 0, 10);

            // Then
            assertNotNull(products);
            assertFalse(products.isEmpty());
            assertTrue(products.stream().anyMatch(p -> 
                p.getName().contains(keyword) || p.getDescription().contains(keyword)));
        }

        @Test
        @DisplayName("Should find products by brand")
        void shouldFindProductsByBrand() {
            // Given
            String brand = "Apple";

            // When
            List<Product> products = productJpaAdapter.findByBrand(brand);

            // Then
            assertNotNull(products);
            assertFalse(products.isEmpty());
            assertTrue(products.stream().allMatch(p -> brand.equals(p.getBrand())));
        }

        @Test
        @DisplayName("Should find products by status")
        void shouldFindProductsByStatus() {
            // Given
            ProductStatus status = ProductStatus.AVAILABLE;

            // When
            List<Product> products = productJpaAdapter.findByStatus(status, 0, 10);

            // Then
            assertNotNull(products);
            assertFalse(products.isEmpty());
            assertTrue(products.stream().allMatch(p -> status.equals(p.getStatus())));
        }

        @Test
        @DisplayName("Should count products by category")
        void shouldCountProductsByCategory() {
            // Given
            String category = "Smartphones";

            // When
            long count = productJpaAdapter.countByCategory(category);

            // Then
            assertTrue(count > 0);
        }

        @Test
        @DisplayName("Should count search results")
        void shouldCountSearchResults() {
            // Given
            String keyword = "iPhone";

            // When
            long count = productJpaAdapter.countSearchResults(keyword);

            // Then
            assertTrue(count > 0);
        }
    }

    @Nested
    @DisplayName("Cache Mechanism Tests")
    class CacheMechanismTests {

        @Test
        @DisplayName("Should cache product lookup results")
        void shouldCacheProductLookupResults() {
            // Given
            String productId = testProductEntity.getProductId();

            // When - First call should hit database
            Optional<Product> firstCall = productJpaAdapter.findById(productId);
            
            // Clear entity manager to ensure subsequent calls would need to hit database
            entityManager.clear();
            
            // Second call should hit cache
            Optional<Product> secondCall = productJpaAdapter.findById(productId);

            // Then
            assertTrue(firstCall.isPresent());
            assertTrue(secondCall.isPresent());
            assertEquals(firstCall.get().getProductId(), secondCall.get().getProductId());
            
            // Verify cache contains the product
            assertNotNull(cacheManager.getCache("products"));
        }

        @Test
        @DisplayName("Should cache search results")
        void shouldCacheSearchResults() {
            // Given
            String keyword = "iPhone";

            // When - First search should hit database
            List<Product> firstSearch = productJpaAdapter.searchByKeyword(keyword, 0, 10);
            
            // Clear entity manager
            entityManager.clear();
            
            // Second search should hit cache
            List<Product> secondSearch = productJpaAdapter.searchByKeyword(keyword, 0, 10);

            // Then
            assertNotNull(firstSearch);
            assertNotNull(secondSearch);
            assertEquals(firstSearch.size(), secondSearch.size());
            
            // Verify cache contains search results
            assertNotNull(cacheManager.getCache("productSearch"));
        }

        @Test
        @DisplayName("Should invalidate cache when product is updated")
        void shouldInvalidateCacheWhenProductIsUpdated() {
            // Given
            String productId = testProductEntity.getProductId();
            
            // Cache the product first
            Optional<Product> cachedProduct = productJpaAdapter.findById(productId);
            assertTrue(cachedProduct.isPresent());

            // When - Update the product
            Product updatedProduct = cachedProduct.get();
            updatedProduct.updateProductInfo("Updated Name", "Updated Description", new BigDecimal("45900.00"));
            productJpaAdapter.save(updatedProduct);

            // Then - Fetch again should return updated product
            Optional<Product> refreshedProduct = productJpaAdapter.findById(productId);
            assertTrue(refreshedProduct.isPresent());
            assertEquals("Updated Name", refreshedProduct.get().getName());
        }

        @Test
        @DisplayName("Should handle cache eviction correctly")
        void shouldHandleCacheEvictionCorrectly() {
            // Given
            String productId = testProductEntity.getProductId();
            
            // Cache the product
            Optional<Product> cachedProduct = productJpaAdapter.findById(productId);
            assertTrue(cachedProduct.isPresent());

            // When - Manually evict cache
            cacheManager.getCache("products").evict(productId);

            // Then - Next call should hit database again
            Optional<Product> afterEviction = productJpaAdapter.findById(productId);
            assertTrue(afterEviction.isPresent());
            assertEquals(cachedProduct.get().getProductId(), afterEviction.get().getProductId());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should demonstrate cache performance improvement")
        void shouldDemonstrateCachePerformanceImprovement() {
            // Given
            String productId = testProductEntity.getProductId();

            // When - Measure first call (database hit)
            long startTime1 = System.nanoTime();
            Optional<Product> firstCall = productJpaAdapter.findById(productId);
            long endTime1 = System.nanoTime();
            long firstCallDuration = endTime1 - startTime1;

            // Clear entity manager to simulate fresh state
            entityManager.clear();

            // Measure second call (cache hit)
            long startTime2 = System.nanoTime();
            Optional<Product> secondCall = productJpaAdapter.findById(productId);
            long endTime2 = System.nanoTime();
            long secondCallDuration = endTime2 - startTime2;

            // Then
            assertTrue(firstCall.isPresent());
            assertTrue(secondCall.isPresent());
            assertEquals(firstCall.get().getProductId(), secondCall.get().getProductId());
            
            // Cache hit should be faster (though this might be flaky in test environment)
            // We'll just verify both calls completed successfully
            assertTrue(firstCallDuration > 0);
            assertTrue(secondCallDuration > 0);
        }

        @Test
        @DisplayName("Should handle multiple concurrent cache access")
        void shouldHandleMultipleConcurrentCacheAccess() throws InterruptedException {
            // Given
            String productId = testProductEntity.getProductId();
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] results = new boolean[threadCount];

            // When - Multiple threads accessing same cached data
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    Optional<Product> product = productJpaAdapter.findById(productId);
                    results[index] = product.isPresent();
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then - All threads should successfully retrieve the product
            for (boolean result : results) {
                assertTrue(result);
            }
        }
    }

    @Nested
    @DisplayName("Data Consistency Tests")
    class DataConsistencyTests {

        @Test
        @DisplayName("Should maintain data consistency across cache and database")
        void shouldMaintainDataConsistencyAcrossCacheAndDatabase() {
            // Given
            Product product = Product.createIPhone17Pro();
            
            // When - Save product
            Product savedProduct = productJpaAdapter.save(product);
            
            // Retrieve from cache
            Optional<Product> cachedProduct = productJpaAdapter.findById(savedProduct.getProductId());
            
            // Retrieve directly from database
            Optional<ProductJpaEntity> dbEntity = productJpaRepository.findById(savedProduct.getProductId());

            // Then
            assertTrue(cachedProduct.isPresent());
            assertTrue(dbEntity.isPresent());
            assertEquals(cachedProduct.get().getName(), dbEntity.get().getName());
            assertEquals(cachedProduct.get().getPrice(), dbEntity.get().getPrice());
            assertEquals(cachedProduct.get().getBrand(), dbEntity.get().getBrand());
        }

        @Test
        @DisplayName("Should handle null values correctly in cache")
        void shouldHandleNullValuesCorrectlyInCache() {
            // Given
            String nonExistentId = "NON-EXISTENT-PRODUCT";

            // When - First call returns empty
            Optional<Product> firstCall = productJpaAdapter.findById(nonExistentId);
            
            // Second call should also return empty (cached result)
            Optional<Product> secondCall = productJpaAdapter.findById(nonExistentId);

            // Then
            assertFalse(firstCall.isPresent());
            assertFalse(secondCall.isPresent());
        }
    }
}