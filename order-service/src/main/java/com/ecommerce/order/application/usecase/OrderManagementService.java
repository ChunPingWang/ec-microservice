package com.ecommerce.order.application.usecase;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.order.application.dto.CreateOrderRequest;
import com.ecommerce.order.application.dto.OrderDto;
import com.ecommerce.order.application.mapper.OrderMapper;
import com.ecommerce.order.application.port.in.OrderManagementUseCase;
import com.ecommerce.order.application.port.out.CartPersistencePort;
import com.ecommerce.order.application.port.out.OrderEventPort;
import com.ecommerce.order.application.port.out.OrderPersistencePort;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.domain.exception.CartNotFoundException;
import com.ecommerce.order.domain.exception.InvalidOrderStateException;
import com.ecommerce.order.domain.exception.OrderNotFoundException;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.order.domain.service.CartDomainService;
import com.ecommerce.order.domain.service.OrderDomainService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單管理服務
 * 實作訂單相關的業務邏輯
 */
@UseCase
public class OrderManagementService implements OrderManagementUseCase {
    
    private final OrderPersistencePort orderPersistencePort;
    private final CartPersistencePort cartPersistencePort;
    private final ProductServicePort productServicePort;
    private final OrderEventPort orderEventPort;
    private final OrderDomainService orderDomainService;
    private final CartDomainService cartDomainService;
    
    public OrderManagementService(OrderPersistencePort orderPersistencePort,
                                CartPersistencePort cartPersistencePort,
                                ProductServicePort productServicePort,
                                OrderEventPort orderEventPort,
                                OrderDomainService orderDomainService,
                                CartDomainService cartDomainService) {
        this.orderPersistencePort = orderPersistencePort;
        this.cartPersistencePort = cartPersistencePort;
        this.productServicePort = productServicePort;
        this.orderEventPort = orderEventPort;
        this.orderDomainService = orderDomainService;
        this.cartDomainService = cartDomainService;
    }
    
    @Override
    public OrderDto createOrderFromCart(String customerId, CreateOrderRequest request) {
        // 獲取客戶的購物車
        Cart cart = cartPersistencePort.findByCustomerId(customerId)
            .orElseThrow(() -> CartNotFoundException.byCustomerId(customerId));
        
        // 驗證購物車是否可以建立訂單
        cartDomainService.validateCartForOrder(cart);
        
        // 驗證所有商品的庫存
        for (Cart.CartItem cartItem : cart.getCartItems()) {
            if (!productServicePort.isProductAvailable(cartItem.getProductId())) {
                throw new InvalidOrderStateException("Product is not available: " + cartItem.getProductName());
            }
            
            if (!productServicePort.hasAvailableStock(cartItem.getProductId(), cartItem.getQuantity())) {
                throw new InvalidOrderStateException("Insufficient stock for product: " + cartItem.getProductName());
            }
        }
        
        // 從購物車建立訂單
        Order order = cart.convertToOrder(
            request.getCustomerName(),
            request.getCustomerEmail(),
            request.getShippingAddress(),
            request.getBillingAddress()
        );
        
        // 設定備註
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        
        // 計算運費和稅額
        order.setShippingFee(orderDomainService.calculateShippingFee(order));
        order.setTaxAmount(orderDomainService.calculateTaxAmount(order));
        
        // 驗證訂單金額
        orderDomainService.validateOrderAmount(order);
        
        // 預留庫存
        for (Cart.CartItem cartItem : cart.getCartItems()) {
            productServicePort.reserveStock(cartItem.getProductId(), cartItem.getQuantity());
        }
        
        // 儲存訂單
        Order savedOrder = orderPersistencePort.save(order);
        
        // 清空購物車
        cart.clear();
        cartPersistencePort.save(cart);
        
        // 發布訂單建立事件
        orderEventPort.publishOrderCreated(savedOrder);
        
        return OrderMapper.toDto(savedOrder);
    }
    
    @Override
    public OrderDto confirmOrder(String orderId, String customerId) {
        Order order = getOrderByIdAndCustomer(orderId, customerId);
        
        order.confirm();
        Order savedOrder = orderPersistencePort.save(order);
        
        // 發布訂單確認事件
        orderEventPort.publishOrderConfirmed(savedOrder);
        
        return OrderMapper.toDto(savedOrder);
    }
    
    @Override
    public OrderDto markOrderAsPaid(String orderId, String customerId, String paymentMethod) {
        Order order = getOrderByIdAndCustomer(orderId, customerId);
        
        order.markAsPaid(paymentMethod);
        Order savedOrder = orderPersistencePort.save(order);
        
        // 確認庫存預留（扣減庫存）
        for (var orderItem : order.getOrderItems()) {
            productServicePort.confirmStockReservation(orderItem.getProductId(), orderItem.getQuantity());
        }
        
        // 發布訂單付款事件
        orderEventPort.publishOrderPaid(savedOrder);
        
        return OrderMapper.toDto(savedOrder);
    }
    
