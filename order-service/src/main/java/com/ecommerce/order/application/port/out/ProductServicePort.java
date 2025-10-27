package com.ecommerce.order.application.port.out;

/**
 * 商品服務端口
 * 定義與商品服務互動的介面
 */
public interface ProductServicePort {
    
    /**
     * 檢查商品是否可用
     */
    boolean isProductAvailable(String productId);
    
    /**
     * 檢查是否有足夠的庫存
     */
    boolean hasAvailableStock(String productId, Integer quantity);
    
    /**
     * 預留庫存
     */
    void reserveStock(String productId, Integer quantity);
    
    /**
     * 確認庫存預留（扣減庫存）
     */
    void confirmStockReservation(String productId, Integer quantity);
    
    /**
     * 釋放庫存預留
     */
    void releaseStockReservation(String productId, Integer quantity);
    
    /**
     * 取得商品資訊
     */
    ProductInfo getProductInfo(String productId);
    
    /**
     * 商品資訊
     */
    class ProductInfo {
        private String productId;
        private String productName;
        private String description;
        private boolean available;
        private Integer availableStock;
        
        // Constructors
        public ProductInfo() {}
        
        public ProductInfo(String productId, String productName, String description, 
                          boolean available, Integer availableStock) {
            this.productId = productId;
            this.productName = productName;
            this.description = description;
            this.available = available;
            this.availableStock = availableStock;
        }
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public Integer getAvailableStock() { return availableStock; }
        public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    }
}