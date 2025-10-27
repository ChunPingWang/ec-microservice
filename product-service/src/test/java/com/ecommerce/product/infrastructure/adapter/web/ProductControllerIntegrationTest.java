package com.ecommerce.product.infrastructure.adapter.web;

import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.dto.ProductSearchRequest;
import com.ecommerce.product.application.port.in.ProductSearchUseCase;
import com.ecommerce.product.domain.exception.ProductNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController
 * Tests REST API endpoints and error handling
 */
@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductSearchUseCase productSearchUseCase;

    @Nested
    @DisplayName("Product Search API Tests")
    class ProductSearchApiTests {

        @Test
        @DisplayName("Should search products by keyword successfully")
        void shouldSearchProductsByKeywordSuccessfully() throws Exception {
            // Given
            String keyword = "iPhone";
            ProductDto productDto = createTestProductDto();
            List<ProductDto> products = Arrays.asList(productDto);

            when(productSearchUseCase.searchByKeyword(keyword)).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/v1/products/search")
                    .param("keyword", keyword)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].name").value("iPhone 17 Pro"))
                    .andExpect(jsonPath("$.data[0].brand").value("Apple"))
                    .andExpect(jsonPath("$.data[0].price").value(39900.00))
                    .andExpect(jsonPath("$.data[0].inStock").value(true));
        }

        @Test
        @DisplayName("Should return empty list for non-existent keyword")
        void shouldReturnEmptyListForNonExistentKeyword() throws Exception {
            // Given
            String keyword = "NonExistentProduct";
            when(productSearchUseCase.searchByKeyword(keyword)).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/v1/products/search")
                    .param("keyword", keyword)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should handle missing keyword parameter")
        void shouldHandleMissingKeywordParameter() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/products/search")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should search products by category successfully")
        void shouldSearchProductsByCategorySuccessfully() throws Exception {
            // Given
            String category = "Smartphones";
            ProductDto productDto = createTestProductDto();
            List<ProductDto> products = Arrays.asList(productDto);

            when(productSearchUseCase.getProductsByCategory(category)).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/v1/products/category/{category}", category)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].category").value("Smartphones"));
        }

        @Test
        @DisplayName("Should get featured products successfully")
        void shouldGetFeaturedProductsSuccessfully() throws Exception {
            // Given
            ProductDto productDto = createTestProductDto();
            List<ProductDto> products = Arrays.asList(productDto);

            when(productSearchUseCase.getFeaturedProducts()).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/v1/products/featured")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].name").value("iPhone 17 Pro"));
        }
    }

    @Nested
    @DisplayName("Product Retrieval API Tests")
    class ProductRetrievalApiTests {

        @Test
        @DisplayName("Should get product by ID successfully")
        void shouldGetProductByIdSuccessfully() throws Exception {
            // Given
            String productId = "PROD-123";
            ProductDto productDto = createTestProductDto();
            productDto.setId(productId);

            when(productSearchUseCase.getProductById(productId)).thenReturn(productDto);

            // When & Then
            mockMvc.perform(get("/api/v1/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(productId))
                    .andExpect(jsonPath("$.data.name").value("iPhone 17 Pro"))
                    .andExpect(jsonPath("$.data.brand").value("Apple"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            String productId = "PROD-999";
            when(productSearchUseCase.getProductById(productId))
                    .thenThrow(new ProductNotFoundException(productId));

            // When & Then
            mockMvc.perform(get("/api/v1/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").exists());
        }

        @Test
        @DisplayName("Should validate product ID format")
        void shouldValidateProductIdFormat() throws Exception {
            // Given
            String invalidProductId = "";

            // When & Then
            mockMvc.perform(get("/api/v1/products/{productId}", invalidProductId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound()); // Empty path parameter results in 404
        }
    }

    @Nested
    @DisplayName("Advanced Search API Tests")
    class AdvancedSearchApiTests {

        @Test
        @DisplayName("Should perform advanced search with all parameters")
        void shouldPerformAdvancedSearchWithAllParameters() throws Exception {
            // Given
            ProductSearchRequest searchRequest = new ProductSearchRequest();
            searchRequest.setKeyword("iPhone");
            searchRequest.setCategory("Smartphones");
            searchRequest.setMinPrice(new BigDecimal("30000"));
            searchRequest.setMaxPrice(new BigDecimal("50000"));
            searchRequest.setInStockOnly(true);
            searchRequest.setPage(0);
            searchRequest.setSize(10);

            ProductDto productDto = createTestProductDto();
            when(productSearchUseCase.searchByKeyword(anyString())).thenReturn(Arrays.asList(productDto));

            // When & Then
            mockMvc.perform(post("/api/v1/products/search/advanced")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("Should handle invalid search request")
        void shouldHandleInvalidSearchRequest() throws Exception {
            // Given
            ProductSearchRequest invalidRequest = new ProductSearchRequest();
            invalidRequest.setPage(-1); // Invalid page number

            // When & Then
            mockMvc.perform(post("/api/v1/products/search/advanced")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("Should handle malformed JSON request")
        void shouldHandleMalformedJsonRequest() throws Exception {
            // Given
            String malformedJson = "{ invalid json }";

            // When & Then
            mockMvc.perform(post("/api/v1/products/search/advanced")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Available Products API Tests")
    class AvailableProductsApiTests {

        @Test
        @DisplayName("Should get available products successfully")
        void shouldGetAvailableProductsSuccessfully() throws Exception {
            // Given
            ProductDto productDto = createTestProductDto();
            List<ProductDto> products = Arrays.asList(productDto);

            when(productSearchUseCase.getAvailableProducts()).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/v1/products/available")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].inStock").value(true));
        }

        @Test
        @DisplayName("Should get available products with pagination")
        void shouldGetAvailableProductsWithPagination() throws Exception {
            // Given
            int page = 0;
            int size = 5;
            ProductDto productDto = createTestProductDto();
            List<ProductDto> products = Arrays.asList(productDto);

            when(productSearchUseCase.getAvailableProducts()).thenReturn(products);

            // When & Then
            mockMvc.perform(get("/api/v1/products/available")
                    .param("page", String.valueOf(page))
                    .param("size", String.valueOf(size))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("Should validate pagination parameters")
        void shouldValidatePaginationParameters() throws Exception {
            // When & Then - Invalid page number
            mockMvc.perform(get("/api/v1/products/available")
                    .param("page", "-1")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            // When & Then - Invalid page size
            mockMvc.perform(get("/api/v1/products/available")
                    .param("page", "0")
                    .param("size", "0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Product Suggestions API Tests")
    class ProductSuggestionsApiTests {

        @Test
        @DisplayName("Should get product suggestions successfully")
        void shouldGetProductSuggestionsSuccessfully() throws Exception {
            // Given
            String category = "Smartphones";
            int limit = 5;
            ProductDto productDto = createTestProductDto();
            List<ProductDto> suggestions = Arrays.asList(productDto);

            when(productSearchUseCase.getProductSuggestions(category, limit)).thenReturn(suggestions);

            // When & Then
            mockMvc.perform(get("/api/v1/products/suggestions")
                    .param("category", category)
                    .param("limit", String.valueOf(limit))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].category").value(category));
        }

        @Test
        @DisplayName("Should get general suggestions when no category provided")
        void shouldGetGeneralSuggestionsWhenNoCategoryProvided() throws Exception {
            // Given
            int limit = 3;
            ProductDto productDto = createTestProductDto();
            List<ProductDto> suggestions = Arrays.asList(productDto);

            when(productSearchUseCase.getProductSuggestions(null, limit)).thenReturn(suggestions);

            // When & Then
            mockMvc.perform(get("/api/v1/products/suggestions")
                    .param("limit", String.valueOf(limit))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("Should use default limit when not provided")
        void shouldUseDefaultLimitWhenNotProvided() throws Exception {
            // Given
            ProductDto productDto = createTestProductDto();
            List<ProductDto> suggestions = Arrays.asList(productDto);

            when(productSearchUseCase.getProductSuggestions(isNull(), eq(10))).thenReturn(suggestions);

            // When & Then
            mockMvc.perform(get("/api/v1/products/suggestions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle internal server errors gracefully")
        void shouldHandleInternalServerErrorsGracefully() throws Exception {
            // Given
            String keyword = "iPhone";
            when(productSearchUseCase.searchByKeyword(keyword))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/v1/products/search")
                    .param("keyword", keyword)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("Should handle unsupported HTTP methods")
        void shouldHandleUnsupportedHttpMethods() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/v1/products/PROD-123")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("Should handle unsupported media types")
        void shouldHandleUnsupportedMediaTypes() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/products/search/advanced")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("plain text content"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    private ProductDto createTestProductDto() {
        ProductDto productDto = new ProductDto();
        productDto.setId("PROD-TEST-123");
        productDto.setName("iPhone 17 Pro");
        productDto.setDescription("Latest iPhone with advanced features");
        productDto.setCategory("Smartphones");
        productDto.setPrice(new BigDecimal("39900.00"));
        productDto.setBrand("Apple");
        productDto.setModel("iPhone 17 Pro");
        productDto.setInStock(true);
        productDto.setStockQuantity(50);
        return productDto;
    }
}