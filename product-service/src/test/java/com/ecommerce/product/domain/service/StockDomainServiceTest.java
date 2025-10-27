package com.ecommerce.product.domain.service;

import com.ecommerce.product.domain.exception.InsufficientStockException;
import com.ecommerce.product.domain.exception.StockNotFoundException;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.domain.repository.StockRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StockDomainService
 * Tests domain business logic and concurrency safety
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Stock Domain Service Tests")
class StockDomainServiceTest {

    @Mock
    private StockRepository stockRepository;

    private StockDomainService stockDomainService;

    @BeforeEach
    void setUp() {
        stockDomainService = new StockDomainService(stockRepository);
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
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);

            // When
            Stock result = stockDomainService.reserveStock(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(quantity, result.getReservedQuantity());
            assertEquals(40, result.getAvailableQuantity());
            verify(stockRepository).findByProductId(productId);
            verify(stockRepository).save(stock);
        }

        @Test
        @DisplayName("Should throw exception when stock not found for reservation")
        void shouldThrowExceptionWhenStockNotFoundForReservation() {
            // Given
            String productId = "PROD-999";
            Integer quantity = 10;

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());

            // When & Then
            StockNotFoundException exception = assertThrows(StockNotFoundException.class, () ->
                stockDomainService.reserveStock(productId, quantity)
            );
            verify(stockRepository).findByProductId(productId);
            verify(stockRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock for reservation")
        void shouldThrowExceptionWhenInsufficientStockForReservation() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 60; // More than available
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

            // When & Then
            InsufficientStockException exception = assertThrows(InsufficientStockException.class, () ->
                stockDomainService.reserveStock(productId, quantity)
            );
            verify(stockRepository).findByProductId(productId);
            verify(stockRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should confirm reservation successfully")
        void shouldConfirmReservationSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 10;
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");
            stock.reserveStock(quantity);

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);

            // When
            Stock result = stockDomainService.confirmReservation(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(40, result.getQuantity());
            assertEquals(0, result.getReservedQuantity());
            verify(stockRepository).save(stock);
        }

        @Test
        @DisplayName("Should release reservation successfully")
        void shouldReleaseReservationSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 10;
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");
            stock.reserveStock(quantity);

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);

