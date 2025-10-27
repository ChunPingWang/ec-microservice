package com.ecommerce.customer.infrastructure.adapter.persistence;

import com.ecommerce.common.architecture.Adapter;
import com.ecommerce.customer.application.port.out.CustomerPersistencePort;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerStatus;
import com.ecommerce.customer.infrastructure.adapter.persistence.entity.CustomerJpaEntity;
import com.ecommerce.customer.infrastructure.adapter.persistence.mapper.CustomerJpaMapper;
import com.ecommerce.customer.infrastructure.adapter.persistence.repository.CustomerJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA adapter for customer persistence operations
 * Implements the CustomerPersistencePort using JPA
 */
@Adapter
@Component
public class CustomerJpaAdapter implements CustomerPersistencePort {
    
    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerJpaMapper customerJpaMapper;
    
    public CustomerJpaAdapter(CustomerJpaRepository customerJpaRepository,
                             CustomerJpaMapper customerJpaMapper) {
        this.customerJpaRepository = customerJpaRepository;
        this.customerJpaMapper = customerJpaMapper;
    }
    
    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = customerJpaMapper.toEntity(customer);
        CustomerJpaEntity savedEntity = customerJpaRepository.save(entity);
        return customerJpaMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Customer> findById(String customerId) {
        return customerJpaRepository.findById(customerId)
            .map(customerJpaMapper::toDomain);
    }
    
    @Override
    public Optional<Customer> findByEmail(String email) {
        return customerJpaRepository.findByEmail(email)
            .map(customerJpaMapper::toDomain);
    }
    
    @Override
    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        return customerJpaRepository.findByPhoneNumber(phoneNumber)
            .map(customerJpaMapper::toDomain);
    }
    
    @Override
    public List<Customer> findByStatus(CustomerStatus status) {
        return customerJpaRepository.findByStatus(status).stream()
            .map(customerJpaMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Customer> findByCity(String city) {
        return customerJpaRepository.findByAddressesCity(city).stream()
            .map(customerJpaMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Customer> findByNameContaining(String searchTerm) {
        return customerJpaRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            searchTerm, searchTerm).stream()
            .map(customerJpaMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return customerJpaRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return customerJpaRepository.existsByPhoneNumber(phoneNumber);
    }
    
    @Override
    public void deleteById(String customerId) {
        customerJpaRepository.deleteById(customerId);
    }
    
    @Override
    public List<Customer> findInactiveCustomers(LocalDateTime lastLoginBefore) {
        return customerJpaRepository.findByLastLoginDateBeforeOrLastLoginDateIsNull(lastLoginBefore).stream()
            .map(customerJpaMapper::toDomain)
            .collect(Collectors.toList());
    }
}