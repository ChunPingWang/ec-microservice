package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.dto.ProductSearchRequest;
import com.ecommerce.product.application.dto.ProductSearchResponse;
import com.ecommerce.product.application.port.out.ProductPersistencePort;
import com.ecommerce.product.application.port.out.StockPersistencePort;
import com.ecommerce.product.domain.exception.ProductNotFoundException;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.domain.service.ProductDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductSearchService
 * Tests product search functionality including positive and negative cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Search Service Tests")
class ProductSearchServiceTest {

    @Mock
    private ProductPersistencePort productPersistencePort;

    @Mock
    private StockPersistencePort stockPersistencePort;

    @Mock
    private ProductDomainService productDomainService;

    private ProductSearchService productSearchService;

    @BeforeEach
    void setUp() {
        productSearchService = new ProductSearchService(
            productPersistencePort,
            stockPersistencePort,
            productDomainService
        );
    }

    @Nested
    @DisplayName("Keyword Search Tests")
    class KeywordSearchTests {

        @Test
        @DisplayName("Should search products by keyword successfully")
        void shouldSearchProductsByKeywordSuccessfully() {
            // Given
            String keyword = "iPhone";
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(product.getProductId(), 50, 10, "台北倉庫");
            
            ProductDomainService.ProductWithStock productWithStock = 
                new ProductDomainService.ProductWithStock(product, stock);
            List<ProductDomainService.ProductWithStock> productsWithStock = Arrays.asList(productWithStock);

            when(productDomainService.searchProductsWithStock(keyword)).thenReturn(productsWithStock);

            // When
            List<ProductDto> result = productSearchService.searchByKeyword(keyword);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            ProductDto productDto = result.get(0);
            assertEquals(product.getName(), productDto.getName());
            assertEquals(product.getBrand(), productDto.getBrand());
            assertTrue(productDto.isInStock());
            verify(productDomainService).searchProductsWithStock(keyword);
        }

        @Test
        @DisplayName("Should return empty list when keyword is null")
        void shouldReturnEmptyListWhenKeywordIsNull() {
            // When
            List<ProductDto> result = productSearchService.searchByKeyword(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verifyNoInteractions(productDomainService);
        }

        @Test
        @DisplayName("Should return empty list when keyword is empty")
        void shouldReturnEmptyListWhenKeywordIsEmpty() {
            // When
            List<ProductDto> result = productSearchService.searchByKeyword("   ");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verifyNoInteractions(productDomainService);
        }

        @Test
        @DisplayName("Should handle no search results gracefully")
        void shouldHandleNoSearchResultsGracefully() {
            // Given
            String keyword = "NonExistentProduct";
            when(productDomainService.searchProductsWithStock(keyword)).thenReturn(List.of());

            // When
            List<ProductDto> result = productSearchService.searchByKeyword(keyword);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productDomainService).searchProductsWithStock(keyword);
        }
    }

    @Nested
    @DisplayName("Advanced Search Tests")
    class AdvancedSearchTests {

        @Test
        @DisplayName("Should search products with pagination successfully")
        void shouldSearchProductsWithPaginationSuccessfully() {
            // Given
            ProductSearchRequest request = new ProductSearchRequest();
            request.setKeyword("iPhone");
            request.setPage(0);
            request.setSize(10);

            Product product = Product.createIPhone17Pro();
            List<Product> products = Arrays.asList(product);
            Stock stock = Stock.create(product.getProductId(), 50, 10, "台北倉庫");

            when(productPersistencePort.searchByKeyword("iPhone", 0, 10)).thenReturn(products);
            when(productPersistencePort.countSearchResults("iPhone")).thenReturn(1L);
            when(stockPersistencePort.findByProductId(product.getProductId())).thenReturn(Optional.of(stock));

            // When
            ProductSearchResponse result = productSearchService.searchProducts(request);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getProducts().size());
            assertEquals(0, result.getPage());
            assertEquals(10, result.getSize());
            assertEquals(1L, result.getTotalCount());
            verify(productPersistencePort).searchByKeyword("iPhone", 0, 10);
            verify(productPersistencePort).countSearchResults("iPhone");
        }

