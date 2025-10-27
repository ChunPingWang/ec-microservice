package com.ecommerce.order.infrastructure.adapter.external;

import com.ecommerce.common.architecture.ExternalAdapter;
import com.ecommerce.order.application.port.out.ProductServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

/**
 * 商品服務適配器
 * 與商品微服務進行整合
 */
@ExternalAdapter
@Component
public class ProductServiceAdapter implements ProductServicePort {
    
    private final RestTemplate restTemplate;
    private final String productServiceBaseUrl;
    
    public ProductServiceAdapter(RestTemplate restTemplate,
                               @Value("${services.product-service.base-url:http://localhost:8082}") String productServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.productServiceBaseUrl = productServiceBaseUrl;
    }
    
    @Override
    public boolean isProductAvailable(String productId) {
        try {
            String url = productServiceBaseUrl + "/api/v1/products/" + productId;
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(url, ProductResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ProductResponse productResponse = response.getBody();
                return productResponse.getData() != null && productResponse.getData().isAvailable();
            }
            
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            // Log error and return false for safety
            System.err.println("Error checking product availability: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean hasAvailableStock(String productId, Integer requiredQuantity) {
        try {
            String url = productServiceBaseUrl + "/api/v1/stock/" + productId + "/available";
            ResponseEntity<StockResponse> response = restTemplate.getForEntity(url, StockResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                StockResponse stockResponse = response.getBody();
                return stockResponse.getData() != null && stockResponse.getData() >= requiredQuantity;
            }
            
            return false;
        } catch (Exception e) {
            // Log error and return false for safety
            System.err.println("Error checking stock availability: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public ProductInfo getProductInfo(String productId) {
        try {
            String url = productServiceBaseUrl + "/api/v1/products/" + productId;
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(url, ProductResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ProductResponse productResponse = response.getBody();
                ProductData productData = productResponse.getData();
                
                if (productData != null) {
                    return new ProductInfo(
                        productData.getId(),
                        productData.getName(),
                        productData.getDescription(),
                        productData.isAvailable(),
                        productData.getStockQuantity()
                    );
                }
            }
            
            return null;
        } catch (Exception e) {
            // Log error and return null
            System.err.println("Error getting product info: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public void reserveStock(String productId, Integer quantity) {
        try {
            String url = productServiceBaseUrl + "/api/v1/stock/" + productId + "/reserve";
            StockReservationRequest request = new StockReservationRequest(quantity);
            
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to reserve stock for product: " + productId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reserving stock: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void confirmStockReservation(String productId, Integer quantity) {
        try {
            String url = productServiceBaseUrl + "/api/v1/stock/" + productId + "/confirm";
            StockReservationRequest request = new StockReservationRequest(quantity);
            
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to confirm stock reservation for product: " + productId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error confirming stock reservation: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void releaseStockReservation(String productId, Integer quantity) {
        try {
            String url = productServiceBaseUrl + "/api/v1/stock/" + productId + "/release";
            StockReservationRequest request = new StockReservationRequest(quantity);
            
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to release stock reservation for product: " + productId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error releasing stock reservation: " + e.getMessage(), e);
        }
    }
    
    // Internal DTOs for API communication
    private static class ProductResponse {
        private boolean success;
        private ProductData data;
        private String message;
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public ProductData getData() { return data; }
        public void setData(ProductData data) { this.data = data; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    private static class ProductData {
        private String id;
        private String name;
        private String description;
        private BigDecimal price;
        private String brand;
        private String model;
        private String specifications;
        private boolean available;
        private Integer stockQuantity;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
        
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    }
    
    private static class StockResponse {
        private boolean success;
        private Integer data;
        private String message;
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Integer getData() { return data; }
        public void setData(Integer data) { this.data = data; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    private static class StockReservationRequest {
        private Integer quantity;
        
        public StockReservationRequest() {}
        
        public StockReservationRequest(Integer quantity) {
            this.quantity = quantity;
        }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}