package com.ecommerce.sales.infrastructure.adapter.persistence.mapper;

import com.ecommerce.sales.domain.model.SalesRecord;
import com.ecommerce.sales.infrastructure.adapter.persistence.entity.SalesRecordJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 銷售記錄 JPA 映射器
 * 遵循 SRP：只負責領域物件與 JPA 實體之間的轉換
 */
@Component
public class SalesRecordJpaMapper {
    
    /**
     * 將領域物件轉換為 JPA 實體
     */
    public SalesRecordJpaEntity toJpaEntity(SalesRecord salesRecord) {
        if (salesRecord == null) {
            return null;
        }
        
        return new SalesRecordJpaEntity(
            salesRecord.getSalesRecordId(),
            salesRecord.getOrderId(),
            salesRecord.getCustomerId(),
            salesRecord.getProductId(),
            salesRecord.getProductName(),
            salesRecord.getQuantity(),
            salesRecord.getUnitPrice(),
            salesRecord.getTotalAmount(),
            salesRecord.getDiscount(),
            salesRecord.getCategory(),
            salesRecord.getSaleDate(),
            salesRecord.getChannel(),
            salesRecord.getRegion()
        );
    }
    
    /**
     * 將 JPA 實體轉換為領域物件
     */
    public SalesRecord toDomainObject(SalesRecordJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        return SalesRecord.create(
            jpaEntity.getSalesRecordId(),
            jpaEntity.getOrderId(),
            jpaEntity.getCustomerId(),
            jpaEntity.getProductId(),
            jpaEntity.getProductName(),
            jpaEntity.getQuantity(),
            jpaEntity.getUnitPrice(),
            jpaEntity.getDiscount(),
            jpaEntity.getCategory(),
            jpaEntity.getChannel(),
            jpaEntity.getRegion()
        );
    }
    
    /**
     * 將 JPA 實體列表轉換為領域物件列表
     */
    public List<SalesRecord> toDomainObjectList(List<SalesRecordJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return null;
        }
        
        return jpaEntities.stream()
                .map(this::toDomainObject)
                .collect(Collectors.toList());
    }
    
    /**
     * 將領域物件列表轉換為 JPA 實體列表
     */
    public List<SalesRecordJpaEntity> toJpaEntityList(List<SalesRecord> salesRecords) {
        if (salesRecords == null) {
            return null;
        }
        
        return salesRecords.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
    }
}