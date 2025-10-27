package com.ecommerce.order.infrastructure.adapter.web;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.order.application.dto.CreateOrderRequest;
import com.ecommerce.order.application.dto.OrderDto;
import com.ecommerce.order.application.port.in.OrderManagementUseCase;
import com.ecommerce.order.domain.model.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單控制器
 * 提供訂單管理的 REST API
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Management", description = "訂單管理 API")
public class OrderController {
    
    private final OrderManagementUseCase orderManagementUseCase;
    
    public OrderController(OrderManagementUseCase orderManagementUseCase) {
        this.orderManagementUseCase = orderManagementUseCase;
    }
    
    @PostMapping("/customers/{customerId}")
    @Operation(summary = "從購物車建立訂單", description = "根據客戶的購物車內容建立新訂單")
    public ResponseEntity<ApiResponse<OrderDto>> createOrderFromCart(
            @Parameter(description = "客戶ID") @PathVariable String customerId,
            @Valid @RequestBody CreateOrderRequest request) {
        
        OrderDto order = orderManagementUseCase.createOrderFromCart(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Order created successfully", order));
    }
    
    @PutMapping("/{orderId}/confirm")
    @Operation(summary = "確認訂單", description = "確認指定的訂單")
    public ResponseEntity<ApiResponse<OrderDto>> confirmOrder(
            @Parameter(description = "訂單ID") @PathVariable String orderId,
            @Parameter(description = "客戶ID") @RequestParam String customerId) {
        
        OrderDto order = orderManagementUseCase.confirmOrder(orderId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Order confirmed successfully", order));
    }
    
    @PutMapping("/{orderId}/pay")
    @Operation(summary = "標記訂單為已付款", description = "標記指定訂單為已付款狀態")
    public ResponseEntity<ApiResponse<OrderDto>> markOrderAsPaid(
            @Parameter(description = "訂單ID") @PathVariable String orderId,
            @Parameter(description = "客戶ID") @RequestParam String customerId,
            @Parameter(description = "付款方式") @RequestParam String paymentMethod) {
        
        OrderDto order = orderManagementUseCase.markOrderAsPaid(orderId, customerId, paymentMethod);
        return ResponseEntity.ok(ApiResponse.success("Order marked as paid successfully", order));
    }
    
    @PutMapping("/{orderId}/ship")
    @Operation(summary = "標記訂單為已出貨", description = "標記指定訂單為已出貨狀態")
    public ResponseEntity<ApiResponse<OrderDto>> shipOrder(
            @Parameter(description = "訂單ID") @PathVariable String orderId) {
        
        OrderDto order = orderManagementUseCase.shipOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order shipped successfully", order));
    }
    
    @PutMapping("/{orderId}/deliver")
    @Operation(summary = "標記訂單為已送達", description = "標記指定訂單為已送達狀態")
    public ResponseEntity<ApiResponse<OrderDto>> deliverOrder(
            @Parameter(description = "訂單ID") @PathVariable String orderId) {
        
        OrderDto order = orderManagementUseCase.deliverOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order delivered successfully", order));
    }
    
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "取消訂單", description = "取消指定的訂單")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @Parameter(description = "訂單ID") @PathVariable String orderId,
            @Parameter(description = "客戶ID") @RequestParam String customerId,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        
        OrderDto order = orderManagementUseCase.cancelOrder(orderId, customerId, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
    
    @PutMapping("/{orderId}/refund")
    @Operation(summary = "退款訂單", description = "對指定訂單進行退款")
    public ResponseEntity<ApiResponse<OrderDto>> refundOrder(
            @Parameter(description = "訂單ID") @PathVariable String orderId,
            @Parameter(description = "退款原因") @RequestParam String reason) {
        
        OrderDto order = orderManagementUseCase.refundOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success("Order refunded successfully", order));
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "獲取訂單詳情", description = "根據訂單ID獲取訂單詳細資訊")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @Parameter(description = "訂單ID") @PathVariable String orderId,
            @Parameter(description = "客戶ID") @RequestParam String customerId) {
        
        OrderDto order = orderManagementUseCase.getOrder(orderId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }
    
    @GetMapping("/customers/{customerId}")
    @Operation(summary = "獲取客戶訂單列表", description = "獲取指定客戶的所有訂單")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getCustomerOrders(
            @Parameter(description = "客戶ID") @PathVariable String customerId) {
        
        List<OrderDto> orders = orderManagementUseCase.getCustomerOrders(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer orders retrieved successfully", orders));
    }
    
    @GetMapping("/customers/{customerId}/status/{status}")
    @Operation(summary = "根據狀態獲取客戶訂單", description = "獲取指定客戶特定狀態的訂單")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getCustomerOrdersByStatus(
            @Parameter(description = "客戶ID") @PathVariable String customerId,
            @Parameter(description = "訂單狀態") @PathVariable OrderStatus status) {
        
        List<OrderDto> orders = orderManagementUseCase.getCustomerOrdersByStatus(customerId, status);
        return ResponseEntity.ok(ApiResponse.success("Customer orders by status retrieved successfully", orders));
    }
    
    @GetMapping("/customers/{customerId}/date-range")
    @Operation(summary = "根據日期範圍獲取客戶訂單", description = "獲取指定客戶在特定日期範圍內的訂單")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getCustomerOrdersByDateRange(
            @Parameter(description = "客戶ID") @PathVariable String customerId,
            @Parameter(description = "開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<OrderDto> orders = orderManagementUseCase.getCustomerOrdersByDateRange(customerId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Customer orders by date range retrieved successfully", orders));
    }
    
    @GetMapping("/customers/{customerId}/latest")
    @Operation(summary = "獲取客戶最新訂單", description = "獲取指定客戶的最新訂單")
    public ResponseEntity<ApiResponse<OrderDto>> getLatestCustomerOrder(
            @Parameter(description = "客戶ID") @PathVariable String customerId) {
        
        OrderDto order = orderManagementUseCase.getLatestCustomerOrder(customerId);
        return ResponseEntity.ok(ApiResponse.success("Latest customer order retrieved successfully", order));
    }
    
    @GetMapping("/{orderId}/can-cancel")
    @Operation(summary = "檢查訂單是否可取消", description = "檢查指定訂單是否可以取消")
    public ResponseEntity<ApiResponse<Boolean>> canCancelOrder(
            @Parameter(description = "訂單ID") @PathVariable String orderId,
            @Parameter(description = "客戶ID") @RequestParam String customerId) {
        
        boolean canCancel = orderManagementUseCase.canCancelOrder(orderId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Order cancellation status checked successfully", canCancel));
    }
    
    @PostMapping("/cleanup/expired")
    @Operation(summary = "清理過期訂單", description = "自動取消超時未付款的訂單")
    public ResponseEntity<ApiResponse<List<OrderDto>>> cancelExpiredOrders(
            @Parameter(description = "超時小時數") @RequestParam(defaultValue = "24") int timeoutHours) {
        
        List<OrderDto> cancelledOrders = orderManagementUseCase.cancelExpiredOrders(timeoutHours);
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Cancelled %d expired orders", cancelledOrders.size()), cancelledOrders));
    }
}