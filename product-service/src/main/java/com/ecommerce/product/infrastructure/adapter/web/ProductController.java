package com.ecommerce.product.infrastructure.adapter.web;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.dto.ProductSearchRequest;
import com.ecommerce.product.application.dto.ProductSearchResponse;
import com.ecommerce.product.application.port.in.ProductSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product REST Controller
 * Follows SRP principle by handling only product-related HTTP requests
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "APIs for product search and management")
public class ProductController {
    
    private final ProductSearchUseCase productSearchUseCase;
    
    public ProductController(ProductSearchUseCase productSearchUseCase) {
        this.productSearchUseCase = productSearchUseCase;
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products by keyword", description = "Search products using keyword with optional filters")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        
        List<ProductDto> products = productSearchUseCase.searchByKeyword(keyword);
        
        return ResponseEntity.ok(ApiResponse.success(products, "Products found successfully"));
    }
    
    @PostMapping("/search")
    @Operation(summary = "Advanced product search", description = "Search products with advanced criteria and pagination")
    public ResponseEntity<ApiResponse<ProductSearchResponse>> advancedSearch(
            @RequestBody ProductSearchRequest searchRequest) {
        
        ProductSearchResponse response = productSearchUseCase.searchProducts(searchRequest);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Search completed successfully"));
    }
    
    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieve detailed product information by product ID")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(
            @Parameter(description = "Product ID") @PathVariable String productId) {
        
        ProductDto product = productSearchUseCase.getProductById(productId);
        
        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieve products in a specific category")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByCategory(
            @Parameter(description = "Product category") @PathVariable String category) {
        
        List<ProductDto> products = productSearchUseCase.getProductsByCategory(category);
        
        return ResponseEntity.ok(ApiResponse.success(products, "Products in category retrieved successfully"));
    }
    
    @GetMapping("/category/{category}/paginated")
    @Operation(summary = "Get products by category with pagination", description = "Retrieve paginated products in a specific category")
    public ResponseEntity<ApiResponse<ProductSearchResponse>> getProductsByCategoryPaginated(
            @Parameter(description = "Product category") @PathVariable String category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        ProductSearchResponse response = productSearchUseCase.getProductsByCategory(category, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Paginated products in category retrieved successfully"));
    }
    
    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get products by brand", description = "Retrieve products from a specific brand")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByBrand(
            @Parameter(description = "Product brand") @PathVariable String brand) {
        
        List<ProductDto> products = productSearchUseCase.getProductsByBrand(brand);
        
        return ResponseEntity.ok(ApiResponse.success(products, "Products by brand retrieved successfully"));
    }
    
    @GetMapping("/available")
    @Operation(summary = "Get available products", description = "Retrieve all products that are available and in stock")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAvailableProducts() {
        
        List<ProductDto> products = productSearchUseCase.getAvailableProducts();
        
        return ResponseEntity.ok(ApiResponse.success(products, "Available products retrieved successfully"));
    }
    
    @GetMapping("/available/paginated")
    @Operation(summary = "Get available products with pagination", description = "Retrieve paginated available products")
    public ResponseEntity<ApiResponse<ProductSearchResponse>> getAvailableProductsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        ProductSearchResponse response = productSearchUseCase.getAvailableProducts(page, size);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Paginated available products retrieved successfully"));
    }
    
    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Retrieve featured products like iPhone 17 Pro")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getFeaturedProducts() {
        
        List<ProductDto> products = productSearchUseCase.getFeaturedProducts();
        
        return ResponseEntity.ok(ApiResponse.success(products, "Featured products retrieved successfully"));
    }
    
    @GetMapping("/suggestions")
    @Operation(summary = "Get product suggestions", description = "Get product suggestions based on category")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductSuggestions(
            @Parameter(description = "Category for suggestions") @RequestParam(required = false) String category,
            @Parameter(description = "Maximum number of suggestions") @RequestParam(defaultValue = "10") int limit) {
        
        List<ProductDto> products = productSearchUseCase.getProductSuggestions(category, limit);
        
        return ResponseEntity.ok(ApiResponse.success(products, "Product suggestions retrieved successfully"));
    }
    
    @GetMapping("/iphone-17-pro")
    @Operation(summary = "Get iPhone 17 Pro", description = "Retrieve iPhone 17 Pro product information")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getIPhone17Pro() {
        
        List<ProductDto> products = productSearchUseCase.getProductsByBrand("Apple")
            .stream()
            .filter(product -> product.getModel().contains("iPhone 17 Pro"))
            .toList();
        
        return ResponseEntity.ok(ApiResponse.success(products, "iPhone 17 Pro retrieved successfully"));
    }
}