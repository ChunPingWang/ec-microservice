package com.ecommerce.sales.application.port.in;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.sales.application.dto.GenerateReportRequest;
import com.ecommerce.sales.application.dto.SalesReportDto;
import com.ecommerce.sales.domain.model.ReportType;
import java.time.LocalDate;
import java.util.List;

/**
 * 銷售報表使用案例介面
 * 遵循 ISP：介面職責單一，只處理報表相關操作
 */
@UseCase
public interface SalesReportUseCase {
    
    /**
     * 生成銷售報表
     */
    SalesReportDto generateReport(GenerateReportRequest request);
    
    /**
     * 取得報表清單
     */
    List<SalesReportDto> getReports(ReportType reportType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根據ID取得報表
     */
    SalesReportDto getReportById(String reportId);
    
    /**
     * 匯出報表
     */
    byte[] exportReport(String reportId, String format);
    
    /**
     * 刪除報表
     */
    void deleteReport(String reportId);
}