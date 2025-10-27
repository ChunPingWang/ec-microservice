package com.ecommerce.sales.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.sales.domain.model.SalesChannel;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 建立銷售記錄請求 DTO
 * 遵循 SRP：只負責封裝建立銷售記錄的請求資料
 */
public class CreateSalesRecordRequest extends BaseDto {
    
    @NotBlank(message = "訂單ID不能為空")
    private String orderId;
    
    @NotBlank(message = "客戶ID不能為空")
    private String customerId;
    
    @NotBlank(message = "商品ID不能為空")
    private String productId;
    
    @NotBlank(message = "商品名稱不能為空")
    private String productName;
    
    @NotNull(message = "數量不能為空")
    @Positive(message = "數量必須大於0")
    private Integer quantity;
    
    @NotNull(message = "單價不能為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "單價必須大於0")
    private BigDecimal unitPrice;
    
    @DecimalMin(value = "0.0", message = "折扣不能為負數")
    private BigDecimal discount;
    
    @NotBlank(message = "商品分類不能為空")
    private String category;
    
    @NotNull(message = "銷售通道不能為空")
    private SalesChannel channel;
    
    @NotBlank(message = "銷售區域不能為空")
    private String region;
    
    // 預設建構子
    public CreateSalesRecordRequest() {}
    
    // 全參數建構子
    public CreateSalesRecordRequest(String orderId, String customerId, String productId,
                                  String productName, Integer quantity, BigDecimal unitPrice,
                                  BigDecimal discount, String category, SalesChannel channel,
                                  String region) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.category = category;
        this.channel = channel;
        this.region = region;
    }
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
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
    
    public SalesChannel getChannel() { return channel; }
    public void setChannel(SalesChannel channel) { this.channel = channel; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    @Override
    public String toString() {
        return "CreateSalesRecordRequest{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", discount=" + discount +
                ", category='" + category + '\'' +
                ", channel=" + channel +
                ", region='" + region + '\'' +
                '}';
    }
}