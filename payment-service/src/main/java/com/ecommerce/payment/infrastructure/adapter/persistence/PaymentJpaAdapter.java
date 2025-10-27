package com.ecommerce.payment.infrastructure.adapter.persistence;

import com.ecommerce.common.architecture.PersistenceAdapter;
import com.ecommerce.payment.application.port.out.PaymentPersistencePort;
import com.ecommerce.payment.domain.model.PaymentStatus;
import com.ecommerce.payment.domain.model.PaymentTransaction;
import com.ecommerce.payment.infrastructure.adapter.persistence.entity.PaymentTransactionJpaEntity;
import com.ecommerce.payment.infrastructure.adapter.persistence.mapper.PaymentTransactionJpaMapper;
import com.ecommerce.payment.infrastructure.adapter.persistence.repository.PaymentTransactionJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 付款持久化適配器
 * 實作付款資料的持久化操作
 */
@Component
@PersistenceAdapter
@Transactional
public class PaymentJpaAdapter implements PaymentPersistencePort {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentJpaAdapter.class);
    
    private final PaymentTransactionJpaRepository repository;
    private final PaymentTransactionJpaMapper mapper;
    
    public PaymentJpaAdapter(PaymentTransactionJpaRepository repository, 
                           PaymentTransactionJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    @Override
    public PaymentTransaction save(PaymentTransaction transaction) {
        logger.debug("Saving payment transaction: {}", transaction.getTransactionId());
        
        try {
            PaymentTransactionJpaEntity jpaEntity = mapper.toJpaEntity(transaction);
            PaymentTransactionJpaEntity savedEntity = repository.save(jpaEntity);
            
            PaymentTransaction savedTransaction = mapper.toDomainEntity(savedEntity);
            
            logger.info("Payment transaction saved successfully: {}", transaction.getTransactionId());
            return savedTransaction;
            
        } catch (Exception e) {
            logger.error("Failed to save payment transaction: {}", transaction.getTransactionId(), e);
            throw new RuntimeException("Failed to save payment transaction", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> findById(String transactionId) {
        logger.debug("Finding payment transaction by ID: {}", transactionId);
        
        try {
            Optional<PaymentTransactionJpaEntity> jpaEntity = repository.findById(transactionId);
            
            if (jpaEntity.isPresent()) {
                PaymentTransaction transaction = mapper.toDomainEntity(jpaEntity.get());
                logger.debug("Payment transaction found: {}", transactionId);
                return Optional.of(transaction);
            } else {
                logger.debug("Payment transaction not found: {}", transactionId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Failed to find payment transaction: {}", transactionId, e);
            throw new RuntimeException("Failed to find payment transaction", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> findByOrderId(String orderId) {
        logger.debug("Finding payment transaction by order ID: {}", orderId);
        
        try {
            Optional<PaymentTransactionJpaEntity> jpaEntity = repository.findByOrderId(orderId);
            
            if (jpaEntity.isPresent()) {
                PaymentTransaction transaction = mapper.toDomainEntity(jpaEntity.get());
                logger.debug("Payment transaction found for order: {}", orderId);
                return Optional.of(transaction);
            } else {
                logger.debug("Payment transaction not found for order: {}", orderId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Failed to find payment transaction for order: {}", orderId, e);
            throw new RuntimeException("Failed to find payment transaction for order", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> findAllByOrderId(String orderId) {
        logger.debug("Finding all payment transactions for order: {}", orderId);
        
        try {
            List<PaymentTransactionJpaEntity> jpaEntities = repository.findAllByOrderId(orderId);
            
            List<PaymentTransaction> transactions = jpaEntities.stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} payment transactions for order: {}", transactions.size(), orderId);
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to find payment transactions for order: {}", orderId, e);
            throw new RuntimeException("Failed to find payment transactions for order", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> findByCustomerId(String customerId) {
        logger.debug("Finding payment transactions for customer: {}", customerId);
        
        try {
            List<PaymentTransactionJpaEntity> jpaEntities = repository.findByCustomerId(customerId);
            
            List<PaymentTransaction> transactions = jpaEntities.stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} payment transactions for customer: {}", transactions.size(), customerId);
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to find payment transactions for customer: {}", customerId, e);
            throw new RuntimeException("Failed to find payment transactions for customer", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> findByCustomerIdAndStatus(String customerId, PaymentStatus status) {
        logger.debug("Finding payment transactions for customer: {} with status: {}", customerId, status);
        
        try {
            List<PaymentTransactionJpaEntity> jpaEntities = 
                    repository.findByCustomerIdAndStatus(customerId, status);
            
            List<PaymentTransaction> transactions = jpaEntities.stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} payment transactions for customer: {} with status: {}", 
                        transactions.size(), customerId, status);
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to find payment transactions for customer: {} with status: {}", 
                        customerId, status, e);
            throw new RuntimeException("Failed to find payment transactions for customer with status", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> findByStatus(PaymentStatus status) {
        logger.debug("Finding payment transactions with status: {}", status);
        
        try {
            List<PaymentTransactionJpaEntity> jpaEntities = repository.findByStatus(status);
            
            List<PaymentTransaction> transactions = jpaEntities.stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} payment transactions with status: {}", transactions.size(), status);
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to find payment transactions with status: {}", status, e);
            throw new RuntimeException("Failed to find payment transactions with status", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> findByGatewayTransactionId(String gatewayTransactionId) {
        logger.debug("Finding payment transaction by gateway transaction ID: {}", gatewayTransactionId);
        
        try {
            Optional<PaymentTransactionJpaEntity> jpaEntity = 
                    repository.findByGatewayTransactionId(gatewayTransactionId);
            
            if (jpaEntity.isPresent()) {
                PaymentTransaction transaction = mapper.toDomainEntity(jpaEntity.get());
                logger.debug("Payment transaction found for gateway transaction: {}", gatewayTransactionId);
                return Optional.of(transaction);
            } else {
                logger.debug("Payment transaction not found for gateway transaction: {}", gatewayTransactionId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Failed to find payment transaction for gateway transaction: {}", 
                        gatewayTransactionId, e);
            throw new RuntimeException("Failed to find payment transaction for gateway transaction", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Finding payment transactions between: {} and {}", startDate, endDate);
        
        try {
            List<PaymentTransactionJpaEntity> jpaEntities = 
                    repository.findByCreatedAtBetween(startDate, endDate);
            
            List<PaymentTransaction> transactions = jpaEntities.stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} payment transactions between dates", transactions.size());
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to find payment transactions between dates", e);
            throw new RuntimeException("Failed to find payment transactions between dates", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> findSuccessfulPaymentsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Finding successful payment transactions between: {} and {}", startDate, endDate);
        
        try {
            List<PaymentTransactionJpaEntity> jpaEntities = 
                    repository.findSuccessfulPaymentsBetween(startDate, endDate);
            
            List<PaymentTransaction> transactions = jpaEntities.stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} successful payment transactions between dates", transactions.size());
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to find successful payment transactions between dates", e);
            throw new RuntimeException("Failed to find successful payment transactions between dates", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> findTimeoutTransactions(LocalDateTime cutoffTime) {
        logger.debug("Finding timeout payment transactions before: {}", cutoffTime);
        
        try {
            List<PaymentTransactionJpaEntity> jpaEntities = repository.findTimeoutTransactions(cutoffTime);
            
            List<PaymentTransaction> transactions = jpaEntities.stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} timeout payment transactions", transactions.size());
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to find timeout payment transactions", e);
            throw new RuntimeException("Failed to find timeout payment transactions", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPaymentsByCustomer(String customerId) {
        logger.debug("Calculating total payments for customer: {}", customerId);
        
        try {
            BigDecimal total = repository.calculateTotalPaymentsByCustomer(customerId);
            logger.debug("Total payments for customer {}: {}", customerId, total);
            return total;
            
        } catch (Exception e) {
            logger.error("Failed to calculate total payments for customer: {}", customerId, e);
            throw new RuntimeException("Failed to calculate total payments for customer", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPaymentsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Calculating total payments between: {} and {}", startDate, endDate);
        
        try {
            BigDecimal total = repository.calculateTotalPaymentsBetween(startDate, endDate);
            logger.debug("Total payments between dates: {}", total);
            return total;
            
        } catch (Exception e) {
            logger.error("Failed to calculate total payments between dates", e);
            throw new RuntimeException("Failed to calculate total payments between dates", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String transactionId) {
        logger.debug("Checking if payment transaction exists: {}", transactionId);
        
        try {
            boolean exists = repository.existsById(transactionId);
            logger.debug("Payment transaction {} exists: {}", transactionId, exists);
            return exists;
            
        } catch (Exception e) {
            logger.error("Failed to check if payment transaction exists: {}", transactionId, e);
            throw new RuntimeException("Failed to check if payment transaction exists", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasSuccessfulPaymentForOrder(String orderId) {
        logger.debug("Checking if order has successful payment: {}", orderId);
        
        try {
            boolean hasPayment = repository.hasSuccessfulPaymentForOrder(orderId);
            logger.debug("Order {} has successful payment: {}", orderId, hasPayment);
            return hasPayment;
            
        } catch (Exception e) {
            logger.error("Failed to check if order has successful payment: {}", orderId, e);
            throw new RuntimeException("Failed to check if order has successful payment", e);
        }
    }
    
    @Override
    public void delete(PaymentTransaction transaction) {
        logger.debug("Deleting payment transaction: {}", transaction.getTransactionId());
        
        try {
            PaymentTransactionJpaEntity jpaEntity = mapper.toJpaEntity(transaction);
            repository.delete(jpaEntity);
            
            logger.info("Payment transaction deleted successfully: {}", transaction.getTransactionId());
            
        } catch (Exception e) {
            logger.error("Failed to delete payment transaction: {}", transaction.getTransactionId(), e);
            throw new RuntimeException("Failed to delete payment transaction", e);
        }
    }
    
    @Override
    public void deleteById(String transactionId) {
        logger.debug("Deleting payment transaction by ID: {}", transactionId);
        
        try {
            repository.deleteById(transactionId);
            logger.info("Payment transaction deleted successfully: {}", transactionId);
            
        } catch (Exception e) {
            logger.error("Failed to delete payment transaction: {}", transactionId, e);
            throw new RuntimeException("Failed to delete payment transaction", e);
        }
    }
}