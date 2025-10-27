package com.ecommerce.order.infrastructure.adapter.web;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.order.application.dto.AddToCartRequest;
import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.dto.UpdateCartItemRequest;
import com.ecommerce.order.application.port.in.CartUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 購物車控制器
 * 提供購物車管理的 REST API
 */
@RestController
@RequestMapping("/api/v1/carts")
@Tag(name = "Cart Management", description = "購物車管理 API")
public class CartController {
    
    private final CartUseCase cartUseCase;
    
    public CartController(CartUseCase cartUseCase) {
        this.cartUseCase = cartUseCase;
    }
    
    @GetMapping("/customers/{customerId}")
    @Operation(summary = "獲取購物車", description = "獲取指定客戶的購物車")
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @Parameter(description = "客戶ID") @PathVariable String customerId) {
        
        CartDto cart = cartUseCase.getOrCreateCart(customerId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }
    
    @PostMapping("/customers/{customerId}/items")
    @Operation(summary = "加入商品到購物車", description = "將商品加入到指定客戶的購物車")
    public ResponseEntity<ApiResponse<CartDto>> addToCart(
            @Parameter(description = "客戶ID") @PathVariable String customerId,
            @Valid @RequestBody AddToCartRequest request) {
        
        CartDto cart = cartUseCase.addToCart(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Item added to cart successfully", cart));
    }
    
    @PutMapping("/customers/{customerId}/items/{productId}")
    @Operation(summary = "更新購物車項目", description = "更新購物車中指定商品的數量")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItem(
            @Parameter(description = "客戶ID") @PathVariable String customerId,
            @Parameter(description = "商品ID") @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        
        CartDto cart = cartUseCase.updateCartItem(customerId, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }
    
    @DeleteMapping("/customers/{customerId}/items/{productId}")
    @Operation(summary = "從購物車移除商品", description = "從購物車中移除指定商品")
    public ResponseEntity<ApiResponse<CartDto>> removeFromCart(
            @Parameter(description = "客戶ID") @PathVariable String customerId,
            @Parameter(description = "商品ID") @PathVariable String productId) {
        
        CartDto cart = cartUseCase.removeFromCart(customerId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }
    
    @DeleteMapping("/customers/{customerId}")
    @Operation(summary = "清空購物車", description = "清空指定客戶的購物車")
    public ResponseEntity<ApiResponse<CartDto>> clearCart(
            @Parameter(description = "客戶ID") @PathVariable String customerId) {
        
        CartDto cart = cartUseCase.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", cart));
    }
    
    @GetMapping("/customers/{customerId}/contains/{productId}")
    @Operation(summary = "檢查購物車是否包含商品", description = "檢查購物車是否包含指定商品")
    public ResponseEntity<ApiResponse<Boolean>> cartContainsProduct(
            @Parameter(description = "客戶ID") @PathVariable String customerId,
            @Parameter(description = "商品ID") @PathVariable String productId) {
        
        boolean contains = cartUseCase.cartContainsProduct(customerId, productId);
        return ResponseEntity.ok(ApiResponse.success("Cart product check completed successfully", contains));
    }
    
    @PutMapping("/customers/{customerId}/extend-expiry")
    @Operation(summary = "延長購物車有效期", description = "延長購物車的有效期限")
    public ResponseEntity<ApiResponse<CartDto>> extendCartExpiry(
            @Parameter(description = "客戶ID") @PathVariable String customerId,
            @Parameter(description = "延長天數") @RequestParam(defaultValue = "7") int days) {
        
        CartDto cart = cartUseCase.extendCartExpiry(customerId, days);
        return ResponseEntity.ok(ApiResponse.success("Cart expiry extended successfully", cart));
    }
    
    @PostMapping("/merge")
    @Operation(summary = "合併購物車", description = "將來源購物車合併到目標客戶的購物車")
    public ResponseEntity<ApiResponse<CartDto>> mergeCarts(
            @Parameter(description = "目標客戶ID") @RequestParam String targetCustomerId,
            @Parameter(description = "來源購物車ID") @RequestParam String sourceCartId) {
        
        CartDto cart = cartUseCase.mergeCarts(targetCustomerId, sourceCartId);
        return ResponseEntity.ok(ApiResponse.success("Carts merged successfully", cart));
    }
    
    @PostMapping("/customers/{customerId}/validate-for-order")
    @Operation(summary = "驗證購物車是否可建立訂單", description = "驗證購物車內容是否符合建立訂單的條件")
    public ResponseEntity<ApiResponse<String>> validateCartForOrder(
            @Parameter(description = "客戶ID") @PathVariable String customerId) {
        
        cartUseCase.validateCartForOrder(customerId);
        return ResponseEntity.ok(ApiResponse.success("Cart is valid for order creation", "valid"));
    }
    
    @PostMapping("/cleanup/expired")
    @Operation(summary = "清理過期購物車", description = "清理所有過期的購物車")
    public ResponseEntity<ApiResponse<Integer>> cleanupExpiredCarts() {
        
        int cleanedCount = cartUseCase.cleanupExpiredCarts();
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Cleaned up %d expired carts", cleanedCount), cleanedCount));
    }
    
    @PostMapping("/cleanup/abandoned")
    @Operation(summary = "清理長時間未更新的購物車", description = "清理長時間未更新的購物車")
    public ResponseEntity<ApiResponse<Integer>> cleanupAbandonedCarts(
            @Parameter(description = "天數閾值") @RequestParam(defaultValue = "30") int daysThreshold) {
        
        int cleanedCount = cartUseCase.cleanupAbandonedCarts(daysThreshold);
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Cleaned up %d abandoned carts", cleanedCount), cleanedCount));
    }
}