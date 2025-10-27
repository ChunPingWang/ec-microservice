package com.ecommerce.sales.application.port.out;

import com.ecommerce.sales.domain.model.SalesReport;

/**
 * 報表生成輸出埠
 * 遵循 DIP：應用層定義抽象介面，基礎設施層實作
 */
public interface ReportGenerationPort {
    
    /**
     * 生成 PDF 格式報表
     */
    byte[] generatePdfReport(SalesReport report);
    
    /**
     * 生成 Excel 格式報表
     */
    byte[] generateExcelReport(SalesReport report);
    
    /**
     * 生成 CSV 格式報表
     */
    byte[] generateCsvReport(SalesReport report);
    
    /**
     * 檢查支援的格式
     */
    boolean supportsFormat(String format);
}