package com.ecommerce.sales.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 銷售記錄實體 - 記錄每筆銷售交易的詳細資訊
 * 遵循 SRP：只負責銷售記錄的業務邏輯
 */
public class SalesRecord extends BaseEntity {
    
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
    
    // 私有建構子，強制使用工廠方法
    private SalesRecord() {}
    
    /**
     * 建立銷售記錄的工廠方法
     * 包含業務規則驗證
     */
    public static SalesRecord create(String salesRecordId, String orderId, String customerId,
                                   String productId, String productName, Integer quantity,
                                   BigDecimal unitPrice, BigDecimal discount, String category,
                                   SalesChannel channel, String region) {
        validateSalesRecordData(salesRecordId, orderId, customerId, productId, 
                              productName, quantity, unitPrice, discount, category, channel, region);
        
        SalesRecord record = new SalesRecord();
        record.salesRecordId = salesRecordId;
        record.orderId = orderId;
        record.customerId = customerId;
        record.productId = productId;
        record.productName = productName;
        record.quantity = quantity;
        record.unitPrice = unitPrice;
        record.discount = discount != null ? discount : BigDecimal.ZERO;
        record.category = category;
        record.channel = channel;
        record.region = region;
        record.saleDate = LocalDateTime.now();
        record.totalAmount = calculateTotalAmount(quantity, unitPrice, record.discount);
        
        return record;
    }
    
    /**
     * 計算總金額的業務邏輯
     */
    private static BigDecimal calculateTotalAmount(Integer quantity, BigDecimal unitPrice, BigDecimal discount) {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return subtotal.subtract(discount);
    }
    
    /**
     * 驗證銷售記錄資料的業務規則
     */
    private static void validateSalesRecordData(String salesRecordId, String orderId, String customerId,
                                              String productId, String productName, Integer quantity,
                                              BigDecimal unitPrice, BigDecimal discount, String category,
                                              SalesChannel channel, String region) {
        if (salesRecordId == null || salesRecordId.trim().isEmpty()) {
            throw new IllegalArgumentException("銷售記錄ID不能為空");
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("訂單ID不能為空");
        }
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("客戶ID不能為空");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("商品ID不能為空");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("商品名稱不能為空");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("數量必須大於0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("單價不能為負數");
        }
        if (discount != null && discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("折扣不能為負數");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("商品分類不能為空");
        }
        if (channel == null) {
            throw new IllegalArgumentException("銷售通道不能為空");
        }
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("銷售區域不能為空");
        }
    }
    
    /**
     * 檢查是否為高價值銷售（業務規則）
     */
    public boolean isHighValueSale() {
        return totalAmount.compareTo(new BigDecimal("10000")) >= 0;
    }
    
    /**
     * 檢查是否為促銷銷售
     */
    public boolean isPromotionalSale() {
        return discount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 計算折扣率
     */
    public BigDecimal getDiscountRate() {
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return discount.divide(subtotal, 4, BigDecimal.ROUND_HALF_UP);
    }
    
    // Getters
    public String getSalesRecordId() { return salesRecordId; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getDiscount() { return discount; }
    public String getCategory() { return category; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public SalesChannel getChannel() { return channel; }
    public String getRegion() { return region; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesRecord that = (SalesRecord) o;
        return Objects.equals(salesRecordId, that.salesRecordId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(salesRecordId);
    }
    
    @Override
    public String toString() {
        return "SalesRecord{" +
                "salesRecordId='" + salesRecordId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                ", saleDate=" + saleDate +
                '}';
    }
}