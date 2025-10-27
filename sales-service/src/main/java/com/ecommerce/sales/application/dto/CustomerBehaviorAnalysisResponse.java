package com.ecommerce.sales.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.sales.domain.service.SalesDomainService.CustomerSegment;
import java.math.BigDecimal;

/**
 * 客戶行為分析回應 DTO
 */
public class CustomerBehaviorAnalysisResponse extends BaseDto {
    
    private String customerId;
    private CustomerSegment segment;
    private BigDecimal totalSpent;
    private int purchaseCount;
    private BigDecimal averageOrderValue;
    private String segmentDescription;
    
    public CustomerBehaviorAnalysisResponse() {}
    
    public CustomerBehaviorAnalysisResponse(String customerId, CustomerSegment segment,
                                          BigDecimal totalSpent, int purchaseCount,
                                          BigDecimal averageOrderValue, String segmentDescription) {
        this.customerId = customerId;
        this.segment = segment;
        this.totalSpent = totalSpent;
        this.purchaseCount = purchaseCount;
        this.averageOrderValue = averageOrderValue;
        this.segmentDescription = segmentDescription;
    }
    
    // Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public CustomerSegment getSegment() { return segment; }
    public void setSegment(CustomerSegment segment) { this.segment = segment; }
    
    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    
    public int getPurchaseCount() { return purchaseCount; }
    public void setPurchaseCount(int purchaseCount) { this.purchaseCount = purchaseCount; }
    
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    
    public String getSegmentDescription() { return segmentDescription; }
    public void setSegmentDescription(String segmentDescription) { this.segmentDescription = segmentDescription; }
}