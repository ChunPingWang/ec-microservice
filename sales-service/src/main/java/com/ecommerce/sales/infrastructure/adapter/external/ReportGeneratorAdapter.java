package com.ecommerce.sales.infrastructure.adapter.external;

import com.ecommerce.common.architecture.ExternalAdapter;
import com.ecommerce.sales.application.port.out.ReportGenerationPort;
import com.ecommerce.sales.domain.model.SalesReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 報表生成適配器
 * 遵循 DIP：實作輸出埠介面，提供報表生成功能
 * 遵循 SRP：只負責報表生成邏輯
 */
@Component
@ExternalAdapter
public class ReportGeneratorAdapter implements ReportGenerationPort {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorAdapter.class);
    
    private static final Set<String> SUPPORTED_FORMATS = Set.of("pdf", "excel", "xlsx", "csv");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public byte[] generatePdfReport(SalesReport report) {
        logger.info("生成 PDF 報表: reportId={}", report.getReportId());
        
        try {
            // 實際應用中應該使用 iText 或其他 PDF 生成庫
            // 這裡提供簡化的實作
            String pdfContent = generateReportContent(report, "PDF");
            
            // 模擬 PDF 生成
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(pdfContent.getBytes(StandardCharsets.UTF_8));
            
            logger.info("PDF 報表生成成功: reportId={}, size={} bytes", 
                       report.getReportId(), outputStream.size());
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            logger.error("生成 PDF 報表失敗: reportId={}", report.getReportId(), e);
            throw new RuntimeException("PDF 報表生成失敗", e);
        }
    }
    
    @Override
    public byte[] generateExcelReport(SalesReport report) {
        logger.info("生成 Excel 報表: reportId={}", report.getReportId());
        
        try {
            // 實際應用中應該使用 Apache POI 生成 Excel
            // 這裡提供簡化的實作
            String excelContent = generateReportContent(report, "Excel");
            
            // 模擬 Excel 生成
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(excelContent.getBytes(StandardCharsets.UTF_8));
            
            logger.info("Excel 報表生成成功: reportId={}, size={} bytes", 
                       report.getReportId(), outputStream.size());
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            logger.error("生成 Excel 報表失敗: reportId={}", report.getReportId(), e);
            throw new RuntimeException("Excel 報表生成失敗", e);
        }
    }
    
    @Override
    public byte[] generateCsvReport(SalesReport report) {
        logger.info("生成 CSV 報表: reportId={}", report.getReportId());
        
        try {
            String csvContent = generateCsvContent(report);
            
            logger.info("CSV 報表生成成功: reportId={}, size={} bytes", 
                       report.getReportId(), csvContent.length());
            
            return csvContent.getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("生成 CSV 報表失敗: reportId={}", report.getReportId(), e);
            throw new RuntimeException("CSV 報表生成失敗", e);
        }
    }
    
    @Override
    public boolean supportsFormat(String format) {
        return format != null && SUPPORTED_FORMATS.contains(format.toLowerCase());
    }
    
    // 私有輔助方法
    
    private String generateReportContent(SalesReport report, String format) {
        StringBuilder content = new StringBuilder();
        
        // 報表標題
        content.append("=".repeat(60)).append("\n");
        content.append(String.format("銷售報表 (%s 格式)\n", format));
        content.append("=".repeat(60)).append("\n\n");
        
        // 報表基本資訊
        content.append("報表資訊:\n");
        content.append(String.format("報表ID: %s\n", report.getReportId()));
        content.append(String.format("報表名稱: %s\n", report.getReportName()));
        content.append(String.format("報表類型: %s\n", report.getReportType().getDisplayName()));
        content.append(String.format("日期範圍: %s 至 %s\n", 
                      report.getStartDate().format(DATE_FORMATTER),
                      report.getEndDate().format(DATE_FORMATTER)));
        content.append(String.format("生成時間: %s\n", 
                      report.getGeneratedAt().format(DATETIME_FORMATTER)));
        content.append("\n");
        
        // 銷售統計
        content.append("銷售統計:\n");
        content.append(String.format("總收入: $%,.2f\n", report.getMetrics().getTotalRevenue()));
        content.append(String.format("總銷量: %,d\n", report.getMetrics().getTotalQuantity()));
        content.append(String.format("平均訂單價值: $%,.2f\n", report.getMetrics().getAverageOrderValue()));
        content.append(String.format("總折扣: $%,.2f\n", report.getMetrics().getTotalDiscount()));
        content.append(String.format("折扣率: %.2f%%\n", report.getMetrics().getDiscountRate().multiply(java.math.BigDecimal.valueOf(100))));
        content.append(String.format("高價值銷售率: %.2f%%\n", report.getMetrics().getHighValueSalesRate().multiply(java.math.BigDecimal.valueOf(100))));
        content.append(String.format("記錄數量: %,d\n", report.getRecords().size()));
        content.append("\n");
        
        // 銷售記錄明細
        if (!report.getRecords().isEmpty()) {
            content.append("銷售記錄明細:\n");
            content.append("-".repeat(120)).append("\n");
            content.append(String.format("%-15s %-15s %-20s %-8s %-12s %-12s %-10s %-15s\n",
                          "銷售記錄ID", "訂單ID", "商品名稱", "數量", "單價", "總金額", "通道", "銷售日期"));
            content.append("-".repeat(120)).append("\n");
            
            report.getRecords().forEach(record -> {
                content.append(String.format("%-15s %-15s %-20s %-8d $%-11.2f $%-11.2f %-10s %s\n",
                              record.getSalesRecordId(),
                              record.getOrderId(),
                              record.getProductName().length() > 20 ? 
                                  record.getProductName().substring(0, 17) + "..." : record.getProductName(),
                              record.getQuantity(),
                              record.getUnitPrice(),
                              record.getTotalAmount(),
                              record.getChannel().name(),
                              record.getSaleDate().format(DATETIME_FORMATTER)));
            });
            
            content.append("-".repeat(120)).append("\n");
        }
        
        return content.toString();
    }
    
    private String generateCsvContent(SalesReport report) {
        StringBuilder csv = new StringBuilder();
        
        // CSV 標頭
        csv.append("銷售記錄ID,訂單ID,客戶ID,商品ID,商品名稱,數量,單價,總金額,折扣,分類,銷售日期,通道,區域\n");
        
        // CSV 資料
        report.getRecords().forEach(record -> {
            csv.append(String.format("%s,%s,%s,%s,\"%s\",%d,%.2f,%.2f,%.2f,\"%s\",%s,%s,\"%s\"\n",
                      record.getSalesRecordId(),
                      record.getOrderId(),
                      record.getCustomerId(),
                      record.getProductId(),
                      record.getProductName(),
                      record.getQuantity(),
                      record.getUnitPrice(),
                      record.getTotalAmount(),
                      record.getDiscount(),
                      record.getCategory(),
                      record.getSaleDate().format(DATETIME_FORMATTER),
                      record.getChannel().name(),
                      record.getRegion()));
        });
        
        return csv.toString();
    }
}