package com.ecommerce.product.application.usecase;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.dto.ProductSearchRequest;
import com.ecommerce.product.application.dto.ProductSearchResponse;
import com.ecommerce.product.application.mapper.ProductMapper;
import com.ecommerce.product.application.port.in.ProductSearchUseCase;
import com.ecommerce.product.application.port.out.ProductPersistencePort;
import com.ecommerce.product.application.port.out.StockPersistencePort;
import com.ecommerce.product.domain.exception.ProductNotFoundException;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.domain.service.ProductDomainService;

import java.util.List;
import java.util.Optional;

/**
 * Product search use case implementation
 * Follows SRP principle by handling only product search operations
 */
@UseCase
public class ProductSearchService implements ProductSearchUseCase {
    
    private final ProductPersistencePort productPersistencePort;
    private final StockPersistencePort stockPersistencePort;
    private final ProductDomainService productDomainService;
    
    public ProductSearchService(ProductPersistencePort productPersistencePort,
                              StockPersistencePort stockPersistencePort,
                              ProductDomainService productDomainService) {
        this.productPersistencePort = productPersistencePort;
        this.stockPersistencePort = stockPersistencePort;
        this.productDomainService = productDomainService;
    }
    
    @Override
    public List<ProductDto> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        List<ProductDomainService.ProductWithStock> productsWithStock = 
            productDomainService.searchProductsWithStock(keyword.trim());
        
        return ProductMapper.toDtoListFromProductWithStock(productsWithStock);
    }
    
    @Override
    public ProductSearchResponse searchProducts(ProductSearchRequest searchRequest) {
        validateSearchRequest(searchRequest);
        
        List<Product> products;
        long totalCount;
        
        if (searchRequest.hasKeyword()) {
            products = productPersistencePort.searchByKeyword(
                searchRequest.getKeyword(), 
                searchRequest.getPage(), 
                searchRequest.getSize()
            );
            totalCount = productPersistencePort.countSearchResults(searchRequest.getKeyword());
        } else if (searchRequest.hasCategory()) {
            products = productPersistencePort.findByCategory(
                searchRequest.getCategory(), 
                searchRequest.getPage(), 
                searchRequest.getSize()
            );
            totalCount = productPersistencePort.countByCategory(searchRequest.getCategory());
        } else if (searchRequest.hasBrand()) {
            products = productPersistencePort.findByBrand(searchRequest.getBrand());
            totalCount = products.size();
        } else {
            products = productPersistencePort.findByStatus(
                ProductStatus.AVAILABLE, 
                searchRequest.getPage(), 
                searchRequest.getSize()
            );
            totalCount = productPersistencePort.countByStatus(ProductStatus.AVAILABLE);
        }
        
        // Enrich with stock information
        List<ProductDto> productDtos = enrichProductsWithStock(products);
        
        // Filter by stock availability if requested
        if (Boolean.TRUE.equals(searchRequest.getInStockOnly())) {
            productDtos = productDtos.stream()
                .filter(ProductDto::isInStock)
                .toList();
        }
        
        return new ProductSearchResponse(
            productDtos,
            searchRequest.getPage(),
            searchRequest.getSize(),
            totalCount,
            searchRequest.getSortBy(),
            searchRequest.getSortDirection()
        );
    }
    
    @Override
    public List<ProductDto> getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return List.of();
        }
        
        List<ProductDomainService.ProductWithStock> productsWithStock = 
            productDomainService.getProductsByCategoryWithStock(category.trim());
        
        return ProductMapper.toDtoListFromProductWithStock(productsWithStock);
    }
    
    @Override
    public ProductSearchResponse getProductsByCategory(String category, int page, int size) {
        if (category == null || category.trim().isEmpty()) {
            return new ProductSearchResponse(List.of(), page, size, 0);
        }
        
        List<Product> products = productPersistencePort.findByCategory(category.trim(), page, size);
        long totalCount = productPersistencePort.countByCategory(category.trim());
        
        List<ProductDto> productDtos = enrichProductsWithStock(products);
        
        return new ProductSearchResponse(productDtos, page, size, totalCount);
    }
    
    @Override
    public ProductDto getProductById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new ProductNotFoundException(productId);
        }
        
        Product product = productPersistencePort.findById(productId.trim())
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        Optional<Stock> stock = stockPersistencePort.findByProductId(productId.trim());
        
        return ProductMapper.toDto(product, stock.orElse(null));
    }
    
    @Override
    public List<ProductDto> getAvailableProducts() {
        List<ProductDomainService.ProductWithStock> productsWithStock = 
            productDomainService.getAvailableProductsWithStock();
        
        return ProductMapper.toDtoListFromProductWithStock(productsWithStock);
    }
    
    @Override
    public ProductSearchResponse getAvailableProducts(int page, int size) {
        List<Product> products = productPersistencePort.findByStatus(ProductStatus.AVAILABLE, page, size);
        long totalCount = productPersistencePort.countByStatus(ProductStatus.AVAILABLE);
        
        List<ProductDto> productDtos = enrichProductsWithStock(products)
            .stream()
            .filter(ProductDto::isInStock)
            .toList();
        
        return new ProductSearchResponse(productDtos, page, size, totalCount);
    }
    
    @Override
    public List<ProductDto> getProductsByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return List.of();
        }
        
        List<Product> products = productPersistencePort.findByBrand(brand.trim());
        return enrichProductsWithStock(products);
    }
    
    @Override
    public List<ProductDto> getFeaturedProducts() {
        // Get iPhone 17 Pro and other featured products
        List<Product> iPhoneProducts = productPersistencePort.findByBrandAndModel("Apple", "iPhone 17 Pro");
        
        // Add other featured products logic here if needed
        List<Product> featuredProducts = iPhoneProducts;
        
        return enrichProductsWithStock(featuredProducts);
    }
    
    @Override
    public List<ProductDto> getProductSuggestions(String category, int limit) {
        if (category == null || category.trim().isEmpty()) {
            // Return general suggestions (e.g., featured products)
            return getFeaturedProducts().stream()
                .limit(limit)
                .toList();
        }
        
        List<Product> products = productPersistencePort.findByCategory(category.trim(), 0, limit);
        return enrichProductsWithStock(products);
    }
    
    // Private helper methods
    private void validateSearchRequest(ProductSearchRequest searchRequest) {
        if (searchRequest == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }
        
        if (!searchRequest.isValidPage()) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        
        if (!searchRequest.isValidPageSize()) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        if (!searchRequest.isValidPriceRange()) {
            throw new IllegalArgumentException("Invalid price range: min price cannot be greater than max price");
        }
    }
    
    private List<ProductDto> enrichProductsWithStock(List<Product> products) {
        return products.stream()
            .map(product -> {
                Optional<Stock> stock = stockPersistencePort.findByProductId(product.getProductId());
                return ProductMapper.toDto(product, stock.orElse(null));
            })
            .toList();
    }
}