package com.ecommerce.customer.infrastructure.config;

import com.ecommerce.customer.application.port.out.CustomerEventPort;
import com.ecommerce.customer.application.port.out.CustomerPersistencePort;
import com.ecommerce.customer.domain.repository.CustomerRepository;
import com.ecommerce.customer.domain.service.CustomerDomainService;
import com.ecommerce.customer.infrastructure.adapter.persistence.CustomerJpaAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Customer service
 * Configures dependency injection for hexagonal architecture
 */
@Configuration
public class CustomerConfig {
    
    /**
     * Configure CustomerRepository implementation
     * Maps domain repository interface to JPA adapter
     */
    @Bean
    public CustomerRepository customerRepository(CustomerPersistencePort customerPersistencePort) {
        // Create an adapter that implements CustomerRepository using CustomerPersistencePort
        return new CustomerRepository() {
            @Override
            public com.ecommerce.customer.domain.model.Customer save(com.ecommerce.customer.domain.model.Customer customer) {
                return customerPersistencePort.save(customer);
            }
            
            @Override
            public java.util.Optional<com.ecommerce.customer.domain.model.Customer> findById(String customerId) {
                return customerPersistencePort.findById(customerId);
            }
            
            @Override
            public java.util.Optional<com.ecommerce.customer.domain.model.Customer> findByEmail(String email) {
                return customerPersistencePort.findByEmail(email);
            }
            
            @Override
            public java.util.Optional<com.ecommerce.customer.domain.model.Customer> findByPhoneNumber(String phoneNumber) {
                return customerPersistencePort.findByPhoneNumber(phoneNumber);
            }
            
            @Override
            public java.util.List<com.ecommerce.customer.domain.model.Customer> findByStatus(com.ecommerce.customer.domain.model.CustomerStatus status) {
                return customerPersistencePort.findByStatus(status);
            }
            
            @Override
            public java.util.List<com.ecommerce.customer.domain.model.Customer> findByRegistrationDateBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
                // This method would need to be added to CustomerPersistencePort if needed
                throw new UnsupportedOperationException("Not implemented in persistence port");
            }
            
            @Override
            public java.util.List<com.ecommerce.customer.domain.model.Customer> findByCity(String city) {
                return customerPersistencePort.findByCity(city);
            }
            
            @Override
            public java.util.List<com.ecommerce.customer.domain.model.Customer> findByNameContaining(String searchTerm) {
                return customerPersistencePort.findByNameContaining(searchTerm);
            }
            
            @Override
            public boolean existsByEmail(String email) {
                return customerPersistencePort.existsByEmail(email);
            }
            
            @Override
            public boolean existsByPhoneNumber(String phoneNumber) {
                return customerPersistencePort.existsByPhoneNumber(phoneNumber);
            }
            
            @Override
            public void deleteById(String customerId) {
                customerPersistencePort.deleteById(customerId);
            }
            
            @Override
            public long count() {
                // This method would need to be added to CustomerPersistencePort if needed
                throw new UnsupportedOperationException("Not implemented in persistence port");
            }
            
            @Override
            public long countByStatus(com.ecommerce.customer.domain.model.CustomerStatus status) {
                // This method would need to be added to CustomerPersistencePort if needed
                throw new UnsupportedOperationException("Not implemented in persistence port");
            }
            
            @Override
            public java.util.List<com.ecommerce.customer.domain.model.Customer> findAll(int page, int size) {
                // This method would need to be added to CustomerPersistencePort if needed
                throw new UnsupportedOperationException("Not implemented in persistence port");
            }
            
            @Override
            public java.util.List<com.ecommerce.customer.domain.model.Customer> findInactiveCustomers(java.time.LocalDateTime lastLoginBefore) {
                return customerPersistencePort.findInactiveCustomers(lastLoginBefore);
            }
        };
    }
    
    /**
     * Configure CustomerDomainService
     * Injects the repository dependency
     */
    @Bean
    public CustomerDomainService customerDomainService(CustomerRepository customerRepository) {
        return new CustomerDomainService(customerRepository);
    }
}