        @Test
        @DisplayName("Should search products by category successfully")
        void shouldSearchProductsByCategorySuccessfully() {
            // Given
            ProductSearchRequest request = new ProductSearchRequest();
            request.setCategory("Smartphones");
            request.setPage(0);
            request.setSize(10);

            Product product = Product.createIPhone17Pro();
            List<Product> products = Arrays.asList(product);

            when(productPersistencePort.findByCategory("Smartphones", 0, 10)).thenReturn(products);
            when(productPersistencePort.countByCategory("Smartphones")).thenReturn(1L);
            when(stockPersistencePort.findByProductId(product.getProductId())).thenReturn(Optional.empty());

            // When
            ProductSearchResponse result = productSearchService.searchProducts(request);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getProducts().size());
            verify(productPersistencePort).findByCategory("Smartphones", 0, 10);
            verify(productPersistencePort).countByCategory("Smartphones");
        }

        @Test
        @DisplayName("Should filter by in-stock only when requested")
        void shouldFilterByInStockOnlyWhenRequested() {
            // Given
            ProductSearchRequest request = new ProductSearchRequest();
            request.setKeyword("iPhone");
            request.setInStockOnly(true);
            request.setPage(0);
            request.setSize(10);

            Product product1 = Product.createIPhone17Pro();
            Product product2 = Product.create("iPhone 16", "Older model", "Smartphones", 
                new BigDecimal("29900"), "Apple", "iPhone 16", "A17 chip");
            List<Product> products = Arrays.asList(product1, product2);

            Stock stock1 = Stock.create(product1.getProductId(), 50, 10, "台北倉庫");
            Stock stock2 = Stock.create(product2.getProductId(), 0, 10, "台北倉庫"); // Out of stock

            when(productPersistencePort.searchByKeyword("iPhone", 0, 10)).thenReturn(products);
            when(productPersistencePort.countSearchResults("iPhone")).thenReturn(2L);
            when(stockPersistencePort.findByProductId(product1.getProductId())).thenReturn(Optional.of(stock1));
            when(stockPersistencePort.findByProductId(product2.getProductId())).thenReturn(Optional.of(stock2));

            // When
            ProductSearchResponse result = productSearchService.searchProducts(request);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getProducts().size()); // Only in-stock product
            assertTrue(result.getProducts().get(0).isInStock());
        }

        @Test
        @DisplayName("Should throw exception for invalid search request")
        void shouldThrowExceptionForInvalidSearchRequest() {
            // Given
            ProductSearchRequest request = new ProductSearchRequest();
            request.setPage(-1); // Invalid page

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productSearchService.searchProducts(request)
            );
            assertEquals("Page number must be non-negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid page size")
        void shouldThrowExceptionForInvalidPageSize() {
            // Given
            ProductSearchRequest request = new ProductSearchRequest();
            request.setPage(0);
            request.setSize(0); // Invalid size

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productSearchService.searchProducts(request)
            );
            assertEquals("Page size must be between 1 and 100", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Product Retrieval Tests")
    class ProductRetrievalTests {

        @Test
        @DisplayName("Should get product by ID successfully")
        void shouldGetProductByIdSuccessfully() {
            // Given
            String productId = "PROD-123";
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");

            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));
            when(stockPersistencePort.findByProductId(productId)).thenReturn(Optional.of(stock));

            // When
            ProductDto result = productSearchService.getProductById(productId);

            // Then
            assertNotNull(result);
            assertEquals(product.getName(), result.getName());
            assertTrue(result.isInStock());
            verify(productPersistencePort).findById(productId);
            verify(stockPersistencePort).findByProductId(productId);
        }

        @Test
        @DisplayName("Should throw exception when product not found by ID")
        void shouldThrowExceptionWhenProductNotFoundById() {
            // Given
            String productId = "PROD-999";
            when(productPersistencePort.findById(productId)).thenReturn(Optional.empty());

            // When & Then
            ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () ->
                productSearchService.getProductById(productId)
            );
            verify(productPersistencePort).findById(productId);
            verifyNoInteractions(stockPersistencePort);
        }

        @Test
        @DisplayName("Should throw exception when product ID is null")
        void shouldThrowExceptionWhenProductIdIsNull() {
            // When & Then
            ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () ->
                productSearchService.getProductById(null)
            );
            verifyNoInteractions(productPersistencePort);
        }

        @Test
        @DisplayName("Should get product without stock information")
        void shouldGetProductWithoutStockInformation() {
            // Given
            String productId = "PROD-123";
            Product product = Product.createIPhone17Pro();

            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));
            when(stockPersistencePort.findByProductId(productId)).thenReturn(Optional.empty());

            // When
            ProductDto result = productSearchService.getProductById(productId);

            // Then
            assertNotNull(result);
            assertEquals(product.getName(), result.getName());
            assertFalse(result.isInStock()); // No stock info means not in stock
        }
    }

    @Nested
    @DisplayName("Category Search Tests")
    class CategorySearchTests {

        @Test
        @DisplayName("Should get products by category successfully")
        void shouldGetProductsByCategorySuccessfully() {
            // Given
            String category = "Smartphones";
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(product.getProductId(), 50, 10, "台北倉庫");
            
            ProductDomainService.ProductWithStock productWithStock = 
                new ProductDomainService.ProductWithStock(product, stock);
            List<ProductDomainService.ProductWithStock> productsWithStock = Arrays.asList(productWithStock);

            when(productDomainService.getProductsByCategoryWithStock(category)).thenReturn(productsWithStock);

            // When
            List<ProductDto> result = productSearchService.getProductsByCategory(category);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(product.getName(), result.get(0).getName());
            verify(productDomainService).getProductsByCategoryWithStock(category);
        }

        @Test
        @DisplayName("Should return empty list for null category")
        void shouldReturnEmptyListForNullCategory() {
            // When
            List<ProductDto> result = productSearchService.getProductsByCategory(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verifyNoInteractions(productDomainService);
        }
    }

    @Nested
    @DisplayName("Featured Products Tests")
    class FeaturedProductsTests {

        @Test
        @DisplayName("Should get featured products successfully")
        void shouldGetFeaturedProductsSuccessfully() {
            // Given
            Product iPhoneProduct = Product.createIPhone17Pro();
            List<Product> featuredProducts = Arrays.asList(iPhoneProduct);
            Stock stock = Stock.create(iPhoneProduct.getProductId(), 50, 10, "台北倉庫");

            when(productPersistencePort.findByBrandAndModel("Apple", "iPhone 17 Pro")).thenReturn(featuredProducts);
            when(stockPersistencePort.findByProductId(iPhoneProduct.getProductId())).thenReturn(Optional.of(stock));

            // When
            List<ProductDto> result = productSearchService.getFeaturedProducts();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(iPhoneProduct.getName(), result.get(0).getName());
            verify(productPersistencePort).findByBrandAndModel("Apple", "iPhone 17 Pro");
        }

        @Test
        @DisplayName("Should get product suggestions with limit")
        void shouldGetProductSuggestionsWithLimit() {
            // Given
            String category = "Smartphones";
            int limit = 3;
            Product product = Product.createIPhone17Pro();
            List<Product> products = Arrays.asList(product);

            when(productPersistencePort.findByCategory(category, 0, limit)).thenReturn(products);
            when(stockPersistencePort.findByProductId(product.getProductId())).thenReturn(Optional.empty());

            // When
            List<ProductDto> result = productSearchService.getProductSuggestions(category, limit);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(productPersistencePort).findByCategory(category, 0, limit);
        }

        @Test
        @DisplayName("Should get general suggestions when category is null")
        void shouldGetGeneralSuggestionsWhenCategoryIsNull() {
            // Given
            int limit = 3;
            Product iPhoneProduct = Product.createIPhone17Pro();
            List<Product> featuredProducts = Arrays.asList(iPhoneProduct);

            when(productPersistencePort.findByBrandAndModel("Apple", "iPhone 17 Pro")).thenReturn(featuredProducts);
            when(stockPersistencePort.findByProductId(iPhoneProduct.getProductId())).thenReturn(Optional.empty());

            // When
            List<ProductDto> result = productSearchService.getProductSuggestions(null, limit);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(productPersistencePort).findByBrandAndModel("Apple", "iPhone 17 Pro");
        }
    }

    @Nested
    @DisplayName("Available Products Tests")
    class AvailableProductsTests {

        @Test
        @DisplayName("Should get available products successfully")
        void shouldGetAvailableProductsSuccessfully() {
            // Given
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(product.getProductId(), 50, 10, "台北倉庫");
            
            ProductDomainService.ProductWithStock productWithStock = 
                new ProductDomainService.ProductWithStock(product, stock);
            List<ProductDomainService.ProductWithStock> productsWithStock = Arrays.asList(productWithStock);

            when(productDomainService.getAvailableProductsWithStock()).thenReturn(productsWithStock);

            // When
            List<ProductDto> result = productSearchService.getAvailableProducts();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).isInStock());
            verify(productDomainService).getAvailableProductsWithStock();
        }

        @Test
        @DisplayName("Should get available products with pagination")
        void shouldGetAvailableProductsWithPagination() {
            // Given
            int page = 0;
            int size = 10;
            Product product = Product.createIPhone17Pro();
            List<Product> products = Arrays.asList(product);
            Stock stock = Stock.create(product.getProductId(), 50, 10, "台北倉庫");

            when(productPersistencePort.findByStatus(ProductStatus.AVAILABLE, page, size)).thenReturn(products);
            when(productPersistencePort.countByStatus(ProductStatus.AVAILABLE)).thenReturn(1L);
            when(stockPersistencePort.findByProductId(product.getProductId())).thenReturn(Optional.of(stock));

            // When
            ProductSearchResponse result = productSearchService.getAvailableProducts(page, size);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getProducts().size());
            assertTrue(result.getProducts().get(0).isInStock());
            assertEquals(1L, result.getTotalCount());
            verify(productPersistencePort).findByStatus(ProductStatus.AVAILABLE, page, size);
        }
    }
}