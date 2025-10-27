package com.ecommerce.sales.application.mapper;

import com.ecommerce.sales.application.dto.*;
import com.ecommerce.sales.domain.model.SalesRecord;
import com.ecommerce.sales.domain.model.SalesReport;
import com.ecommerce.sales.domain.service.SalesDomainService.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 銷售資料映射器
 * 遵循 SRP：只負責領域物件與 DTO 之間的轉換
 */
@Component
public class SalesMapper {
    
    /**
     * 將 SalesRecord 轉換為 SalesRecordDto
     */
    public SalesRecordDto toDto(SalesRecord salesRecord) {
        if (salesRecord == null) {
            return null;
        }
        
        return new SalesRecordDto(
            salesRecord.getSalesRecordId(),
            salesRecord.getOrderId(),
            salesRecord.getCustomerId(),
            salesRecord.getProductId(),
            salesRecord.getProductName(),
            salesRecord.getQuantity(),
            salesRecord.getUnitPrice(),
            salesRecord.getTotalAmount(),
            salesRecord.getDiscount(),
            salesRecord.getCategory(),
            salesRecord.getSaleDate(),
            salesRecord.getChannel(),
            salesRecord.getRegion(),
            salesRecord.isHighValueSale(),
            salesRecord.isPromotionalSale(),
            salesRecord.getDiscountRate()
        );
    }
    
    /**
     * 將 SalesRecord 列表轉換為 SalesRecordDto 列表
     */
    public List<SalesRecordDto> toDtoList(List<SalesRecord> salesRecords) {
        if (salesRecords == null) {
            return null;
        }
        
        return salesRecords.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 將 SalesReport 轉換為 SalesReportDto
     */
    public SalesReportDto toDto(SalesReport salesReport) {
        if (salesReport == null) {
            return null;
        }
        
        SalesReportDto dto = new SalesReportDto();
        dto.setReportId(salesReport.getReportId());
        dto.setReportName(salesReport.getReportName());
        dto.setStartDate(salesReport.getStartDate());
        dto.setEndDate(salesReport.getEndDate());
        dto.setGeneratedAt(salesReport.getGeneratedAt());
        dto.setReportType(salesReport.getReportType());
        dto.setTotalRevenue(salesReport.getMetrics().getTotalRevenue());
        dto.setTotalQuantity(salesReport.getMetrics().getTotalQuantity());
        dto.setAverageOrderValue(salesReport.getMetrics().getAverageOrderValue());
        dto.setTotalDiscount(salesReport.getMetrics().getTotalDiscount());
        dto.setHighValueSalesRate(salesReport.getMetrics().getHighValueSalesRate());
        dto.setRecords(toDtoList(salesReport.getRecords()));
        dto.setAdditionalData(salesReport.getAdditionalData());
        
        return dto;
    }
    
    /**
     * 將 SalesTrendAnalysis 轉換為 SalesAnalysisResponse
     */
    public SalesAnalysisResponse toDto(SalesTrendAnalysis analysis, List<SalesRecord> records) {
        if (analysis == null) {
            return null;
        }
        
        // 計算統計資料
        BigDecimal totalRevenue = records.stream()
                .map(SalesRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Integer totalQuantity = records.stream()
                .mapToInt(SalesRecord::getQuantity)
                .sum();
        
        BigDecimal averageOrderValue = records.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(records.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal totalDiscount = records.stream()
                .map(SalesRecord::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discountRate = totalRevenue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                totalDiscount.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP);
        
        int highValueSalesCount = (int) records.stream()
                .mapToLong(record -> record.isHighValueSale() ? 1 : 0)
                .sum();
        
        BigDecimal highValueSalesRate = records.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(highValueSalesCount)
                        .divide(BigDecimal.valueOf(records.size()), 4, BigDecimal.ROUND_HALF_UP);
        
        return new SalesAnalysisResponse(
            null, null, // startDate, endDate 由呼叫者設定
            totalRevenue,
            totalQuantity,
            averageOrderValue,
            totalDiscount,
            discountRate,
            analysis.getAverageDailyRevenue(),
            analysis.getDirection(),
            analysis.getAnalysis(),
            records.size(),
            highValueSalesCount,
            highValueSalesRate
        );
    }
    
    /**
     * 將 CustomerBehaviorAnalysis 轉換為 CustomerBehaviorAnalysisResponse
     */
    public CustomerBehaviorAnalysisResponse toDto(CustomerBehaviorAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        
        BigDecimal averageOrderValue = analysis.getPurchaseCount() == 0 ? BigDecimal.ZERO :
                analysis.getTotalSpent().divide(BigDecimal.valueOf(analysis.getPurchaseCount()), 
                                              2, BigDecimal.ROUND_HALF_UP);
        
        String segmentDescription = getSegmentDescription(analysis.getSegment());
        
        return new CustomerBehaviorAnalysisResponse(
            analysis.getCustomerId(),
            analysis.getSegment(),
            analysis.getTotalSpent(),
            analysis.getPurchaseCount(),
            averageOrderValue,
            segmentDescription
        );
    }
    
    /**
     * 將 ProductPerformanceAnalysis 轉換為 ProductPerformanceAnalysisResponse
     */
    public ProductPerformanceAnalysisResponse toDto(ProductPerformanceAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        
        String performanceDescription = getPerformanceDescription(analysis.getPerformanceLevel());
        
        return new ProductPerformanceAnalysisResponse(
            analysis.getCategory(),
            analysis.getTotalRevenue(),
            analysis.getTotalQuantity(),
            analysis.getPerformanceLevel(),
            performanceDescription
        );
    }
    
    // 私有輔助方法
    
    private String getSegmentDescription(CustomerSegment segment) {
        return switch (segment) {
            case NEW -> "新客戶 - 首次購買或購買次數較少";
            case REGULAR -> "一般客戶 - 有多次購買記錄";
            case LOYAL -> "忠實客戶 - 高頻率購買且消費金額較高";
            case VIP -> "VIP客戶 - 最高價值客戶，享有特殊服務";
        };
    }
    
    private String getPerformanceDescription(PerformanceLevel level) {
        return switch (level) {
            case LOW -> "績效較低 - 需要關注和改善";
            case MEDIUM -> "績效中等 - 表現穩定";
            case HIGH -> "績效優秀 - 表現卓越";
        };
    }
}