    @Override
    public OrderDto shipOrder(String orderId) {
        Order order = orderPersistencePort.findById(orderId)
            .orElseThrow(() -> OrderNotFoundException.byOrderId(orderId));
        
        order.ship();
        Order savedOrder = orderPersistencePort.save(order);
        
        // 發布訂單出貨事件
        orderEventPort.publishOrderShipped(savedOrder);
        
        return OrderMapper.toDto(savedOrder);
    }
    
    @Override
    public OrderDto deliverOrder(String orderId) {
        Order order = orderPersistencePort.findById(orderId)
            .orElseThrow(() -> OrderNotFoundException.byOrderId(orderId));
        
        order.deliver();
        Order savedOrder = orderPersistencePort.save(order);
        
        // 發布訂單送達事件
        orderEventPort.publishOrderDelivered(savedOrder);
        
        return OrderMapper.toDto(savedOrder);
    }
    
    @Override
    public OrderDto cancelOrder(String orderId, String customerId, String reason) {
        Order order = getOrderByIdAndCustomer(orderId, customerId);
        
        // 釋放庫存預留
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
            for (var orderItem : order.getOrderItems()) {
                productServicePort.releaseStockReservation(orderItem.getProductId(), orderItem.getQuantity());
            }
        }
        
        order.cancel(reason);
        Order savedOrder = orderPersistencePort.save(order);
        
        // 發布訂單取消事件
        orderEventPort.publishOrderCancelled(savedOrder, reason);
        
        return OrderMapper.toDto(savedOrder);
    }
    
    @Override
    public OrderDto refundOrder(String orderId, String reason) {
        Order order = orderPersistencePort.findById(orderId)
            .orElseThrow(() -> OrderNotFoundException.byOrderId(orderId));
        
        order.refund();
        Order savedOrder = orderPersistencePort.save(order);
        
        // 發布訂單退款事件
        orderEventPort.publishOrderRefunded(savedOrder, reason);
        
        return OrderMapper.toDto(savedOrder);
    }
    
    @Override
    public OrderDto getOrder(String orderId, String customerId) {
        Order order = getOrderByIdAndCustomer(orderId, customerId);
        return OrderMapper.toDto(order);
    }
    
    @Override
    public List<OrderDto> getCustomerOrders(String customerId) {
        List<Order> orders = orderPersistencePort.findByCustomerId(customerId);
        return OrderMapper.toDtoList(orders);
    }
    
    @Override
    public List<OrderDto> getCustomerOrdersByStatus(String customerId, OrderStatus status) {
        List<Order> orders = orderPersistencePort.findByCustomerIdAndStatus(customerId, status);
        return OrderMapper.toDtoList(orders);
    }
    
    @Override
    public List<OrderDto> getCustomerOrdersByDateRange(String customerId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderPersistencePort.findByCustomerIdAndOrderDateBetween(customerId, startDate, endDate);
        return OrderMapper.toDtoList(orders);
    }
    
    @Override
    public OrderDto getLatestCustomerOrder(String customerId) {
        Order order = orderPersistencePort.findLatestOrderByCustomerId(customerId)
            .orElseThrow(() -> OrderNotFoundException.byCustomerId(customerId));
        return OrderMapper.toDto(order);
    }
    
    @Override
    public boolean canCancelOrder(String orderId, String customerId) {
        Order order = getOrderByIdAndCustomer(orderId, customerId);
        return orderDomainService.canCancelOrder(orderId);
    }
    
    @Override
    public List<OrderDto> cancelExpiredOrders(int timeoutHours) {
        List<Order> expiredOrders = orderDomainService.cancelExpiredOrders(timeoutHours);
        
        // 為每個取消的訂單釋放庫存預留
        for (Order order : expiredOrders) {
            for (var orderItem : order.getOrderItems()) {
                productServicePort.releaseStockReservation(orderItem.getProductId(), orderItem.getQuantity());
            }
            // 發布訂單取消事件
            orderEventPort.publishOrderCancelled(order, "自動取消：超時未付款");
        }
        
        return OrderMapper.toDtoList(expiredOrders);
    }
    
    // Private helper methods
    private Order getOrderByIdAndCustomer(String orderId, String customerId) {
        Order order = orderPersistencePort.findById(orderId)
            .orElseThrow(() -> OrderNotFoundException.byOrderId(orderId));
        
        orderDomainService.validateCustomerOrderAccess(orderId, customerId);
        
        return order;
    }
}