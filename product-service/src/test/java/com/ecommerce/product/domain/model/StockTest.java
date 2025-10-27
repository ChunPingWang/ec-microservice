package com.ecommerce.product.domain.model;

import com.ecommerce.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Stock domain entity
 * Tests stock management business rules and validation logic
 */
@DisplayName("Stock Domain Entity Tests")
class StockTest {

    @Nested
    @DisplayName("Stock Creation Tests")
    class StockCreationTests {

        @Test
        @DisplayName("Should create stock with valid data")
        void shouldCreateStockWithValidData() {
            // Given
            String productId = "PROD-123";
            Integer initialQuantity = 100;
            Integer minimumThreshold = 10;
            String warehouseLocation = "台北倉庫";

            // When
            Stock stock = Stock.create(productId, initialQuantity, minimumThreshold, warehouseLocation);

            // Then
            assertNotNull(stock);
            assertNotNull(stock.getStockId());
            assertEquals(productId, stock.getProductId());
            assertEquals(initialQuantity, stock.getQuantity());
            assertEquals(0, stock.getReservedQuantity());
            assertEquals(minimumThreshold, stock.getMinimumThreshold());
            assertEquals(10000, stock.getMaximumCapacity());
            assertEquals(warehouseLocation, stock.getWarehouseLocation());
            assertEquals(initialQuantity, stock.getAvailableQuantity());
            assertNotNull(stock.getLastRestockDate());
            assertFalse(stock.isLowStock());
            assertFalse(stock.isOutOfStock());
        }

