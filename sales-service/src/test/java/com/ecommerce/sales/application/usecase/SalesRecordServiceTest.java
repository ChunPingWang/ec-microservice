package com.ecommerce.sales.application.usecase;

import com.ecommerce.sales.application.dto.CreateSalesRecordRequest;
import com.ecommerce.sales.application.dto.SalesRecordDto;
import com.ecommerce.sales.application.mapper.SalesMapper;
import com.ecommerce.sales.application.port.out.SalesEventPublisherPort;
import com.ecommerce.sales.application.port.out.SalesPersistencePort;
import com.ecommerce.sales.domain.exception.SalesRecordNotFoundException;
import com.ecommerce.sales.domain.model.SalesChannel;
import com.ecommerce.sales.domain.model.SalesRecord;
import com.ecommerce.sales.domain.service.SalesDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 銷售記錄服務測試
 * 測試銷售記錄的建立、查詢和事件發布
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("銷售記錄服務測試")
class SalesRecordServiceTest {

    @Mock
    private SalesDomainService salesDomainService;

    @Mock
    private SalesPersistencePort salesPersistencePort;

    @Mock
    private SalesEventPublisherPort salesEventPublisherPort;

    @Mock
    private SalesMapper salesMapper;

    private SalesRecordService salesRecordService;

    @BeforeEach
    void setUp() {
        salesRecordService = new SalesRecordService(
            salesDomainService, salesPersistencePort, salesEventPublisherPort, salesMapper
        );
    }

    @Test
    @DisplayName("應該成功建立銷售記錄")
    void shouldCreateSalesRecordSuccessfully() {
        // Given
        CreateSalesRecordRequest request = new CreateSalesRecordRequest();
        request.setOrderId("ORDER-001");
        request.setCustomerId("CUST-001");
        request.setProductId("PROD-001");
        request.setProductName("iPhone 17 Pro");
        request.setQuantity(1);
        request.setUnitPrice(new BigDecimal("35000"));
        request.setDiscount(new BigDecimal("1000"));
        request.setCategory("Electronics");
        request.setChannel(SalesChannel.ONLINE);
        request.setRegion("台北");

        SalesRecord mockSalesRecord = SalesRecord.create(
            "SR-001", request.getOrderId(), request.getCustomerId(), request.getProductId(),
            request.getProductName(), request.getQuantity(), request.getUnitPrice(),
            request.getDiscount(), request.getCategory(), request.getChannel(), request.getRegion()
        );

        SalesRecordDto expectedDto = new SalesRecordDto();
        expectedDto.setSalesRecordId("SR-001");
        expectedDto.setOrderId(request.getOrderId());

        when(salesDomainService.createSalesRecord(
            request.getOrderId(), request.getCustomerId(), request.getProductId(),
            request.getProductName(), request.getQuantity(), request.getUnitPrice(),
            request.getDiscount(), request.getCategory(), request.getChannel(), request.getRegion()
        )).thenReturn(mockSalesRecord);
        when(salesMapper.toDto(mockSalesRecord)).thenReturn(expectedDto);

        // When
        SalesRecordDto result = salesRecordService.createSalesRecord(request);

        // Then
        assertNotNull(result);
        assertEquals("SR-001", result.getSalesRecordId());
        assertEquals(request.getOrderId(), result.getOrderId());

        verify(salesDomainService).createSalesRecord(
            request.getOrderId(), request.getCustomerId(), request.getProductId(),
            request.getProductName(), request.getQuantity(), request.getUnitPrice(),
            request.getDiscount(), request.getCategory(), request.getChannel(), request.getRegion()
        );
        verify(salesEventPublisherPort).publishSalesRecordCreated(any());
        verify(salesMapper).toDto(mockSalesRecord);
    }

