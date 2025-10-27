package com.ecommerce.sales.domain.event;

import com.ecommerce.common.architecture.DomainEvent;
import java.math.BigDecimal;

/**
 * 高價值銷售事件
 * 當發生高價值銷售時發布此事件，用於觸發特殊處理流程
 */
public class HighValueSaleEvent extends DomainEvent {
    
    private final String salesRecordId;
    private final String customerId;
    private final BigDecimal saleAmount;
    private final String productName;
    private final String region;
    
    public HighValueSaleEvent(String salesRecordId, String customerId, BigDecimal saleAmount,
                            String productName, String region) {
        super("HighValueSale");
        this.salesRecordId = salesRecordId;
        this.customerId = customerId;
        this.saleAmount = saleAmount;
        this.productName = productName;
        this.region = region;
    }
    
    public String getSalesRecordId() { return salesRecordId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getSaleAmount() { return saleAmount; }
    public String getProductName() { return productName; }
    public String getRegion() { return region; }
    
    @Override
    public String toString() {
        return "HighValueSaleEvent{" +
                "salesRecordId='" + salesRecordId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", saleAmount=" + saleAmount +
                ", productName='" + productName + '\'' +
                ", region='" + region + '\'' +
                ", timestamp=" + getOccurredOn() +
                '}';
    }
}