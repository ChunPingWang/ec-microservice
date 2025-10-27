package com.ecommerce.sales.infrastructure.adapter.messaging;

import com.ecommerce.common.architecture.ExternalAdapter;
import com.ecommerce.sales.application.port.out.SalesEventPublisherPort;
import com.ecommerce.sales.domain.event.HighValueSaleEvent;
import com.ecommerce.sales.domain.event.SalesRecordCreatedEvent;
import com.ecommerce.sales.domain.event.SalesReportGeneratedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 銷售事件發布器
 * 遵循 DIP：實作輸出埠介面，提供事件發布功能
 * 遵循 SRP：只負責事件發布邏輯
 */
@Component
@ExternalAdapter
public class SalesEventPublisher implements SalesEventPublisherPort {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesEventPublisher.class);
    
    // 交換器和路由鍵常數
    private static final String SALES_EXCHANGE = "sales.exchange";
    private static final String SALES_RECORD_CREATED_ROUTING_KEY = "sales.record.created";
    private static final String HIGH_VALUE_SALE_ROUTING_KEY = "sales.high-value";
    private static final String SALES_REPORT_GENERATED_ROUTING_KEY = "sales.report.generated";
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    public SalesEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void publishSalesRecordCreated(SalesRecordCreatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                SALES_EXCHANGE,
                SALES_RECORD_CREATED_ROUTING_KEY,
                eventJson
            );
            
            logger.info("已發布銷售記錄建立事件: salesRecordId={}, orderId={}", 
                       event.getSalesRecordId(), event.getOrderId());
            
        } catch (JsonProcessingException e) {
            logger.error("序列化銷售記錄建立事件失敗: {}", event.getSalesRecordId(), e);
            throw new RuntimeException("事件發布失敗", e);
        } catch (Exception e) {
            logger.error("發布銷售記錄建立事件失敗: {}", event.getSalesRecordId(), e);
            throw new RuntimeException("事件發布失敗", e);
        }
    }
    
    @Override
    public void publishHighValueSale(HighValueSaleEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                SALES_EXCHANGE,
                HIGH_VALUE_SALE_ROUTING_KEY,
                eventJson
            );
            
            logger.info("已發布高價值銷售事件: salesRecordId={}, customerId={}, amount={}", 
                       event.getSalesRecordId(), event.getCustomerId(), event.getSaleAmount());
            
        } catch (JsonProcessingException e) {
            logger.error("序列化高價值銷售事件失敗: {}", event.getSalesRecordId(), e);
            throw new RuntimeException("事件發布失敗", e);
        } catch (Exception e) {
            logger.error("發布高價值銷售事件失敗: {}", event.getSalesRecordId(), e);
            throw new RuntimeException("事件發布失敗", e);
        }
    }
    
    @Override
    public void publishSalesReportGenerated(SalesReportGeneratedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                SALES_EXCHANGE,
                SALES_REPORT_GENERATED_ROUTING_KEY,
                eventJson
            );
            
            logger.info("已發布銷售報表生成事件: reportId={}, reportName={}, recordCount={}", 
                       event.getReportId(), event.getReportName(), event.getRecordCount());
            
        } catch (JsonProcessingException e) {
            logger.error("序列化銷售報表生成事件失敗: {}", event.getReportId(), e);
            throw new RuntimeException("事件發布失敗", e);
        } catch (Exception e) {
            logger.error("發布銷售報表生成事件失敗: {}", event.getReportId(), e);
            throw new RuntimeException("事件發布失敗", e);
        }
    }
}