    @Test
    @DisplayName("建立高價值銷售記錄時應發布高價值銷售事件")
    void shouldPublishHighValueSaleEventForHighValueSales() {
        // Given
        CreateSalesRecordRequest request = new CreateSalesRecordRequest();
        request.setOrderId("ORDER-002");
        request.setCustomerId("CUST-002");
        request.setProductId("PROD-002");
        request.setProductName("MacBook Pro");
        request.setQuantity(1);
        request.setUnitPrice(new BigDecimal("50000")); // 高價值商品
        request.setDiscount(BigDecimal.ZERO);
        request.setCategory("Electronics");
        request.setChannel(SalesChannel.ONLINE);
        request.setRegion("台北");

        SalesRecord mockHighValueSalesRecord = SalesRecord.create(
            "SR-002", request.getOrderId(), request.getCustomerId(), request.getProductId(),
            request.getProductName(), request.getQuantity(), request.getUnitPrice(),
            request.getDiscount(), request.getCategory(), request.getChannel(), request.getRegion()
        );

        SalesRecordDto expectedDto = new SalesRecordDto();
        expectedDto.setSalesRecordId("SR-002");

        when(salesDomainService.createSalesRecord(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockHighValueSalesRecord);
        when(salesMapper.toDto(mockHighValueSalesRecord)).thenReturn(expectedDto);

        // When
        SalesRecordDto result = salesRecordService.createSalesRecord(request);

        // Then
        assertNotNull(result);
        verify(salesEventPublisherPort).publishSalesRecordCreated(any());
        verify(salesEventPublisherPort).publishHighValueSale(any()); // 應該發布高價值銷售事件
    }

    @Test
    @DisplayName("應該成功根據ID查詢銷售記錄")
    void shouldGetSalesRecordByIdSuccessfully() {
        // Given
        String salesRecordId = "SR-001";
        SalesRecord mockSalesRecord = SalesRecord.create(
            salesRecordId, "ORDER-001", "CUST-001", "PROD-001", "iPhone 17 Pro",
            1, new BigDecimal("35000"), new BigDecimal("1000"), "Electronics", 
            SalesChannel.ONLINE, "台北"
        );

        SalesRecordDto expectedDto = new SalesRecordDto();
        expectedDto.setSalesRecordId(salesRecordId);

        when(salesPersistencePort.findById(salesRecordId)).thenReturn(Optional.of(mockSalesRecord));
        when(salesMapper.toDto(mockSalesRecord)).thenReturn(expectedDto);

        // When
        SalesRecordDto result = salesRecordService.getSalesRecordById(salesRecordId);

        // Then
        assertNotNull(result);
        assertEquals(salesRecordId, result.getSalesRecordId());
        verify(salesPersistencePort).findById(salesRecordId);
        verify(salesMapper).toDto(mockSalesRecord);
    }

    @Test
    @DisplayName("查詢不存在的銷售記錄時應拋出異常")
    void shouldThrowExceptionWhenSalesRecordNotFound() {
        // Given
        String nonExistentId = "SR-NONEXISTENT";
        when(salesPersistencePort.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        SalesRecordNotFoundException exception = assertThrows(
            SalesRecordNotFoundException.class,
            () -> salesRecordService.getSalesRecordById(nonExistentId)
        );
        
        assertNotNull(exception);
        verify(salesPersistencePort).findById(nonExistentId);
        verify(salesMapper, never()).toDto(any(SalesRecord.class));
    }

    @Test
    @DisplayName("應該成功根據訂單ID查詢銷售記錄")
    void shouldGetSalesRecordsByOrderIdSuccessfully() {
        // Given
        String orderId = "ORDER-001";
        List<SalesRecord> mockRecords = List.of(
            SalesRecord.create(
                "SR-001", orderId, "CUST-001", "PROD-001", "iPhone 17 Pro",
                1, new BigDecimal("35000"), new BigDecimal("1000"), "Electronics", 
                SalesChannel.ONLINE, "台北"
            ),
            SalesRecord.create(
                "SR-002", orderId, "CUST-001", "PROD-002", "AirPods Pro",
                1, new BigDecimal("8000"), BigDecimal.ZERO, "Electronics", 
                SalesChannel.ONLINE, "台北"
            )
        );

        List<SalesRecordDto> expectedDtos = List.of(
            createMockSalesRecordDto("SR-001"),
            createMockSalesRecordDto("SR-002")
        );

        when(salesPersistencePort.findByOrderId(orderId)).thenReturn(mockRecords);
        when(salesMapper.toDtoList(mockRecords)).thenReturn(expectedDtos);

        // When
        List<SalesRecordDto> result = salesRecordService.getSalesRecordsByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(salesPersistencePort).findByOrderId(orderId);
        verify(salesMapper).toDtoList(mockRecords);
    }

    @Test
    @DisplayName("應該成功根據客戶ID查詢銷售記錄")
    void shouldGetSalesRecordsByCustomerIdSuccessfully() {
        // Given
        String customerId = "CUST-001";
        List<SalesRecord> mockRecords = List.of(
            SalesRecord.create(
                "SR-001", "ORDER-001", customerId, "PROD-001", "iPhone 17 Pro",
                1, new BigDecimal("35000"), new BigDecimal("1000"), "Electronics", 
                SalesChannel.ONLINE, "台北"
            )
        );

        List<SalesRecordDto> expectedDtos = List.of(createMockSalesRecordDto("SR-001"));

        when(salesPersistencePort.findByCustomerId(customerId)).thenReturn(mockRecords);
        when(salesMapper.toDtoList(mockRecords)).thenReturn(expectedDtos);

        // When
        List<SalesRecordDto> result = salesRecordService.getSalesRecordsByCustomerId(customerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(salesPersistencePort).findByCustomerId(customerId);
        verify(salesMapper).toDtoList(mockRecords);
    }

    @Test
    @DisplayName("應該成功根據日期範圍查詢銷售記錄")
    void shouldGetSalesRecordsByDateRangeSuccessfully() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        List<SalesRecord> mockRecords = List.of(
            SalesRecord.create(
                "SR-001", "ORDER-001", "CUST-001", "PROD-001", "iPhone 17 Pro",
                1, new BigDecimal("35000"), new BigDecimal("1000"), "Electronics", 
                SalesChannel.ONLINE, "台北"
            )
        );

        List<SalesRecordDto> expectedDtos = List.of(createMockSalesRecordDto("SR-001"));

        when(salesPersistencePort.findByDateRange(startDate, endDate)).thenReturn(mockRecords);
        when(salesMapper.toDtoList(mockRecords)).thenReturn(expectedDtos);

        // When
        List<SalesRecordDto> result = salesRecordService.getSalesRecordsByDateRange(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(salesPersistencePort).findByDateRange(startDate, endDate);
        verify(salesMapper).toDtoList(mockRecords);
    }

    @Test
    @DisplayName("開始日期晚於結束日期時應拋出異常")
    void shouldThrowExceptionWhenStartDateAfterEndDate() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 2, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> salesRecordService.getSalesRecordsByDateRange(startDate, endDate)
        );
        
        assertEquals("開始日期不能晚於結束日期", exception.getMessage());
        verify(salesPersistencePort, never()).findByDateRange(any(), any());
    }

    @Test
    @DisplayName("應該成功根據分類查詢銷售記錄")
    void shouldGetSalesRecordsByCategorySuccessfully() {
        // Given
        String category = "Electronics";
        List<SalesRecord> mockRecords = List.of(
            SalesRecord.create(
                "SR-001", "ORDER-001", "CUST-001", "PROD-001", "iPhone 17 Pro",
                1, new BigDecimal("35000"), new BigDecimal("1000"), category, 
                SalesChannel.ONLINE, "台北"
            )
        );

        List<SalesRecordDto> expectedDtos = List.of(createMockSalesRecordDto("SR-001"));

        when(salesPersistencePort.findByCategory(category)).thenReturn(mockRecords);
        when(salesMapper.toDtoList(mockRecords)).thenReturn(expectedDtos);

        // When
        List<SalesRecordDto> result = salesRecordService.getSalesRecordsByCategory(category);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(salesPersistencePort).findByCategory(category);
        verify(salesMapper).toDtoList(mockRecords);
    }

    @Test
    @DisplayName("應該成功根據通道查詢銷售記錄")
    void shouldGetSalesRecordsByChannelSuccessfully() {
        // Given
        SalesChannel channel = SalesChannel.ONLINE;
        List<SalesRecord> mockRecords = List.of(
            SalesRecord.create(
                "SR-001", "ORDER-001", "CUST-001", "PROD-001", "iPhone 17 Pro",
                1, new BigDecimal("35000"), new BigDecimal("1000"), "Electronics", 
                channel, "台北"
            )
        );

        List<SalesRecordDto> expectedDtos = List.of(createMockSalesRecordDto("SR-001"));

        when(salesPersistencePort.findByChannel(channel)).thenReturn(mockRecords);
        when(salesMapper.toDtoList(mockRecords)).thenReturn(expectedDtos);

        // When
        List<SalesRecordDto> result = salesRecordService.getSalesRecordsByChannel(channel);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(salesPersistencePort).findByChannel(channel);
        verify(salesMapper).toDtoList(mockRecords);
    }

    // 輔助方法

    private SalesRecordDto createMockSalesRecordDto(String salesRecordId) {
        SalesRecordDto dto = new SalesRecordDto();
        dto.setSalesRecordId(salesRecordId);
        dto.setOrderId("ORDER-001");
        dto.setCustomerId("CUST-001");
        dto.setProductId("PROD-001");
        dto.setProductName("iPhone 17 Pro");
        dto.setQuantity(1);
        dto.setUnitPrice(new BigDecimal("35000"));
        dto.setTotalAmount(new BigDecimal("34000"));
        dto.setDiscount(new BigDecimal("1000"));
        dto.setCategory("Electronics");
        dto.setChannel(SalesChannel.ONLINE);
        dto.setRegion("台北");
        return dto;
    }
}