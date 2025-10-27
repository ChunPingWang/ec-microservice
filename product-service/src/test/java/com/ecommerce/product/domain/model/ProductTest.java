package com.ecommerce.product.domain.model;

import com.ecommerce.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product domain entity
 * Tests business rules and validation logic
 */
@DisplayName("Product Domain Entity Tests")
class ProductTest {

    @Nested
    @DisplayName("Product Creation Tests")
    class ProductCreationTests {

        @Test
        @DisplayName("Should create product with valid data")
        void shouldCreateProductWithValidData() {
            // Given
            String name = "iPhone 17 Pro";
            String description = "Latest iPhone with advanced features";
            String category = "Smartphones";
            BigDecimal price = new BigDecimal("39900.00");
            String brand = "Apple";
            String model = "iPhone 17 Pro";
            String specifications = "6.3-inch display, A18 Pro chip";

            // When
            Product product = Product.create(name, description, category, price, brand, model, specifications);

            // Then
            assertNotNull(product);
            assertNotNull(product.getProductId());
            assertEquals(name, product.getName());
            assertEquals(description, product.getDescription());
            assertEquals(category, product.getCategory());
            assertEquals(price, product.getPrice());
            assertEquals(brand, product.getBrand());
            assertEquals(model, product.getModel());
            assertEquals(specifications, product.getSpecifications());
            assertEquals(ProductStatus.AVAILABLE, product.getStatus());
            assertNotNull(product.getLaunchDate());
            assertTrue(product.isAvailable());
            assertEquals("Apple iPhone 17 Pro", product.getFullName());
        }

        @Test
        @DisplayName("Should create iPhone 17 Pro with factory method")
        void shouldCreateIPhone17ProWithFactoryMethod() {
            // When
            Product product = Product.createIPhone17Pro();

            // Then
            assertNotNull(product);
            assertEquals("iPhone 17 Pro", product.getName());
            assertEquals("Apple", product.getBrand());
            assertEquals("iPhone 17 Pro", product.getModel());
            assertEquals(new BigDecimal("39900.00"), product.getPrice());
            assertEquals("Smartphones", product.getCategory());
            assertTrue(product.getSpecifications().contains("A18 Pro chip"));
            assertEquals(ProductStatus.AVAILABLE, product.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            // Given
            String name = null;
            String description = "Test description";
            String category = "Test";
            BigDecimal price = new BigDecimal("100.00");
            String brand = "Test Brand";
            String model = "Test Model";
            String specifications = "Test specs";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Product.create(name, description, category, price, brand, model, specifications)
            );
            assertEquals("Product name is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when price is zero")
        void shouldThrowExceptionWhenPriceIsZero() {
            // Given
            String name = "Test Product";
            String description = "Test description";
            String category = "Test";
            BigDecimal price = BigDecimal.ZERO;
            String brand = "Test Brand";
            String model = "Test Model";
            String specifications = "Test specs";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Product.create(name, description, category, price, brand, model, specifications)
            );
            assertEquals("Product price must be greater than zero", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when price has more than 2 decimal places")
        void shouldThrowExceptionWhenPriceHasMoreThan2DecimalPlaces() {
            // Given
            String name = "Test Product";
            String description = "Test description";
            String category = "Test";
            BigDecimal price = new BigDecimal("100.123");
            String brand = "Test Brand";
            String model = "Test Model";
            String specifications = "Test specs";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Product.create(name, description, category, price, brand, model, specifications)
            );
            assertEquals("Product price cannot have more than 2 decimal places", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Product Update Tests")
    class ProductUpdateTests {

        @Test
        @DisplayName("Should update product information successfully")
        void shouldUpdateProductInformationSuccessfully() {
            // Given
            Product product = Product.createIPhone17Pro();
            String newName = "iPhone 17 Pro Max";
            String newDescription = "Updated description";
            BigDecimal newPrice = new BigDecimal("45900.00");

            // When
            product.updateProductInfo(newName, newDescription, newPrice);

            // Then
            assertEquals(newName, product.getName());
            assertEquals(newDescription, product.getDescription());
            assertEquals(newPrice, product.getPrice());
            assertNotNull(product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should update specifications successfully")
        void shouldUpdateSpecificationsSuccessfully() {
            // Given
            Product product = Product.createIPhone17Pro();
            String newSpecs = "Updated specifications with new features";

            // When
            product.updateSpecifications(newSpecs);

            // Then
            assertEquals(newSpecs, product.getSpecifications());
            assertNotNull(product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should set valid image URL successfully")
        void shouldSetValidImageUrlSuccessfully() {
            // Given
            Product product = Product.createIPhone17Pro();
            String validImageUrl = "https://example.com/image.jpg";

            // When
            product.setImageUrl(validImageUrl);

            // Then
            assertEquals(validImageUrl, product.getImageUrl());
            assertNotNull(product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should throw exception when setting invalid image URL")
        void shouldThrowExceptionWhenSettingInvalidImageUrl() {
            // Given
            Product product = Product.createIPhone17Pro();
            String invalidImageUrl = "invalid-url";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                product.setImageUrl(invalidImageUrl)
            );
            assertEquals("Invalid image URL format", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Product Status Tests")
    class ProductStatusTests {

        @Test
        @DisplayName("Should discontinue product successfully")
        void shouldDiscontinueProductSuccessfully() {
            // Given
            Product product = Product.createIPhone17Pro();
            assertTrue(product.isAvailable());

            // When
            product.discontinue();

            // Then
            assertFalse(product.isAvailable());
            assertTrue(product.isDiscontinued());
            assertEquals(ProductStatus.DISCONTINUED, product.getStatus());
            assertNotNull(product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should make product available successfully")
        void shouldMakeProductAvailableSuccessfully() {
            // Given
            Product product = Product.createIPhone17Pro();
            product.discontinue();
            assertTrue(product.isDiscontinued());

            // When
            product.makeAvailable();

            // Then
            assertTrue(product.isAvailable());
            assertFalse(product.isDiscontinued());
            assertEquals(ProductStatus.AVAILABLE, product.getStatus());
            assertNotNull(product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should mark product out of stock successfully")
        void shouldMarkProductOutOfStockSuccessfully() {
            // Given
            Product product = Product.createIPhone17Pro();
            assertTrue(product.isAvailable());

            // When
            product.markOutOfStock();

            // Then
            assertFalse(product.isAvailable());
            assertTrue(product.isOutOfStock());
            assertEquals(ProductStatus.OUT_OF_STOCK, product.getStatus());
            assertNotNull(product.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Product Equality Tests")
    class ProductEqualityTests {

        @Test
        @DisplayName("Should be equal when product IDs are same")
        void shouldBeEqualWhenProductIdsAreSame() {
            // Given
            Product product1 = Product.createIPhone17Pro();
            Product product2 = Product.createIPhone17Pro();

            // When & Then
            assertEquals(product1, product1);
            assertNotEquals(product1, product2); // Different IDs
            assertEquals(product1.hashCode(), product1.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            // Given
            Product product = Product.createIPhone17Pro();

            // When & Then
            assertNotEquals(product, null);
            assertNotEquals(product, "string");
        }
    }
}