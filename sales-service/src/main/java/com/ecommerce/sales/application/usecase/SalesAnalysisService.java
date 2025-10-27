package com.ecommerce.sales.application.usecase;

import com.ecommerce.sales.application.dto.*;
import com.ecommerce.sales.application.mapper.SalesMapper;
import com.ecommerce.sales.application.port.in.SalesAnalysisUseCase;
import com.ecommerce.sales.application.port.out.SalesPersistencePort;
import com.ecommerce.sales.domain.model.SalesChannel;
import com.ecommerce.sales.domain.model.SalesRecord;
import com.ecommerce.sales.domain.service.SalesDomainService;
import com.ecommerce.sales.domain.service.SalesDomainService.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 銷售分析服務實作
 * 遵循 SRP：只負責銷售分析相關的業務流程
 */
@Service
@Transactional(readOnly = true)
public class SalesAnalysisService implements SalesAnalysisUseCase {
    
    private final SalesDomainService salesDomainService;
    private final SalesPersistencePort salesPersistencePort;
    private final SalesMapper salesMapper;
    
    public SalesAnalysisService(SalesDomainService salesDomainService,
                              SalesPersistencePort salesPersistencePort,
                              SalesMapper salesMapper) {
        this.salesDomainService = salesDomainService;
        this.salesPersistencePort = salesPersistencePort;
        this.salesMapper = salesMapper;
    }
    
    @Override
    public SalesAnalysisResponse analyzeSalesTrend(SalesAnalysisRequest request) {
        request.validate();
        
        // 取得指定期間的銷售記錄
        List<SalesRecord> records = getFilteredRecords(request);
        
        // 使用領域服務分析趨勢
        SalesTrendAnalysis trendAnalysis = salesDomainService.analyzeSalesTrend(
            request.getStartDate(), request.getEndDate());
        
        // 轉換為 DTO 並設定日期範圍
        SalesAnalysisResponse response = salesMapper.toDto(trendAnalysis, records);
        response.setStartDate(request.getStartDate());
        response.setEndDate(request.getEndDate());
        
        return response;
    }
    
    @Override
    public CustomerBehaviorAnalysisResponse analyzeCustomerBehavior(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("客戶ID不能為空");
        }
        
        // 使用領域服務分析客戶行為
        CustomerBehaviorAnalysis behaviorAnalysis = salesDomainService.analyzeCustomerBehavior(customerId);
        
        return salesMapper.toDto(behaviorAnalysis);
    }
    
    @Override
    public ProductPerformanceAnalysisResponse analyzeProductPerformance(String category, 
                                                                       LocalDate startDate, 
                                                                       LocalDate endDate) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("商品分類不能為空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日期不能晚於結束日期");
        }
        
        // 使用領域服務分析商品績效
        ProductPerformanceAnalysis performanceAnalysis = salesDomainService.analyzeProductPerformance(
            category, startDate, endDate);
        
        return salesMapper.toDto(performanceAnalysis);
    }
    
    @Override
    public Map<SalesChannel, BigDecimal> analyzeChannelPerformance(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日期不能晚於結束日期");
        }
        
        // 使用領域服務分析通道績效
        return salesDomainService.analyzeChannelPerformance(startDate, endDate);
    }
    
    @Override
    public boolean shouldTriggerRestockAlert(String productId, LocalDate startDate, LocalDate endDate) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("商品ID不能為空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日期不能晚於結束日期");
        }
        
        // 使用領域服務檢查補貨警告
        return salesDomainService.shouldTriggerRestockAlert(productId, startDate, endDate);
    }
    
    @Override
    public SalesAnalysisResponse getSalesSummary(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日期不能晚於結束日期");
        }
        
        // 建立分析請求
        SalesAnalysisRequest request = new SalesAnalysisRequest(startDate, endDate);
        
        // 執行趨勢分析
        return analyzeSalesTrend(request);
    }
    
    // 私有輔助方法
    
    private List<SalesRecord> getFilteredRecords(SalesAnalysisRequest request) {
        List<SalesRecord> records = salesPersistencePort.findByDateRange(
            request.getStartDate(), request.getEndDate());
        
        // 根據請求參數進行過濾
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            records = records.stream()
                    .filter(record -> request.getCategory().equals(record.getCategory()))
                    .toList();
        }
        
        if (request.getRegion() != null && !request.getRegion().trim().isEmpty()) {
            records = records.stream()
                    .filter(record -> request.getRegion().equals(record.getRegion()))
                    .toList();
        }
        
        if (request.getCustomerId() != null && !request.getCustomerId().trim().isEmpty()) {
            records = records.stream()
                    .filter(record -> request.getCustomerId().equals(record.getCustomerId()))
                    .toList();
        }
        
        if (request.getProductId() != null && !request.getProductId().trim().isEmpty()) {
            records = records.stream()
                    .filter(record -> request.getProductId().equals(record.getProductId()))
                    .toList();
        }
        
        return records;
    }
}