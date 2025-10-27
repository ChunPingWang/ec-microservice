package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.StockDto;
import com.ecommerce.product.application.dto.StockReservationRequest;
import com.ecommerce.product.application.dto.StockUpdateRequest;
import com.ecommerce.product.application.port.out.NotificationPort;
import com.ecommerce.product.application.port.out.ProductPersistencePort;
import com.ecommerce.product.application.port.out.StockPersistencePort;
import com.ecommerce.product.domain.exception.StockNotFoundException;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.domain.service.StockDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StockManagementService
 * Tests stock management functionality including concurrency safety
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Stock Management Service Tests")
class StockManagementServiceTest {

    @Mock
    private StockPersistencePort stockPersistencePort;

    @Mock
    private ProductPersistencePort productPersistencePort;

    @Mock
    private StockDomainService stockDomainService;

    @Mock
    private NotificationPort notificationPort;

    private StockManagementService stockManagementService;

    @BeforeEach
    void setUp() {
        stockManagementService = new StockManagementService(
            stockPersistencePort,
            productPersistencePort,
            stockDomainService,
            notificationPort
        );
    }

    @Nested
    @DisplayName("Stock Reservation Tests")
    class StockReservationTests {

        @Test
        @DisplayName("Should reserve stock successfully")
        void shouldReserveStockSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 10;
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");
            stock.reserveStock(quantity);

            when(stockDomainService.reserveStock(productId, quantity)).thenReturn(stock);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            // When
            StockDto result = stockManagementService.reserveStock(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            assertEquals(quantity, result.getReservedQuantity());
            verify(stockDomainService).reserveStock(productId, quantity);
        }

        @Test
        @DisplayName("Should throw exception when reserving with null product ID")
        void shouldThrowExceptionWhenReservingWithNullProductId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                stockManagementService.reserveStock(null, 10)
            );
            assertEquals("Product ID cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when reserving with invalid quantity")
        void shouldThrowExceptionWhenReservingWithInvalidQuantity() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                stockManagementService.reserveStock("PROD-123", 0)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should confirm reservation successfully")
        void shouldConfirmReservationSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 10;
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");
            stock.reserveStock(quantity);
            stock.confirmReservation(quantity);

            when(stockDomainService.confirmReservation(productId, quantity)).thenReturn(stock);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            // When
            StockDto result = stockManagementService.confirmReservation(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            verify(stockDomainService).confirmReservation(productId, quantity);
        }

        @Test
        @DisplayName("Should release reservation successfully")
        void shouldReleaseReservationSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 10;
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");

