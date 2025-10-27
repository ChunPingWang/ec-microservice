package com.ecommerce.sales.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 銷售指標值物件 - 封裝銷售統計資料
 * 不可變物件，遵循值物件設計原則
 */
public class SalesMetrics {
    
    private final BigDecimal totalRevenue;
    private final Integer totalQuantity;
    private final BigDecimal averageOrderValue;
    private final BigDecimal totalDiscount;
    private final BigDecimal highValueSalesRate;
    
    public SalesMetrics(BigDecimal totalRevenue, Integer totalQuantity, 
                       BigDecimal averageOrderValue, BigDecimal totalDiscount,
                       BigDecimal highValueSalesRate) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalQuantity = totalQuantity != null ? totalQuantity : 0;
        this.averageOrderValue = averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO;
        this.totalDiscount = totalDiscount != null ? totalDiscount : BigDecimal.ZERO;
        this.highValueSalesRate = highValueSalesRate != null ? highValueSalesRate : BigDecimal.ZERO;
    }
    
    /**
     * 計算淨收入（扣除折扣後的收入）
     */
    public BigDecimal getNetRevenue() {
        return totalRevenue.subtract(totalDiscount);
    }
    
    /**
     * 計算折扣率
     */
    public BigDecimal getDiscountRate() {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalDiscount.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 檢查是否達到目標收入
     */
    public boolean meetsRevenueTarget(BigDecimal target) {
        return totalRevenue.compareTo(target) >= 0;
    }
    
    /**
     * 檢查是否為高績效銷售（高價值銷售率 > 20%）
     */
    public boolean isHighPerformance() {
        return highValueSalesRate.compareTo(new BigDecimal("0.2")) > 0;
    }
    
    // Getters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public BigDecimal getHighValueSalesRate() { return highValueSalesRate; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesMetrics that = (SalesMetrics) o;
        return Objects.equals(totalRevenue, that.totalRevenue) &&
               Objects.equals(totalQuantity, that.totalQuantity) &&
               Objects.equals(averageOrderValue, that.averageOrderValue) &&
               Objects.equals(totalDiscount, that.totalDiscount) &&
               Objects.equals(highValueSalesRate, that.highValueSalesRate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalRevenue, totalQuantity, averageOrderValue, 
                          totalDiscount, highValueSalesRate);
    }
    
    @Override
    public String toString() {
        return "SalesMetrics{" +
                "totalRevenue=" + totalRevenue +
                ", totalQuantity=" + totalQuantity +
                ", averageOrderValue=" + averageOrderValue +
                ", totalDiscount=" + totalDiscount +
                ", highValueSalesRate=" + highValueSalesRate +
                '}';
    }
}