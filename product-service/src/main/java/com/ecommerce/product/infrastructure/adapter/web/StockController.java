package com.ecommerce.product.infrastructure.adapter.web;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.application.dto.StockDto;
import com.ecommerce.product.application.dto.StockReservationRequest;
import com.ecommerce.product.application.dto.StockUpdateRequest;
import com.ecommerce.product.application.port.in.StockManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Stock REST Controller
 * Follows SRP principle by handling only stock-related HTTP requests
 */
@RestController
@RequestMapping("/api/v1/stock")
@Tag(name = "Stock Management", description = "APIs for stock management and inventory operations")
public class StockController {
    
    private final StockManagementUseCase stockManagementUseCase;
    
    public StockController(StockManagementUseCase stockManagementUseCase) {
        this.stockManagementUseCase = stockManagementUseCase;
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock by product ID", description = "Retrieve stock information for a specific product")
    public ResponseEntity<ApiResponse<StockDto>> getStockByProductId(
            @Parameter(description = "Product ID") @PathVariable String productId) {
        
        StockDto stock = stockManagementUseCase.getStockByProductId(productId);
        
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock information retrieved successfully"));
    }
    
    @GetMapping("/product/{productId}/available")
    @Operation(summary = "Get available quantity", description = "Get available quantity for a specific product")
    public ResponseEntity<ApiResponse<Integer>> getAvailableQuantity(
            @Parameter(description = "Product ID") @PathVariable String productId) {
        
        Integer availableQuantity = stockManagementUseCase.getAvailableQuantity(productId);
        
        return ResponseEntity.ok(ApiResponse.success(availableQuantity, "Available quantity retrieved successfully"));
    }
    
    @GetMapping("/product/{productId}/check")
    @Operation(summary = "Check stock availability", description = "Check if sufficient stock is available for a product")
    public ResponseEntity<ApiResponse<Boolean>> checkStockAvailability(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Required quantity") @RequestParam Integer quantity) {
        
        boolean hasStock = stockManagementUseCase.hasAvailableStock(productId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success(hasStock, "Stock availability checked successfully"));
    }
    
    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock", description = "Reserve stock for a specific product")
    public ResponseEntity<ApiResponse<StockDto>> reserveStock(
            @Parameter(description = "Product ID") @RequestParam String productId,
            @Parameter(description = "Quantity to reserve") @RequestParam Integer quantity) {
        
        StockDto stock = stockManagementUseCase.reserveStock(productId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock reserved successfully"));
    }
    
    @PostMapping("/confirm-reservation")
    @Operation(summary = "Confirm stock reservation", description = "Confirm a stock reservation (convert to sale)")
    public ResponseEntity<ApiResponse<StockDto>> confirmReservation(
            @Parameter(description = "Product ID") @RequestParam String productId,
            @Parameter(description = "Quantity to confirm") @RequestParam Integer quantity) {
        
        StockDto stock = stockManagementUseCase.confirmReservation(productId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock reservation confirmed successfully"));
    }
    
    @PostMapping("/release-reservation")
    @Operation(summary = "Release stock reservation", description = "Release a stock reservation (cancel reservation)")
    public ResponseEntity<ApiResponse<StockDto>> releaseReservation(
            @Parameter(description = "Product ID") @RequestParam String productId,
            @Parameter(description = "Quantity to release") @RequestParam Integer quantity) {
        
        StockDto stock = stockManagementUseCase.releaseReservation(productId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock reservation released successfully"));
    }
    
    @PostMapping("/add")
    @Operation(summary = "Add stock", description = "Add stock to a product (restock operation)")
    public ResponseEntity<ApiResponse<StockDto>> addStock(
            @Parameter(description = "Product ID") @RequestParam String productId,
            @Parameter(description = "Quantity to add") @RequestParam Integer quantity) {
        
        StockDto stock = stockManagementUseCase.addStock(productId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock added successfully"));
    }
    
    @PostMapping("/reduce")
    @Operation(summary = "Reduce stock", description = "Reduce stock for a product (direct sale)")
    public ResponseEntity<ApiResponse<StockDto>> reduceStock(
            @Parameter(description = "Product ID") @RequestParam String productId,
            @Parameter(description = "Quantity to reduce") @RequestParam Integer quantity) {
        
        StockDto stock = stockManagementUseCase.reduceStock(productId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock reduced successfully"));
    }
    
    @PutMapping("/thresholds")
    @Operation(summary = "Update stock thresholds", description = "Update minimum and maximum thresholds for a product")
    public ResponseEntity<ApiResponse<StockDto>> updateStockThresholds(
            @Parameter(description = "Product ID") @RequestParam String productId,
            @Parameter(description = "Minimum threshold") @RequestParam Integer minimumThreshold,
            @Parameter(description = "Maximum capacity") @RequestParam Integer maximumCapacity) {
        
        StockDto stock = stockManagementUseCase.updateStockThresholds(productId, minimumThreshold, maximumCapacity);
        
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock thresholds updated successfully"));
    }
    
    @PostMapping("/bulk-reserve")
    @Operation(summary = "Bulk reserve stock", description = "Reserve stock for multiple products in a single operation")
    public ResponseEntity<ApiResponse<List<StockDto>>> bulkReserveStock(
            @RequestBody List<StockReservationRequest> reservationRequests) {
        
        List<StockDto> stocks = stockManagementUseCase.bulkReserveStock(reservationRequests);
        
        return ResponseEntity.ok(ApiResponse.success(stocks, "Bulk stock reservation completed successfully"));
    }
    
    @PutMapping("/update")
    @Operation(summary = "Update stock", description = "Update stock with specific operation type")
    public ResponseEntity<ApiResponse<StockDto>> updateStock(
            @RequestBody StockUpdateRequest stockUpdateRequest) {
        
        StockDto stock = stockManagementUseCase.updateStock(stockUpdateRequest);
        
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock updated successfully"));
    }
    
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieve products with low stock levels")
    public ResponseEntity<ApiResponse<List<StockDto>>> getLowStockProducts() {
        
        List<StockDto> stocks = stockManagementUseCase.getLowStockProducts();
        
        return ResponseEntity.ok(ApiResponse.success(stocks, "Low stock products retrieved successfully"));
    }
    
    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock products", description = "Retrieve products that are out of stock")
    public ResponseEntity<ApiResponse<List<StockDto>>> getOutOfStockProducts() {
        
        List<StockDto> stocks = stockManagementUseCase.getOutOfStockProducts();
        
        return ResponseEntity.ok(ApiResponse.success(stocks, "Out of stock products retrieved successfully"));
    }
    
    @GetMapping("/warehouse/{warehouseLocation}")
    @Operation(summary = "Get stock by warehouse", description = "Retrieve stock information for a specific warehouse")
    public ResponseEntity<ApiResponse<List<StockDto>>> getStockByWarehouse(
            @Parameter(description = "Warehouse location") @PathVariable String warehouseLocation) {
        
        List<StockDto> stocks = stockManagementUseCase.getStockByWarehouse(warehouseLocation);
        
        return ResponseEntity.ok(ApiResponse.success(stocks, "Warehouse stock retrieved successfully"));
    }
}