        @Test
        @DisplayName("Should throw exception when product ID is null")
        void shouldThrowExceptionWhenProductIdIsNull() {
            // Given
            String productId = null;
            Integer initialQuantity = 100;
            Integer minimumThreshold = 10;
            String warehouseLocation = "台北倉庫";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Stock.create(productId, initialQuantity, minimumThreshold, warehouseLocation)
            );
            assertEquals("Product ID is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void shouldThrowExceptionWhenQuantityIsNegative() {
            // Given
            String productId = "PROD-123";
            Integer initialQuantity = -1;
            Integer minimumThreshold = 10;
            String warehouseLocation = "台北倉庫";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Stock.create(productId, initialQuantity, minimumThreshold, warehouseLocation)
            );
            assertEquals("Quantity cannot be negative", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Stock Addition Tests")
    class StockAdditionTests {

        @Test
        @DisplayName("Should add stock successfully")
        void shouldAddStockSuccessfully() {
            // Given
            Stock stock = Stock.create("PROD-123", 50, 10, "台北倉庫");
            Integer additionalQuantity = 30;

            // When
            stock.addStock(additionalQuantity);

            // Then
            assertEquals(80, stock.getQuantity());
            assertEquals(80, stock.getAvailableQuantity());
            assertNotNull(stock.getLastRestockDate());
            assertNotNull(stock.getUpdatedAt());
        }

        @Test
        @DisplayName("Should throw exception when adding zero or negative quantity")
        void shouldThrowExceptionWhenAddingZeroOrNegativeQuantity() {
            // Given
            Stock stock = Stock.create("PROD-123", 50, 10, "台北倉庫");

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                stock.addStock(0)
            );
            assertEquals("Additional quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when exceeding maximum capacity")
        void shouldThrowExceptionWhenExceedingMaximumCapacity() {
            // Given
            Stock stock = Stock.create("PROD-123", 9990, 10, "台北倉庫");

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                stock.addStock(20)
            );
            assertEquals("Cannot exceed maximum capacity of 10000", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Stock Reduction Tests")
    class StockReductionTests {

        @Test
        @DisplayName("Should reduce stock successfully")
        void shouldReduceStockSuccessfully() {
            // Given
            Stock stock = Stock.create("PROD-123", 50, 10, "台北倉庫");
            Integer quantityToReduce = 20;

            // When
            stock.reduceStock(quantityToReduce);

            // Then
            assertEquals(30, stock.getQuantity());
            assertEquals(30, stock.getAvailableQuantity());
            assertNotNull(stock.getLastSaleDate());
            assertNotNull(stock.getUpdatedAt());
        }

        @Test
        @DisplayName("Should throw exception when reducing more than available")
        void shouldThrowExceptionWhenReducingMoreThanAvailable() {
            // Given
            Stock stock = Stock.create("PROD-123", 20, 10, "台北倉庫");
            stock.reserveStock(10); // Reserve 10, leaving 10 available

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                stock.reduceStock(15)
            );
            assertTrue(exception.getMessage().contains("Insufficient available stock"));
        }
    }

    @Nested
    @DisplayName("Stock Reservation Tests")
    class StockReservationTests {

        @Test
        @DisplayName("Should reserve stock successfully")
        void shouldReserveStockSuccessfully() {
            // Given
            Stock stock = Stock.create("PROD-123", 50, 10, "台北倉庫");
            Integer quantityToReserve = 20;

            // When
            stock.reserveStock(quantityToReserve);

            // Then
            assertEquals(50, stock.getQuantity());
            assertEquals(20, stock.getReservedQuantity());
            assertEquals(30, stock.getAvailableQuantity());
            assertTrue(stock.hasReservedStock());
            assertNotNull(stock.getUpdatedAt());
        }

        @Test
        @DisplayName("Should release reservation successfully")
        void shouldReleaseReservationSuccessfully() {
            // Given
            Stock stock = Stock.create("PROD-123", 50, 10, "台北倉庫");
            stock.reserveStock(20);

            // When
            stock.releaseReservation(10);

            // Then
            assertEquals(50, stock.getQuantity());
            assertEquals(10, stock.getReservedQuantity());
            assertEquals(40, stock.getAvailableQuantity());
            assertNotNull(stock.getUpdatedAt());
        }

        @Test
        @DisplayName("Should confirm reservation successfully")
        void shouldConfirmReservationSuccessfully() {
            // Given
            Stock stock = Stock.create("PROD-123", 50, 10, "台北倉庫");
            stock.reserveStock(20);

            // When
            stock.confirmReservation(15);

            // Then
            assertEquals(35, stock.getQuantity());
            assertEquals(5, stock.getReservedQuantity());
            assertEquals(30, stock.getAvailableQuantity());
            assertNotNull(stock.getLastSaleDate());
            assertNotNull(stock.getUpdatedAt());
        }

        @Test
        @DisplayName("Should throw exception when confirming more than reserved")
        void shouldThrowExceptionWhenConfirmingMoreThanReserved() {
            // Given
            Stock stock = Stock.create("PROD-123", 50, 10, "台北倉庫");
            stock.reserveStock(10);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                stock.confirmReservation(15)
            );
            assertTrue(exception.getMessage().contains("Cannot confirm more than reserved quantity"));
        }
    }

    @Nested
    @DisplayName("Stock Status Tests")
    class StockStatusTests {

        @Test
        @DisplayName("Should detect low stock correctly")
        void shouldDetectLowStockCorrectly() {
            // Given
            Stock stock = Stock.create("PROD-123", 15, 20, "台北倉庫");

            // When & Then
            assertTrue(stock.isLowStock());
            assertFalse(stock.isOutOfStock());
        }

        @Test
        @DisplayName("Should detect out of stock correctly")
        void shouldDetectOutOfStockCorrectly() {
            // Given
            Stock stock = Stock.create("PROD-123", 0, 10, "台北倉庫");

            // When & Then
            assertTrue(stock.isOutOfStock());
            assertTrue(stock.isLowStock());
        }

        @Test
        @DisplayName("Should calculate stock utilization percentage correctly")
        void shouldCalculateStockUtilizationPercentageCorrectly() {
            // Given
            Stock stock = Stock.create("PROD-123", 2500, 10, "台北倉庫");

            // When
            Integer utilization = stock.getStockUtilizationPercentage();

            // Then
            assertEquals(25, utilization);
        }

        @Test
        @DisplayName("Should detect at capacity correctly")
        void shouldDetectAtCapacityCorrectly() {
            // Given
            Stock stock = Stock.create("PROD-123", 10000, 10, "台北倉庫");

            // When & Then
            assertTrue(stock.isAtCapacity());
        }
    }

    @Nested
    @DisplayName("Stock Threshold Management Tests")
    class StockThresholdManagementTests {

        @Test
        @DisplayName("Should update thresholds successfully")
        void shouldUpdateThresholdsSuccessfully() {
            // Given
            Stock stock = Stock.create("PROD-123", 100, 10, "台北倉庫");
            Integer newMinThreshold = 20;
            Integer newMaxCapacity = 5000;

            // When
            stock.updateThresholds(newMinThreshold, newMaxCapacity);

            // Then
            assertEquals(newMinThreshold, stock.getMinimumThreshold());
            assertEquals(newMaxCapacity, stock.getMaximumCapacity());
            assertNotNull(stock.getUpdatedAt());
        }

        @Test
        @DisplayName("Should relocate stock successfully")
        void shouldRelocateStockSuccessfully() {
            // Given
            Stock stock = Stock.create("PROD-123", 100, 10, "台北倉庫");
            String newLocation = "高雄倉庫";

            // When
            stock.relocateStock(newLocation);

            // Then
            assertEquals(newLocation, stock.getWarehouseLocation());
            assertNotNull(stock.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Stock Availability Tests")
    class StockAvailabilityTests {

        @Test
        @DisplayName("Should check available stock correctly")
        void shouldCheckAvailableStockCorrectly() {
            // Given
            Stock stock = Stock.create("PROD-123", 50, 10, "台北倉庫");
            stock.reserveStock(20);

            // When & Then
            assertTrue(stock.hasAvailableStock(30));
            assertFalse(stock.hasAvailableStock(35));
            assertEquals(30, stock.getAvailableQuantity());
        }
    }
}