            when(stockDomainService.releaseReservation(productId, quantity)).thenReturn(stock);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            // When
            StockDto result = stockManagementService.releaseReservation(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            verify(stockDomainService).releaseReservation(productId, quantity);
        }
    }

    @Nested
    @DisplayName("Stock Addition and Reduction Tests")
    class StockAdditionAndReductionTests {

        @Test
        @DisplayName("Should add stock successfully")
        void shouldAddStockSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 20;
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 30, 10, "台北倉庫");

            when(stockDomainService.isOutOfStock(productId)).thenReturn(true);
            when(stockDomainService.restockProduct(productId, quantity)).thenReturn(stock);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            // When
            StockDto result = stockManagementService.addStock(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            verify(stockDomainService).restockProduct(productId, quantity);
            verify(notificationPort).sendRestockNotification(eq(productId), eq(product.getFullName()), anyInt());
        }

        @Test
        @DisplayName("Should reduce stock successfully")
        void shouldReduceStockSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 10;
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 5, 10, "台北倉庫"); // Low stock after reduction

            when(stockDomainService.reduceStock(productId, quantity)).thenReturn(stock);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            // When
            StockDto result = stockManagementService.reduceStock(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            verify(stockDomainService).reduceStock(productId, quantity);
            verify(notificationPort).sendLowStockAlert(eq(productId), eq(product.getFullName()), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should send out of stock notification when stock becomes zero")
        void shouldSendOutOfStockNotificationWhenStockBecomesZero() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 50;
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 0, 10, "台北倉庫"); // Out of stock

            when(stockDomainService.reduceStock(productId, quantity)).thenReturn(stock);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            // When
            StockDto result = stockManagementService.reduceStock(productId, quantity);

            // Then
            assertNotNull(result);
            verify(notificationPort).sendOutOfStockNotification(productId, product.getFullName());
        }
    }

    @Nested
    @DisplayName("Stock Query Tests")
    class StockQueryTests {

        @Test
        @DisplayName("Should get stock by product ID successfully")
        void shouldGetStockByProductIdSuccessfully() {
            // Given
            String productId = "PROD-123";
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");

            when(stockPersistencePort.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            // When
            StockDto result = stockManagementService.getStockByProductId(productId);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            assertEquals(50, result.getQuantity());
            verify(stockPersistencePort).findByProductId(productId);
        }

        @Test
        @DisplayName("Should throw exception when stock not found")
        void shouldThrowExceptionWhenStockNotFound() {
            // Given
            String productId = "PROD-999";
            when(stockPersistencePort.findByProductId(productId)).thenReturn(Optional.empty());

            // When & Then
            StockNotFoundException exception = assertThrows(StockNotFoundException.class, () ->
                stockManagementService.getStockByProductId(productId)
            );
            verify(stockPersistencePort).findByProductId(productId);
        }

        @Test
        @DisplayName("Should check available stock correctly")
        void shouldCheckAvailableStockCorrectly() {
            // Given
            String productId = "PROD-123";
            Integer requiredQuantity = 20;

            when(stockDomainService.hasAvailableStock(productId, requiredQuantity)).thenReturn(true);

            // When
            boolean result = stockManagementService.hasAvailableStock(productId, requiredQuantity);

            // Then
            assertTrue(result);
            verify(stockDomainService).hasAvailableStock(productId, requiredQuantity);
        }

        @Test
        @DisplayName("Should get available quantity correctly")
        void shouldGetAvailableQuantityCorrectly() {
            // Given
            String productId = "PROD-123";
            Integer expectedQuantity = 30;

            when(stockDomainService.getAvailableQuantity(productId)).thenReturn(expectedQuantity);

            // When
            Integer result = stockManagementService.getAvailableQuantity(productId);

            // Then
            assertEquals(expectedQuantity, result);
            verify(stockDomainService).getAvailableQuantity(productId);
        }
    }

    @Nested
    @DisplayName("Stock Status Tests")
    class StockStatusTests {

        @Test
        @DisplayName("Should get low stock products successfully")
        void shouldGetLowStockProductsSuccessfully() {
            // Given
            Stock stock1 = Stock.create("PROD-123", 5, 10, "台北倉庫");
            Stock stock2 = Stock.create("PROD-124", 8, 10, "台北倉庫");
            List<Stock> lowStockItems = Arrays.asList(stock1, stock2);
            Product product1 = Product.createIPhone17Pro();
            Product product2 = Product.createIPhone17Pro();

            when(stockDomainService.getProductsNeedingRestock()).thenReturn(lowStockItems);
            when(productPersistencePort.findById("PROD-123")).thenReturn(Optional.of(product1));
            when(productPersistencePort.findById("PROD-124")).thenReturn(Optional.of(product2));

            // When
            List<StockDto> result = stockManagementService.getLowStockProducts();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(stockDomainService).getProductsNeedingRestock();
        }

        @Test
        @DisplayName("Should get out of stock products successfully")
        void shouldGetOutOfStockProductsSuccessfully() {
            // Given
            Stock stock = Stock.create("PROD-123", 0, 10, "台北倉庫");
            List<Stock> outOfStockItems = Arrays.asList(stock);
            Product product = Product.createIPhone17Pro();

            when(stockDomainService.getOutOfStockProducts()).thenReturn(outOfStockItems);
            when(productPersistencePort.findById("PROD-123")).thenReturn(Optional.of(product));

            // When
            List<StockDto> result = stockManagementService.getOutOfStockProducts();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(0, result.get(0).getQuantity());
            verify(stockDomainService).getOutOfStockProducts();
        }
    }

    @Nested
    @DisplayName("Bulk Operations Tests")
    class BulkOperationsTests {

        @Test
        @DisplayName("Should bulk reserve stock successfully")
        void shouldBulkReserveStockSuccessfully() {
            // Given
            StockReservationRequest request1 = new StockReservationRequest("PROD-123", 10);
            StockReservationRequest request2 = new StockReservationRequest("PROD-124", 5);
            List<StockReservationRequest> requests = Arrays.asList(request1, request2);

            Stock stock1 = Stock.create("PROD-123", 50, 10, "台北倉庫");
            Stock stock2 = Stock.create("PROD-124", 30, 10, "台北倉庫");
            List<Stock> updatedStocks = Arrays.asList(stock1, stock2);

            Product product1 = Product.createIPhone17Pro();
            Product product2 = Product.createIPhone17Pro();

            when(stockDomainService.bulkReserveStock(anyList())).thenReturn(updatedStocks);
            when(productPersistencePort.findById("PROD-123")).thenReturn(Optional.of(product1));
            when(productPersistencePort.findById("PROD-124")).thenReturn(Optional.of(product2));

            // When
            List<StockDto> result = stockManagementService.bulkReserveStock(requests);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(stockDomainService).bulkReserveStock(anyList());
        }

        @Test
        @DisplayName("Should throw exception for invalid bulk reservation requests")
        void shouldThrowExceptionForInvalidBulkReservationRequests() {
            // Given
            List<StockReservationRequest> emptyRequests = List.of();

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                stockManagementService.bulkReserveStock(emptyRequests)
            );
            assertEquals("Reservation requests cannot be null or empty", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Concurrency Safety Tests")
    class ConcurrencySafetyTests {

        @Test
        @DisplayName("Should handle concurrent stock reservations safely")
        void shouldHandleConcurrentStockReservationsSafely() throws InterruptedException {
            // Given
            String productId = "PROD-123";
            Integer quantity = 5;
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");

            when(stockDomainService.reserveStock(eq(productId), eq(quantity))).thenReturn(stock);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<CompletableFuture<StockDto>> futures = Arrays.asList(
                CompletableFuture.supplyAsync(() -> stockManagementService.reserveStock(productId, quantity), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.reserveStock(productId, quantity), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.reserveStock(productId, quantity), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.reserveStock(productId, quantity), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.reserveStock(productId, quantity), executor)
            );

            // When
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFutures.get(5, TimeUnit.SECONDS);

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);

            // Then
            // All operations should complete without exceptions
            for (CompletableFuture<StockDto> future : futures) {
                assertNotNull(future.get());
            }

            // Verify that domain service was called for each concurrent operation
            verify(stockDomainService, times(5)).reserveStock(productId, quantity);
        }

        @Test
        @DisplayName("Should handle concurrent stock updates safely")
        void shouldHandleConcurrentStockUpdatesSafely() throws InterruptedException {
            // Given
            String productId = "PROD-123";
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 100, 10, "台北倉庫");

            when(stockDomainService.restockProduct(eq(productId), anyInt())).thenReturn(stock);
            when(stockDomainService.reduceStock(eq(productId), anyInt())).thenReturn(stock);
            when(stockDomainService.isOutOfStock(productId)).thenReturn(false);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            ExecutorService executor = Executors.newFixedThreadPool(6);
            List<CompletableFuture<StockDto>> futures = Arrays.asList(
                CompletableFuture.supplyAsync(() -> stockManagementService.addStock(productId, 10), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.reduceStock(productId, 5), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.addStock(productId, 15), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.reduceStock(productId, 8), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.addStock(productId, 20), executor),
                CompletableFuture.supplyAsync(() -> stockManagementService.reduceStock(productId, 3), executor)
            );

            // When
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFutures.get(5, TimeUnit.SECONDS);

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);

            // Then
            // All operations should complete without exceptions
            for (CompletableFuture<StockDto> future : futures) {
                assertNotNull(future.get());
            }

            // Verify that domain service methods were called
            verify(stockDomainService, times(3)).restockProduct(eq(productId), anyInt());
            verify(stockDomainService, times(3)).reduceStock(eq(productId), anyInt());
        }
    }

    @Nested
    @DisplayName("Threshold Management Tests")
    class ThresholdManagementTests {

        @Test
        @DisplayName("Should update stock thresholds successfully")
        void shouldUpdateStockThresholdsSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer minimumThreshold = 20;
            Integer maximumCapacity = 5000;
            Product product = Product.createIPhone17Pro();
            Stock stock = Stock.create(productId, 100, 10, "台北倉庫");

            when(stockPersistencePort.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(stockPersistencePort.save(stock)).thenReturn(stock);
            when(productPersistencePort.findById(productId)).thenReturn(Optional.of(product));

            // When
            StockDto result = stockManagementService.updateStockThresholds(productId, minimumThreshold, maximumCapacity);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            verify(stockPersistencePort).findByProductId(productId);
            verify(stockPersistencePort).save(stock);
        }

        @Test
        @DisplayName("Should get stock by warehouse location successfully")
        void shouldGetStockByWarehouseLocationSuccessfully() {
            // Given
            String warehouseLocation = "台北倉庫";
            Stock stock1 = Stock.create("PROD-123", 50, 10, warehouseLocation);
            Stock stock2 = Stock.create("PROD-124", 30, 10, warehouseLocation);
            List<Stock> stocks = Arrays.asList(stock1, stock2);
            Product product1 = Product.createIPhone17Pro();
            Product product2 = Product.createIPhone17Pro();

            when(stockPersistencePort.findByWarehouseLocation(warehouseLocation)).thenReturn(stocks);
            when(productPersistencePort.findById("PROD-123")).thenReturn(Optional.of(product1));
            when(productPersistencePort.findById("PROD-124")).thenReturn(Optional.of(product2));

            // When
            List<StockDto> result = stockManagementService.getStockByWarehouse(warehouseLocation);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(stockPersistencePort).findByWarehouseLocation(warehouseLocation);
        }
    }
}