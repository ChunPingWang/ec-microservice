package com.ecommerce.order.infrastructure.adapter.persistence.mapper;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderItem;
import com.ecommerce.order.infrastructure.adapter.persistence.entity.OrderJpaEntity;
import com.ecommerce.order.infrastructure.adapter.persistence.entity.OrderItemJpaEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 訂單 JPA 映射器
 * 負責領域物件與 JPA 實體之間的轉換
 */
public class OrderJpaMapper {
    
    /**
     * 將領域物件轉換為 JPA 實體
     */
    public static OrderJpaEntity toJpaEntity(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setOrderId(order.getOrderId());
        entity.setCustomerId(order.getCustomerId());
        entity.setCustomerName(order.getCustomerName());
        entity.setCustomerEmail(order.getCustomerEmail());
        entity.setShippingAddress(order.getShippingAddress());
        entity.setBillingAddress(order.getBillingAddress());
        entity.setStatus(order.getStatus());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setShippingFee(order.getShippingFee());
        entity.setTaxAmount(order.getTaxAmount());
        entity.setFinalAmount(order.getFinalAmount());
        entity.setPaymentMethod(order.getPaymentMethod());
        entity.setNotes(order.getNotes());
        entity.setOrderDate(order.getOrderDate());
        entity.setConfirmedDate(order.getConfirmedDate());
        entity.setPaidDate(order.getPaidDate());
        entity.setShippedDate(order.getShippedDate());
        entity.setDeliveredDate(order.getDeliveredDate());
        entity.setCancelledDate(order.getCancelledDate());
        entity.setCancellationReason(order.getCancellationReason());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        
        // 轉換訂單項目
        List<OrderItemJpaEntity> orderItemEntities = order.getOrderItems().stream()
            .map(orderItem -> toOrderItemJpaEntity(orderItem, entity))
            .collect(Collectors.toList());
        entity.setOrderItems(orderItemEntities);
        
        return entity;
    }
    
    /**
     * 將 JPA 實體轉換為領域物件
     */
    public static Order toDomainObject(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // 使用反射或其他方式創建領域物件
        // 這裡簡化實作，實際應用中可能需要更複雜的映射邏輯
        Order order = Order.create(
            entity.getCustomerId(),
            entity.getCustomerName(),
            entity.getCustomerEmail(),
            entity.getShippingAddress(),
            entity.getBillingAddress()
        );
        
        // 設定其他屬性（需要使用反射或提供 setter 方法）
        setOrderProperties(order, entity);
        
        // 轉換訂單項目
        for (OrderItemJpaEntity itemEntity : entity.getOrderItems()) {
            OrderItem orderItem = toOrderItemDomainObject(itemEntity);
            order.addOrderItem(orderItem);
        }
        
        return order;
    }
    
    /**
     * 將訂單項目領域物件轉換為 JPA 實體
     */
    public static OrderItemJpaEntity toOrderItemJpaEntity(OrderItem orderItem, OrderJpaEntity orderEntity) {
        if (orderItem == null) {
            return null;
        }
        
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setOrderItemId(orderItem.getOrderItemId());
        entity.setProductId(orderItem.getProductId());
        entity.setProductName(orderItem.getProductName());
        entity.setUnitPrice(orderItem.getUnitPrice());
        entity.setQuantity(orderItem.getQuantity());
        entity.setTotalPrice(orderItem.getTotalPrice());
        entity.setProductSpecifications(orderItem.getProductSpecifications());
        entity.setCreatedAt(orderItem.getCreatedAt());
        entity.setUpdatedAt(orderItem.getUpdatedAt());
        entity.setOrder(orderEntity);
        
        return entity;
    }
    
    /**
     * 將訂單項目 JPA 實體轉換為領域物件
     */
    public static OrderItem toOrderItemDomainObject(OrderItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return OrderItem.create(
            entity.getProductId(),
            entity.getProductName(),
            entity.getUnitPrice(),
            entity.getQuantity(),
            entity.getProductSpecifications()
        );
    }
    
    /**
     * 將 JPA 實體列表轉換為領域物件列表
     */
    public static List<Order> toDomainObjectList(List<OrderJpaEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(OrderJpaMapper::toDomainObject)
            .collect(Collectors.toList());
    }
    
    /**
     * 將領域物件列表轉換為 JPA 實體列表
     */
    public static List<OrderJpaEntity> toJpaEntityList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        
        return orders.stream()
            .map(OrderJpaMapper::toJpaEntity)
            .collect(Collectors.toList());
    }
    
    // Private helper method to set order properties
    private static void setOrderProperties(Order order, OrderJpaEntity entity) {
        // 這裡需要使用反射或提供適當的 setter 方法來設定私有屬性
        // 為了簡化，這裡省略具體實作
        // 實際應用中，可能需要使用 MapStruct 或其他映射工具
        
        try {
            // 使用反射設定私有屬性
            java.lang.reflect.Field orderIdField = Order.class.getDeclaredField("orderId");
            orderIdField.setAccessible(true);
            orderIdField.set(order, entity.getOrderId());
            
            java.lang.reflect.Field statusField = Order.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(order, entity.getStatus());
            
            // 設定其他必要屬性...
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JPA entity to domain object", e);
        }
    }
}