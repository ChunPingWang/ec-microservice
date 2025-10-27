package com.ecommerce.order.infrastructure.adapter.persistence;

import com.ecommerce.common.architecture.PersistenceAdapter;
import com.ecommerce.order.application.port.out.OrderPersistencePort;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.order.infrastructure.adapter.persistence.entity.OrderJpaEntity;
import com.ecommerce.order.infrastructure.adapter.persistence.mapper.OrderJpaMapper;
import com.ecommerce.order.infrastructure.adapter.persistence.repository.OrderJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 訂單 JPA 適配器
 * 實作訂單持久化輸出埠
 */
@PersistenceAdapter
@Transactional
public class OrderJpaAdapter implements OrderPersistencePort {
    
    private final OrderJpaRepository orderJpaRepository;
    
    public OrderJpaAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }
    
    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = OrderJpaMapper.toJpaEntity(order);
        OrderJpaEntity savedEntity = orderJpaRepository.save(entity);
        return OrderJpaMapper.toDomainObject(savedEntity);
    }
    
    @Override
    public Optional<Order> findById(String orderId) {
        return orderJpaRepository.findById(orderId)
            .map(OrderJpaMapper::toDomainObject);
    }
    
    @Override
    public List<Order> findByCustomerId(String customerId) {
        List<OrderJpaEntity> entities = orderJpaRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
        return OrderJpaMapper.toDomainObjectList(entities);
    }
    
    @Override
    public List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status) {
        List<OrderJpaEntity> entities = orderJpaRepository.findByCustomerIdAndStatusOrderByOrderDateDesc(customerId, status);
        return OrderJpaMapper.toDomainObjectList(entities);
    }
    
    public List<Order> findByStatus(OrderStatus status) {
        List<OrderJpaEntity> entities = orderJpaRepository.findByStatusOrderByOrderDateDesc(status);
        return OrderJpaMapper.toDomainObjectList(entities);
    }
    
    public List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        List<OrderJpaEntity> entities = orderJpaRepository.findByOrderDateBetweenOrderByOrderDateDesc(startDate, endDate);
        return OrderJpaMapper.toDomainObjectList(entities);
    }
    
    @Override
    public List<Order> findByCustomerIdAndOrderDateBetween(String customerId, LocalDateTime startDate, LocalDateTime endDate) {
        List<OrderJpaEntity> entities = orderJpaRepository.findByCustomerIdAndOrderDateBetweenOrderByOrderDateDesc(
            customerId, startDate, endDate);
        return OrderJpaMapper.toDomainObjectList(entities);
    }
    
    @Override
    public List<Order> findPendingOrdersOlderThan(LocalDateTime cutoffTime) {
        List<OrderJpaEntity> entities = orderJpaRepository.findPendingOrdersOlderThan(cutoffTime);
        return OrderJpaMapper.toDomainObjectList(entities);
    }
    
    public long countByCustomerId(String customerId) {
        return orderJpaRepository.countByCustomerId(customerId);
    }
    
    public long countByStatus(OrderStatus status) {
        return orderJpaRepository.countByStatus(status);
    }
    
    @Override
    public boolean existsById(String orderId) {
        return orderJpaRepository.existsByOrderId(orderId);
    }
    
    @Override
    public void delete(Order order) {
        orderJpaRepository.deleteById(order.getOrderId());
    }
    
    @Override
    public void deleteById(String orderId) {
        orderJpaRepository.deleteById(orderId);
    }
    
    public List<Order> findOrdersContainingProduct(String productId) {
        List<OrderJpaEntity> entities = orderJpaRepository.findOrdersContainingProduct(productId);
        return OrderJpaMapper.toDomainObjectList(entities);
    }
    
    @Override
    public Optional<Order> findLatestOrderByCustomerId(String customerId) {
        return orderJpaRepository.findFirstByCustomerIdOrderByOrderDateDesc(customerId)
            .map(OrderJpaMapper::toDomainObject);
    }
}