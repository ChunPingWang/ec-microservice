package com.ecommerce.product.application.port.in;

import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.dto.ProductSearchRequest;
import com.ecommerce.product.application.dto.ProductSearchResponse;

import java.util.List;

/**
 * Input port for product search operations
 * Follows ISP principle by defining specific search-related operations
 */
public interface ProductSearchUseCase {
    
    /**
     * Search products by keyword
     * @param keyword the search keyword
     * @return list of matching products
     */
    List<ProductDto> searchByKeyword(String keyword);
    
    /**
     * Search products with advanced criteria
     * @param searchRequest the search criteria
     * @return paginated search results
     */
    ProductSearchResponse searchProducts(ProductSearchRequest searchRequest);
    
    /**
     * Get products by category
     * @param category the product category
     * @return list of products in the category
     */
    List<ProductDto> getProductsByCategory(String category);
    
    /**
     * Get products by category with pagination
     * @param category the product category
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated products in the category
     */
    ProductSearchResponse getProductsByCategory(String category, int page, int size);
    
    /**
     * Get product by ID
     * @param productId the product ID
     * @return the product details
     */
    ProductDto getProductById(String productId);
    
    /**
     * Get available products (in stock and status = AVAILABLE)
     * @return list of available products
     */
    List<ProductDto> getAvailableProducts();
    
    /**
     * Get available products with pagination
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated available products
     */
    ProductSearchResponse getAvailableProducts(int page, int size);
    
    /**
     * Get products by brand
     * @param brand the product brand
     * @return list of products from the brand
     */
    List<ProductDto> getProductsByBrand(String brand);
    
    /**
     * Get featured products (e.g., iPhone 17 Pro)
     * @return list of featured products
     */
    List<ProductDto> getFeaturedProducts();
    
    /**
     * Get product suggestions based on category or search history
     * @param category the category for suggestions
     * @param limit the maximum number of suggestions
     * @return list of suggested products
     */
    List<ProductDto> getProductSuggestions(String category, int limit);
}