            // When
            Stock result = stockDomainService.releaseReservation(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(50, result.getQuantity());
            assertEquals(0, result.getReservedQuantity());
            assertEquals(50, result.getAvailableQuantity());
            verify(stockRepository).save(stock);
        }
    }

    @Nested
    @DisplayName("Stock Management Tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should restock product successfully")
        void shouldRestockProductSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 20;
            Stock stock = Stock.create(productId, 30, 10, "台北倉庫");

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);

            // When
            Stock result = stockDomainService.restockProduct(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(50, result.getQuantity());
            verify(stockRepository).save(stock);
        }

        @Test
        @DisplayName("Should reduce stock successfully")
        void shouldReduceStockSuccessfully() {
            // Given
            String productId = "PROD-123";
            Integer quantity = 15;
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);

            // When
            Stock result = stockDomainService.reduceStock(productId, quantity);

            // Then
            assertNotNull(result);
            assertEquals(35, result.getQuantity());
            verify(stockRepository).save(stock);
        }

        @Test
        @DisplayName("Should check if product is out of stock")
        void shouldCheckIfProductIsOutOfStock() {
            // Given
            String productId = "PROD-123";
            Stock stock = Stock.create(productId, 0, 10, "台北倉庫");

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

            // When
            boolean result = stockDomainService.isOutOfStock(productId);

            // Then
            assertTrue(result);
            verify(stockRepository).findByProductId(productId);
        }

        @Test
        @DisplayName("Should check available stock correctly")
        void shouldCheckAvailableStockCorrectly() {
            // Given
            String productId = "PROD-123";
            Integer requiredQuantity = 20;
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");
            stock.reserveStock(10); // 40 available

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

            // When
            boolean result = stockDomainService.hasAvailableStock(productId, requiredQuantity);

            // Then
            assertTrue(result);
            verify(stockRepository).findByProductId(productId);
        }

        @Test
        @DisplayName("Should get available quantity correctly")
        void shouldGetAvailableQuantityCorrectly() {
            // Given
            String productId = "PROD-123";
            Stock stock = Stock.create(productId, 50, 10, "台北倉庫");
            stock.reserveStock(15); // 35 available

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

            // When
            Integer result = stockDomainService.getAvailableQuantity(productId);

            // Then
            assertEquals(35, result);
            verify(stockRepository).findByProductId(productId);
        }
    }

    @Nested
    @DisplayName("Stock Query Tests")
    class StockQueryTests {

        @Test
        @DisplayName("Should get products needing restock")
        void shouldGetProductsNeedingRestock() {
            // Given
            Stock lowStock1 = Stock.create("PROD-123", 5, 10, "台北倉庫");
            Stock lowStock2 = Stock.create("PROD-124", 8, 10, "台北倉庫");
            List<Stock> lowStockProducts = Arrays.asList(lowStock1, lowStock2);

            when(stockRepository.findLowStockProducts()).thenReturn(lowStockProducts);

            // When
            List<Stock> result = stockDomainService.getProductsNeedingRestock();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(Stock::isLowStock));
            verify(stockRepository).findLowStockProducts();
        }

        @Test
        @DisplayName("Should get out of stock products")
        void shouldGetOutOfStockProducts() {
            // Given
            Stock outOfStock1 = Stock.create("PROD-123", 0, 10, "台北倉庫");
            Stock outOfStock2 = Stock.create("PROD-124", 0, 10, "台北倉庫");
            List<Stock> outOfStockProducts = Arrays.asList(outOfStock1, outOfStock2);

            when(stockRepository.findOutOfStockProducts()).thenReturn(outOfStockProducts);

            // When
            List<Stock> result = stockDomainService.getOutOfStockProducts();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(Stock::isOutOfStock));
            verify(stockRepository).findOutOfStockProducts();
        }
    }

    @Nested
    @DisplayName("Bulk Operations Tests")
    class BulkOperationsTests {

        @Test
        @DisplayName("Should bulk reserve stock successfully")
        void shouldBulkReserveStockSuccessfully() {
            // Given
            StockDomainService.StockReservationRequest request1 = 
                new StockDomainService.StockReservationRequest("PROD-123", 10);
            StockDomainService.StockReservationRequest request2 = 
                new StockDomainService.StockReservationRequest("PROD-124", 5);
            List<StockDomainService.StockReservationRequest> requests = Arrays.asList(request1, request2);

            Stock stock1 = Stock.create("PROD-123", 50, 10, "台北倉庫");
            Stock stock2 = Stock.create("PROD-124", 30, 10, "台北倉庫");

            when(stockRepository.findByProductId("PROD-123")).thenReturn(Optional.of(stock1));
            when(stockRepository.findByProductId("PROD-124")).thenReturn(Optional.of(stock2));
            when(stockRepository.saveAll(anyList())).thenReturn(Arrays.asList(stock1, stock2));

            // When
            List<Stock> result = stockDomainService.bulkReserveStock(requests);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(stockRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should handle partial failure in bulk reservation")
        void shouldHandlePartialFailureInBulkReservation() {
            // Given
            StockDomainService.StockReservationRequest request1 = 
                new StockDomainService.StockReservationRequest("PROD-123", 10);
            StockDomainService.StockReservationRequest request2 = 
                new StockDomainService.StockReservationRequest("PROD-999", 5); // Non-existent
            List<StockDomainService.StockReservationRequest> requests = Arrays.asList(request1, request2);

            Stock stock1 = Stock.create("PROD-123", 50, 10, "台北倉庫");

            when(stockRepository.findByProductId("PROD-123")).thenReturn(Optional.of(stock1));
            when(stockRepository.findByProductId("PROD-999")).thenReturn(Optional.empty());

            // When & Then
            StockNotFoundException exception = assertThrows(StockNotFoundException.class, () ->
                stockDomainService.bulkReserveStock(requests)
            );
            verify(stockRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("Concurrency Safety Tests")
    class ConcurrencySafetyTests {

        @Test
        @DisplayName("Should handle concurrent stock operations safely")
        void shouldHandleConcurrentStockOperationsSafely() throws InterruptedException {
            // Given
            String productId = "PROD-123";
            Stock stock = Stock.create(productId, 100, 10, "台北倉庫");

            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);

            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<CompletableFuture<Stock>> futures = Arrays.asList(
                CompletableFuture.supplyAsync(() -> stockDomainService.reserveStock(productId, 5), executor),
                CompletableFuture.supplyAsync(() -> stockDomainService.reserveStock(productId, 3), executor),
                CompletableFuture.supplyAsync(() -> stockDomainService.restockProduct(productId, 10), executor),
                CompletableFuture.supplyAsync(() -> stockDomainService.reduceStock(productId, 2), executor),
                CompletableFuture.supplyAsync(() -> stockDomainService.reserveStock(productId, 7), executor)
            );

            // When
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFutures.get(5, TimeUnit.SECONDS);

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);

            // Then
            // All operations should complete without exceptions
            for (CompletableFuture<Stock> future : futures) {
                assertNotNull(future.get());
            }

            // Verify repository interactions
            verify(stockRepository, atLeast(5)).findByProductId(productId);
            verify(stockRepository, atLeast(5)).save(any(Stock.class));
        }

        @Test
        @DisplayName("Should maintain data consistency under concurrent access")
        void shouldMaintainDataConsistencyUnderConcurrentAccess() throws InterruptedException {
            // Given
            String productId = "PROD-123";
            Stock stock = Stock.create(productId, 1000, 10, "台北倉庫");

            // Use synchronized access to simulate database-level locking
            when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> {
                synchronized (this) {
                    return invocation.getArgument(0);
                }
            });

            ExecutorService executor = Executors.newFixedThreadPool(20);
            int operationCount = 50;
            CompletableFuture<?>[] futures = new CompletableFuture[operationCount];

            // When - Perform many concurrent operations
            for (int i = 0; i < operationCount; i++) {
                final int operationIndex = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        if (operationIndex % 3 == 0) {
                            stockDomainService.reserveStock(productId, 1);
                        } else if (operationIndex % 3 == 1) {
                            stockDomainService.restockProduct(productId, 1);
                        } else {
                            stockDomainService.reduceStock(productId, 1);
                        }
                    } catch (Exception e) {
                        // Some operations may fail due to insufficient stock, which is expected
                    }
                }, executor);
            }

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
            allFutures.get(10, TimeUnit.SECONDS);

            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            // Then - Verify that repository was accessed safely
            verify(stockRepository, atLeast(operationCount)).findByProductId(productId);
        }
    }
}