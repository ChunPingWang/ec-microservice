package com.ecommerce.order.application.port.out;

import com.ecommerce.order.domain.model.Order;

/**
 * 訂單事件端口
 * 定義訂單事件發布的介面
 */
public interface OrderEventPort {
    
    /**
     * 發布訂單建立事件
     */
    void publishOrderCreated(Order order);
    
    /**
     * 發布訂單確認事件
     */
    void publishOrderConfirmed(Order order);
    
    /**
     * 發布訂單付款事件
     */
    void publishOrderPaid(Order order);
    
    /**
     * 發布訂單出貨事件
     */
    void publishOrderShipped(Order order);
    
    /**
     * 發布訂單送達事件
     */
    void publishOrderDelivered(Order order);
    
    /**
     * 發布訂單取消事件
     */
    void publishOrderCancelled(Order order, String reason);
    
    /**
     * 發布訂單退款事件
     */
    void publishOrderRefunded(Order order, String reason);
}