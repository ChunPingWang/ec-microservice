package com.ecommerce.sales.infrastructure.adapter.persistence.entity;

import com.ecommerce.sales.domain.model.SalesChannel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 銷售記錄 JPA 實體
 * 遵循 SRP：只負責資料庫映射
 */
@Entity
@Table(name = "sales_records", indexes = {
    @Index(name = "idx_sales_order_id", columnList = "orderId"),
    @Index(name = "idx_sales_customer_id", columnList = "customerId"),
    @Index(name = "idx_sales_product_id", columnList = "productId"),
    @Index(name = "idx_sales_sale_date", columnList = "saleDate"),
    @Index(name = "idx_sales_category", columnList = "category"),
    @Index(name = "idx_sales_channel", columnList = "channel"),
    @Index(name = "idx_sales_region", columnList = "region")
})
public class SalesRecordJpaEntity {
    
    @Id
    @Column(name = "sales_record_id", length = 50)
    private String salesRecordId;
    
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;
    
    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;
    
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;
    
    @Column(name = "category", nullable = false, length = 100)
    private String category;
    
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private SalesChannel channel;
    
    @Column(name = "region", nullable = false, length = 100)
    private String region;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // JPA 需要預設建構子
    protected SalesRecordJpaEntity() {}
    
    public SalesRecordJpaEntity(String salesRecordId, String orderId, String customerId,
                              String productId, String productName, Integer quantity,
                              BigDecimal unitPrice, BigDecimal totalAmount, BigDecimal discount,
                              String category, LocalDateTime saleDate, SalesChannel channel,
                              String region) {
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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesRecordJpaEntity that = (SalesRecordJpaEntity) o;
        return Objects.equals(salesRecordId, that.salesRecordId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(salesRecordId);
    }
    
    @Override
    public String toString() {
        return "SalesRecordJpaEntity{" +
                "salesRecordId='" + salesRecordId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                ", saleDate=" + saleDate +
                '}';
    }
}