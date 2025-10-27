package com.ecommerce.order.application.port.in;

import com.ecommerce.order.application.dto.CreateOrderRequest;
import com.ecommerce.order.application.dto.OrderDto;
import com.ecommerce.order.domain.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單管理使用案例介面
 * 定義訂單相關的業務操作
 */
public interface OrderManagementUseCase {
    
    /**
     * 從購物車建立訂單
     */
    OrderDto createOrderFromCart(String customerId, CreateOrderRequest request);
    
    /**
     * 確認訂單
     */
    OrderDto confirmOrder(String orderId, String customerId);
    
    /**
     * 標記訂單為已付款
     */
    OrderDto markOrderAsPaid(String orderId, String customerId, String paymentMethod);
    
    /**
     * 出貨訂單
     */
    OrderDto shipOrder(String orderId);
    
    /**
     * 送達訂單
     */
    OrderDto deliverOrder(String orderId);
    
    /**
     * 取消訂單
     */
    OrderDto cancelOrder(String orderId, String customerId, String reason);
    
    /**
     * 退款訂單
     */
    OrderDto refundOrder(String orderId, String reason);
    
    /**
     * 取得訂單詳細資訊
     */
    OrderDto getOrder(String orderId, String customerId);
    
    /**
     * 取得客戶所有訂單
     */
    List<OrderDto> getCustomerOrders(String customerId);
    
    /**
     * 取得客戶指定狀態的訂單
     */
    List<OrderDto> getCustomerOrdersByStatus(String customerId, OrderStatus status);
    
    /**
     * 取得客戶指定日期範圍的訂單
     */
    List<OrderDto> getCustomerOrdersByDateRange(String customerId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 取得客戶最新訂單
     */
    OrderDto getLatestCustomerOrder(String customerId);
    
    /**
     * 檢查訂單是否可以取消
     */
    boolean canCancelOrder(String orderId, String customerId);
    
    /**
     * 取消超時訂單
     */
    List<OrderDto> cancelExpiredOrders(int timeoutHours);
}