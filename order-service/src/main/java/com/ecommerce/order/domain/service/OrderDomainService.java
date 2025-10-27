package com.ecommerce.order.domain.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.order.domain.exception.InvalidOrderStateException;
import com.ecommerce.order.domain.exception.OrderNotFoundException;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單領域服務
 * 處理複雜的訂單業務邏輯和規則
 */
@DomainService
public class OrderDomainService {
    
    private final OrderRepository orderRepository;
    
    public OrderDomainService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    /**
     * 驗證訂單是否可以進行狀態轉換
     */
    public void validateOrderStatusTransition(String orderId, OrderStatus targetStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> OrderNotFoundException.byOrderId(orderId));
        
        if (!order.getStatus().canTransitionTo(targetStatus)) {
            throw InvalidOrderStateException.cannotTransition(order.getStatus(), targetStatus);
        }
    }
    
    /**
     * 計算訂單的運費
     */
    public BigDecimal calculateShippingFee(Order order) {
        BigDecimal totalAmount = order.getTotalAmount();
        
        // 免運費門檻：滿 1000 元免運費
        if (totalAmount.compareTo(new BigDecimal("1000")) >= 0) {
            return BigDecimal.ZERO;
        }
        
        // 台北市內運費 60 元，其他地區 100 元
        String shippingAddress = order.getShippingAddress();
        if (shippingAddress.contains("台北市")) {
            return new BigDecimal("60");
        } else {
            return new BigDecimal("100");
        }
    }
    
    /**
     * 計算訂單的稅額（5% 營業稅）
     */
    public BigDecimal calculateTaxAmount(Order order) {
        BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal shippingFee = order.getShippingFee();
        BigDecimal taxableAmount = totalAmount.add(shippingFee);
        
        // 5% 營業稅
        return taxableAmount.multiply(new BigDecimal("0.05"));
    }
    
    /**
     * 檢查訂單是否可以取消
     */
    public boolean canCancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> OrderNotFoundException.byOrderId(orderId));
        
        return order.getStatus().isCancellable();
    }
    
    /**
     * 自動取消超時未付款的訂單
     */
    public List<Order> cancelExpiredOrders(int timeoutHours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(timeoutHours);
        List<Order> expiredOrders = orderRepository.findPendingOrdersOlderThan(cutoffTime);
        
        for (Order order : expiredOrders) {
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
                order.cancel("自動取消：超時未付款");
                orderRepository.save(order);
            }
        }
        
        return expiredOrders;
    }
    
    /**
     * 驗證訂單金額的合理性
     */
    public void validateOrderAmount(Order order) {
        BigDecimal totalAmount = order.getTotalAmount();
        
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderStateException("Order total amount must be greater than zero");
        }
        
        // 檢查單筆訂單金額上限（100萬元）
        BigDecimal maxOrderAmount = new BigDecimal("1000000");
        if (totalAmount.compareTo(maxOrderAmount) > 0) {
            throw new InvalidOrderStateException("Order amount exceeds maximum limit: " + maxOrderAmount);
        }
        
        // 檢查最終金額是否正確計算
        BigDecimal expectedFinalAmount = totalAmount.add(order.getShippingFee()).add(order.getTaxAmount());
        if (order.getFinalAmount().compareTo(expectedFinalAmount) != 0) {
            throw new InvalidOrderStateException("Order final amount calculation is incorrect");
        }
    }
    
    /**
     * 檢查客戶是否有權限操作訂單
     */
    public void validateCustomerOrderAccess(String orderId, String customerId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> OrderNotFoundException.byOrderId(orderId));
        
        if (!order.getCustomerId().equals(customerId)) {
            throw new InvalidOrderStateException("Customer does not have access to this order");
        }
    }
    
    /**
     * 獲取客戶的訂單統計資訊
     */
    public CustomerOrderStats getCustomerOrderStats(String customerId) {
        List<Order> customerOrders = orderRepository.findByCustomerId(customerId);
        
        long totalOrders = customerOrders.size();
        long completedOrders = customerOrders.stream()
            .mapToLong(order -> order.isCompleted() ? 1 : 0)
            .sum();
        long cancelledOrders = customerOrders.stream()
            .mapToLong(order -> order.isCancelled() ? 1 : 0)
            .sum();
        
        BigDecimal totalSpent = customerOrders.stream()
            .filter(Order::isCompleted)
            .map(Order::getFinalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new CustomerOrderStats(customerId, totalOrders, completedOrders, cancelledOrders, totalSpent);
    }
    
    /**
     * 檢查訂單是否包含指定商品
     */
    public boolean orderContainsProduct(String orderId, String productId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> OrderNotFoundException.byOrderId(orderId));
        
        return order.getOrderItems().stream()
            .anyMatch(item -> item.getProductId().equals(productId));
    }
    
    /**
     * 客戶訂單統計資訊
     */
    public static class CustomerOrderStats {
        private final String customerId;
        private final long totalOrders;
        private final long completedOrders;
        private final long cancelledOrders;
        private final BigDecimal totalSpent;
        
        public CustomerOrderStats(String customerId, long totalOrders, long completedOrders, 
                                long cancelledOrders, BigDecimal totalSpent) {
            this.customerId = customerId;
            this.totalOrders = totalOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalSpent = totalSpent;
        }
        
        // Getters
        public String getCustomerId() { return customerId; }
        public long getTotalOrders() { return totalOrders; }
        public long getCompletedOrders() { return completedOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        
        public double getCompletionRate() {
            return totalOrders > 0 ? (double) completedOrders / totalOrders : 0.0;
        }
        
        public double getCancellationRate() {
            return totalOrders > 0 ? (double) cancelledOrders / totalOrders : 0.0;
        }
    }
}