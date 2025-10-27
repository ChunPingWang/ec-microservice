package com.ecommerce.order.application.mapper;

import com.ecommerce.order.application.dto.OrderDto;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 訂單映射器
 * 負責 Order 實體與 OrderDto 之間的轉換
 */
public class OrderMapper {
    
    /**
     * 將 Order 實體轉換為 OrderDto
     */
    public static OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomerId());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setNotes(order.getNotes());
        dto.setOrderDate(order.getOrderDate());
        dto.setConfirmedDate(order.getConfirmedDate());
        dto.setPaidDate(order.getPaidDate());
        dto.setShippedDate(order.getShippedDate());
        dto.setDeliveredDate(order.getDeliveredDate());
        dto.setCancelledDate(order.getCancelledDate());
        dto.setCancellationReason(order.getCancellationReason());
        
        // 轉換訂單項目
        List<OrderDto.OrderItemDto> orderItemDtos = order.getOrderItems().stream()
            .map(OrderMapper::toOrderItemDto)
            .collect(Collectors.toList());
        dto.setOrderItems(orderItemDtos);
        
        return dto;
    }
    
    /**
     * 將 Order 實體列表轉換為 OrderDto 列表
     */
    public static List<OrderDto> toDtoList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        
        return orders.stream()
            .map(OrderMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 將 OrderItem 實體轉換為 OrderItemDto
     */
    private static OrderDto.OrderItemDto toOrderItemDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        OrderDto.OrderItemDto dto = new OrderDto.OrderItemDto();
        dto.setOrderItemId(orderItem.getOrderItemId());
        dto.setProductId(orderItem.getProductId());
        dto.setProductName(orderItem.getProductName());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setQuantity(orderItem.getQuantity());
        dto.setTotalPrice(orderItem.getTotalPrice());
        dto.setProductSpecifications(orderItem.getProductSpecifications());
        
        return dto;
    }
}