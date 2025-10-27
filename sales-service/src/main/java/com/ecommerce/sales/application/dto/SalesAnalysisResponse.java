package com.ecommerce.sales.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.sales.domain.service.SalesDomainService.TrendDirection;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 銷售分析回應 DTO
 * 遵循 SRP：只負責封裝銷售分析的結果資料
 */
public class SalesAnalysisResponse extends BaseDto {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private Integer totalQuantity;
    private BigDecimal averageOrderValue;
    private BigDecimal totalDiscount;
    private BigDecimal discountRate;
    private BigDecimal averageDailyRevenue;
    private TrendDirection trendDirection;
    private String analysis;
    private int recordCount;
    private int highValueSalesCount;
    private BigDecimal highValueSalesRate;
    
    // 預設建構子
    public SalesAnalysisResponse() {}
    
    // 建構子
    public SalesAnalysisResponse(LocalDate startDate, LocalDate endDate, BigDecimal totalRevenue,
                               Integer totalQuantity, BigDecimal averageOrderValue,
                               BigDecimal totalDiscount, BigDecimal discountRate,
                               BigDecimal averageDailyRevenue, TrendDirection trendDirection,
                               String analysis, int recordCount, int highValueSalesCount,
                               BigDecimal highValueSalesRate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRevenue = totalRevenue;
        this.totalQuantity = totalQuantity;
        this.averageOrderValue = averageOrderValue;
        this.totalDiscount = totalDiscount;
        this.discountRate = discountRate;
        this.averageDailyRevenue = averageDailyRevenue;
        this.trendDirection = trendDirection;
        this.analysis = analysis;
        this.recordCount = recordCount;
        this.highValueSalesCount = highValueSalesCount;
        this.highValueSalesRate = highValueSalesRate;
    }
    
    // Getters and Setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }
    
    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
    
    public BigDecimal getAverageDailyRevenue() { return averageDailyRevenue; }
    public void setAverageDailyRevenue(BigDecimal averageDailyRevenue) { this.averageDailyRevenue = averageDailyRevenue; }
    
    public TrendDirection getTrendDirection() { return trendDirection; }
    public void setTrendDirection(TrendDirection trendDirection) { this.trendDirection = trendDirection; }
    
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    
    public int getRecordCount() { return recordCount; }
    public void setRecordCount(int recordCount) { this.recordCount = recordCount; }
    
    public int getHighValueSalesCount() { return highValueSalesCount; }
    public void setHighValueSalesCount(int highValueSalesCount) { this.highValueSalesCount = highValueSalesCount; }
    
    public BigDecimal getHighValueSalesRate() { return highValueSalesRate; }
    public void setHighValueSalesRate(BigDecimal highValueSalesRate) { this.highValueSalesRate = highValueSalesRate; }
    
    @Override
    public String toString() {
        return "SalesAnalysisResponse{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalRevenue=" + totalRevenue +
                ", totalQuantity=" + totalQuantity +
                ", trendDirection=" + trendDirection +
                ", recordCount=" + recordCount +
                '}';
    }
}