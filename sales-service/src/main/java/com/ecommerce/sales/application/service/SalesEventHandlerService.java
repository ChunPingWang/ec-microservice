package com.ecommerce.sales.application.service;

import com.ecommerce.sales.application.dto.CreateSalesRecordRequest;
import com.ecommerce.sales.application.port.in.SalesRecordUseCase;
import com.ecommerce.sales.domain.model.SalesChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 銷售事件處理服務
 * 遵循 SRP：只負責處理外部事件並轉換為銷售記錄
 */
@Service
public class SalesEventHandlerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesEventHandlerService.class);
    
    private final SalesRecordUseCase salesRecordUseCase;
    
    public SalesEventHandlerService(SalesRecordUseCase salesRecordUseCase) {
        this.salesRecordUseCase = salesRecordUseCase;
    }
    
    /**
     * 處理訂單完成事件，建立銷售記錄
     */
    public void handleOrderCompletedEvent(OrderCompletedEvent event) {
        try {
            logger.info("處理訂單完成事件: {}", event.getOrderId());
            
            // 為每個訂單項目建立銷售記錄
            for (OrderItemEvent item : event.getOrderItems()) {
                CreateSalesRecordRequest request = new CreateSalesRecordRequest(
                    event.getOrderId(),
                    event.getCustomerId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getDiscount(),
                    item.getCategory(),
                    SalesChannel.ONLINE, // 預設為線上通道
                    "台北" // 預設區域
                );
                
                salesRecordUseCase.createSalesRecord(request);
                logger.info("已建立銷售記錄: 訂單={}, 商品={}", event.getOrderId(), item.getProductId());
            }
            
        } catch (Exception e) {
            logger.error("處理訂單完成事件失敗: {}", event.getOrderId(), e);
            // 實際應用中應該有重試機制或死信佇列處理
        }
    }
    
    /**
     * 處理付款完成事件
     */
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        logger.info("收到付款完成事件: 訂單={}, 金額={}", event.getOrderId(), event.getAmount());
        // 付款完成後可能需要更新銷售記錄的狀態或觸發其他業務邏輯
        // 這裡暫時只記錄日誌
    }
    
    // 內部事件類別定義（實際應用中這些應該在共用模組中定義）
    
    public static class OrderCompletedEvent {
        private String orderId;
        private String customerId;
        private BigDecimal totalAmount;
        private java.util.List<OrderItemEvent> orderItems;
        
        // 建構子、getter、setter
        public OrderCompletedEvent() {}
        
        public OrderCompletedEvent(String orderId, String customerId, BigDecimal totalAmount,
                                 java.util.List<OrderItemEvent> orderItems) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.totalAmount = totalAmount;
            this.orderItems = orderItems;
        }
        
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public java.util.List<OrderItemEvent> getOrderItems() { return orderItems; }
        public void setOrderItems(java.util.List<OrderItemEvent> orderItems) { this.orderItems = orderItems; }
    }
    
    public static class OrderItemEvent {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private String category;
        
        // 建構子、getter、setter
        public OrderItemEvent() {}
        
        public OrderItemEvent(String productId, String productName, Integer quantity,
                            BigDecimal unitPrice, BigDecimal discount, String category) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discount = discount;
            this.category = category;
        }
        
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
    
    public static class PaymentCompletedEvent {
        private String orderId;
        private BigDecimal amount;
        private String paymentMethod;
        
        // 建構子、getter、setter
        public PaymentCompletedEvent() {}
        
        public PaymentCompletedEvent(String orderId, BigDecimal amount, String paymentMethod) {
            this.orderId = orderId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
        }
        
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}