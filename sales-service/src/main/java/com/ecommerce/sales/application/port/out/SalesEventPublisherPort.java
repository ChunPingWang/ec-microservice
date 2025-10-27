package com.ecommerce.sales.application.port.out;

import com.ecommerce.sales.domain.event.SalesRecordCreatedEvent;
import com.ecommerce.sales.domain.event.HighValueSaleEvent;
import com.ecommerce.sales.domain.event.SalesReportGeneratedEvent;

/**
 * 銷售事件發布輸出埠
 * 遵循 DIP：應用層定義抽象介面，基礎設施層實作
 */
public interface SalesEventPublisherPort {
    
    /**
     * 發布銷售記錄建立事件
     */
    void publishSalesRecordCreated(SalesRecordCreatedEvent event);
    
    /**
     * 發布高價值銷售事件
     */
    void publishHighValueSale(HighValueSaleEvent event);
    
    /**
     * 發布銷售報表生成事件
     */
    void publishSalesReportGenerated(SalesReportGeneratedEvent event);
}