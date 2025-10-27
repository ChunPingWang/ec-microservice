package com.ecommerce.sales.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.sales.domain.model.SalesChannel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 銷售記錄 DTO
 * 遵循 SRP：只負責封裝銷售記錄的資料傳輸
 */
public class SalesRecordDto extends BaseDto {
    
    private String salesRecordId;
    private String orderId;
    private String customerId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private String category;
    private LocalDateTime saleDate;
    private SalesChannel channel;
    private String region;
    private boolean isHighValueSale;
    private boolean isPromotionalSale;
    private BigDecimal discountRate;
    
    // 預設建構子
    public SalesRecordDto() {}
    
    // 全參數建構子
    public SalesRecordDto(String salesRecordId, String orderId, String customerId,
                         String productId, String productName, Integer quantity,
                         BigDecimal unitPrice, BigDecimal totalAmount, BigDecimal discount,
                         String category, LocalDateTime saleDate, SalesChannel channel,
                         String region, boolean isHighValueSale, boolean isPromotionalSale,
                         BigDecimal discountRate) {
        this.salesRecordId = salesRecordId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.category = category;
        this.saleDate = saleDate;
        this.channel = channel;
        this.region = region;
        this.isHighValueSale = isHighValueSale;
        this.isPromotionalSale = isPromotionalSale;
        this.discountRate = discountRate;
    }
    
    // Getters and Setters
    public String getSalesRecordId() { return salesRecordId; }
    public void setSalesRecordId(String salesRecordId) { this.salesRecordId = salesRecordId; }
    
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
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    
    public SalesChannel getChannel() { return channel; }
    public void setChannel(SalesChannel channel) { this.channel = channel; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public boolean isHighValueSale() { return isHighValueSale; }
    public void setHighValueSale(boolean highValueSale) { isHighValueSale = highValueSale; }
    
    public boolean isPromotionalSale() { return isPromotionalSale; }
    public void setPromotionalSale(boolean promotionalSale) { isPromotionalSale = promotionalSale; }
    
    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
    
    @Override
    public String toString() {
        return "SalesRecordDto{" +
                "salesRecordId='" + salesRecordId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                ", saleDate=" + saleDate +
                ", channel=" + channel +
                ", isHighValueSale=" + isHighValueSale +
                '}';
    }
}