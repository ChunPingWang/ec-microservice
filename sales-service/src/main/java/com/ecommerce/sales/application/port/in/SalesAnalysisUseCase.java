package com.ecommerce.sales.application.port.in;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.sales.application.dto.SalesAnalysisRequest;
import com.ecommerce.sales.application.dto.SalesAnalysisResponse;
import com.ecommerce.sales.application.dto.CustomerBehaviorAnalysisResponse;
import com.ecommerce.sales.application.dto.ProductPerformanceAnalysisResponse;
import com.ecommerce.sales.domain.model.SalesChannel;
import java.time.LocalDate;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 銷售分析使用案例介面
 * 遵循 ISP：介面職責單一，只處理銷售分析相關操作
 * 遵循 DIP：定義高層模組的抽象介面
 */
@UseCase
public interface SalesAnalysisUseCase {
    
    /**
     * 分析銷售趨勢
     */
    SalesAnalysisResponse analyzeSalesTrend(SalesAnalysisRequest request);
    
    /**
     * 分析客戶購買行為
     */
    CustomerBehaviorAnalysisResponse analyzeCustomerBehavior(String customerId);
    
    /**
     * 分析商品績效
     */
    ProductPerformanceAnalysisResponse analyzeProductPerformance(String category, 
                                                               LocalDate startDate, 
                                                               LocalDate endDate);
    
    /**
     * 分析通道績效
     */
    Map<SalesChannel, BigDecimal> analyzeChannelPerformance(LocalDate startDate, LocalDate endDate);
    
    /**
     * 檢查是否需要補貨警告
     */
    boolean shouldTriggerRestockAlert(String productId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 取得銷售統計摘要
     */
    SalesAnalysisResponse getSalesSummary(LocalDate startDate, LocalDate endDate);
}