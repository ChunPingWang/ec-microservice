package com.ecommerce.sales.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.sales.domain.service.SalesDomainService.PerformanceLevel;
import java.math.BigDecimal;

/**
 * 商品績效分析回應 DTO
 */
public class ProductPerformanceAnalysisResponse extends BaseDto {
    
    private String category;
    private BigDecimal totalRevenue;
    private int totalQuantity;
    private PerformanceLevel performanceLevel;
    private String performanceDescription;
    
    public ProductPerformanceAnalysisResponse() {}
    
    public ProductPerformanceAnalysisResponse(String category, BigDecimal totalRevenue,
                                            int totalQuantity, PerformanceLevel performanceLevel,
                                            String performanceDescription) {
        this.category = category;
        this.totalRevenue = totalRevenue;
        this.totalQuantity = totalQuantity;
        this.performanceLevel = performanceLevel;
        this.performanceDescription = performanceDescription;
    }
    
    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    
    public PerformanceLevel getPerformanceLevel() { return performanceLevel; }
    public void setPerformanceLevel(PerformanceLevel performanceLevel) { this.performanceLevel = performanceLevel; }
    
    public String getPerformanceDescription() { return performanceDescription; }
    public void setPerformanceDescription(String performanceDescription) { this.performanceDescription = performanceDescription; }
}