package com.ecommerce.sales.application.port.in;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.sales.application.dto.CreateSalesRecordRequest;
import com.ecommerce.sales.application.dto.SalesRecordDto;
import com.ecommerce.sales.domain.model.SalesChannel;
import java.time.LocalDate;
import java.util.List;

/**
 * 銷售記錄使用案例介面
 * 遵循 ISP：介面職責單一，只處理銷售記錄相關操作
 */
@UseCase
public interface SalesRecordUseCase {
    
    /**
     * 建立銷售記錄
     */
    SalesRecordDto createSalesRecord(CreateSalesRecordRequest request);
    
    /**
     * 根據ID取得銷售記錄
     */
    SalesRecordDto getSalesRecordById(String salesRecordId);
    
    /**
     * 根據訂單ID取得銷售記錄
     */
    List<SalesRecordDto> getSalesRecordsByOrderId(String orderId);
    
    /**
     * 根據客戶ID取得銷售記錄
     */
    List<SalesRecordDto> getSalesRecordsByCustomerId(String customerId);
    
    /**
     * 根據日期範圍取得銷售記錄
     */
    List<SalesRecordDto> getSalesRecordsByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根據分類取得銷售記錄
     */
    List<SalesRecordDto> getSalesRecordsByCategory(String category);
    
    /**
     * 根據通道取得銷售記錄
     */
    List<SalesRecordDto> getSalesRecordsByChannel(SalesChannel channel);
}