package com.ecommerce.sales.application.usecase;

import com.ecommerce.sales.application.dto.CreateSalesRecordRequest;
import com.ecommerce.sales.application.dto.SalesRecordDto;
import com.ecommerce.sales.application.mapper.SalesMapper;
import com.ecommerce.sales.application.port.in.SalesRecordUseCase;
import com.ecommerce.sales.application.port.out.SalesEventPublisherPort;
import com.ecommerce.sales.application.port.out.SalesPersistencePort;
import com.ecommerce.sales.domain.event.HighValueSaleEvent;
import com.ecommerce.sales.domain.event.SalesRecordCreatedEvent;
import com.ecommerce.sales.domain.exception.SalesRecordNotFoundException;
import com.ecommerce.sales.domain.model.SalesChannel;
import com.ecommerce.sales.domain.model.SalesRecord;
import com.ecommerce.sales.domain.service.SalesDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 銷售記錄服務實作
 * 遵循 SRP：只負責銷售記錄相關的業務流程
 * 遵循 DIP：依賴抽象介面而非具體實作
 */
@Service
@Transactional
public class SalesRecordService implements SalesRecordUseCase {
    
    private final SalesDomainService salesDomainService;
    private final SalesPersistencePort salesPersistencePort;
    private final SalesEventPublisherPort salesEventPublisherPort;
    private final SalesMapper salesMapper;
    
    public SalesRecordService(SalesDomainService salesDomainService,
                            SalesPersistencePort salesPersistencePort,
                            SalesEventPublisherPort salesEventPublisherPort,
                            SalesMapper salesMapper) {
        this.salesDomainService = salesDomainService;
        this.salesPersistencePort = salesPersistencePort;
        this.salesEventPublisherPort = salesEventPublisherPort;
        this.salesMapper = salesMapper;
    }
    
    @Override
    public SalesRecordDto createSalesRecord(CreateSalesRecordRequest request) {
        // 使用領域服務建立銷售記錄
        SalesRecord salesRecord = salesDomainService.createSalesRecord(
            request.getOrderId(),
            request.getCustomerId(),
            request.getProductId(),
            request.getProductName(),
            request.getQuantity(),
            request.getUnitPrice(),
            request.getDiscount(),
            request.getCategory(),
            request.getChannel(),
            request.getRegion()
        );
        
        // 發布銷售記錄建立事件
        SalesRecordCreatedEvent createdEvent = new SalesRecordCreatedEvent(
            salesRecord.getSalesRecordId(),
            salesRecord.getOrderId(),
            salesRecord.getCustomerId(),
            salesRecord.getProductId(),
            salesRecord.getTotalAmount(),
            salesRecord.getCategory(),
            salesRecord.getChannel().name()
        );
        salesEventPublisherPort.publishSalesRecordCreated(createdEvent);
        
        // 如果是高價值銷售，發布高價值銷售事件
        if (salesRecord.isHighValueSale()) {
            HighValueSaleEvent highValueEvent = new HighValueSaleEvent(
                salesRecord.getSalesRecordId(),
                salesRecord.getCustomerId(),
                salesRecord.getTotalAmount(),
                salesRecord.getProductName(),
                salesRecord.getRegion()
            );
            salesEventPublisherPort.publishHighValueSale(highValueEvent);
        }
        
        return salesMapper.toDto(salesRecord);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SalesRecordDto getSalesRecordById(String salesRecordId) {
        SalesRecord salesRecord = salesPersistencePort.findById(salesRecordId)
                .orElseThrow(() -> new SalesRecordNotFoundException(salesRecordId));
        
        return salesMapper.toDto(salesRecord);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SalesRecordDto> getSalesRecordsByOrderId(String orderId) {
        List<SalesRecord> salesRecords = salesPersistencePort.findByOrderId(orderId);
        return salesMapper.toDtoList(salesRecords);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SalesRecordDto> getSalesRecordsByCustomerId(String customerId) {
        List<SalesRecord> salesRecords = salesPersistencePort.findByCustomerId(customerId);
        return salesMapper.toDtoList(salesRecords);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SalesRecordDto> getSalesRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日期不能晚於結束日期");
        }
        
        List<SalesRecord> salesRecords = salesPersistencePort.findByDateRange(startDate, endDate);
        return salesMapper.toDtoList(salesRecords);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SalesRecordDto> getSalesRecordsByCategory(String category) {
        List<SalesRecord> salesRecords = salesPersistencePort.findByCategory(category);
        return salesMapper.toDtoList(salesRecords);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SalesRecordDto> getSalesRecordsByChannel(SalesChannel channel) {
        List<SalesRecord> salesRecords = salesPersistencePort.findByChannel(channel);
        return salesMapper.toDtoList(salesRecords);
    }
}