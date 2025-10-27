package com.ecommerce.customer.application.usecase;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.dto.CreateCustomerRequest;
import com.ecommerce.customer.application.dto.UpdateCustomerRequest;
import com.ecommerce.customer.application.mapper.CustomerMapper;
import com.ecommerce.customer.application.port.in.CustomerUseCase;
import com.ecommerce.customer.application.port.out.CustomerEventPort;
import com.ecommerce.customer.application.port.out.CustomerPersistencePort;
import com.ecommerce.customer.domain.event.CustomerRegisteredEvent;
import com.ecommerce.customer.domain.exception.CustomerNotFoundException;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.service.CustomerDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Customer service implementation
 * Implements customer management use cases
 */
@UseCase
@Service
@Transactional
public class CustomerService implements CustomerUseCase {
    
    private final CustomerPersistencePort customerPersistencePort;
    private final CustomerEventPort customerEventPort;
    private final CustomerDomainService customerDomainService;
    private final CustomerMapper customerMapper;
    
    public CustomerService(CustomerPersistencePort customerPersistencePort,
                          CustomerEventPort customerEventPort,
                          CustomerDomainService customerDomainService,
                          CustomerMapper customerMapper) {
        this.customerPersistencePort = customerPersistencePort;
        this.customerEventPort = customerEventPort;
        this.customerDomainService = customerDomainService;
        this.customerMapper = customerMapper;
    }
    
    @Override
    public CustomerDto registerCustomer(CreateCustomerRequest request) {
        // Use domain service for registration with business rules
        Customer customer = customerDomainService.registerCustomer(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhoneNumber()
        );
        
        // Publish domain event
        CustomerRegisteredEvent event = new CustomerRegisteredEvent(
            customer.getCustomerId(),
            customer.getEmail(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getRegistrationDate()
        );
        customerEventPort.publishCustomerRegistered(event);
        
        return customerMapper.toDto(customer);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomer(String customerId) {
        Customer customer = customerPersistencePort.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        return customerMapper.toDto(customer);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        Customer customer = customerPersistencePort.findByEmail(email)
            .orElseThrow(() -> new CustomerNotFoundException("email", email));
        
        return customerMapper.toDto(customer);
    }
    
    @Override
    public CustomerDto updateCustomer(String customerId, UpdateCustomerRequest request) {
        Customer customer = customerPersistencePort.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        // Update personal information
        if (request.getFirstName() != null || request.getLastName() != null || 
            request.getPhoneNumber() != null) {
            
            String firstName = request.getFirstName() != null ? 
                request.getFirstName() : customer.getFirstName();
            String lastName = request.getLastName() != null ? 
                request.getLastName() : customer.getLastName();
            String phoneNumber = request.getPhoneNumber() != null ? 
                request.getPhoneNumber() : customer.getPhoneNumber();
            
            customer.updatePersonalInfo(firstName, lastName, phoneNumber);
        }
        
        // Update email separately with domain service validation
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            customerDomainService.updateCustomerEmail(customerId, request.getEmail());
        }
        
        Customer updatedCustomer = customerPersistencePort.save(customer);
        return customerMapper.toDto(updatedCustomer);
    }
    
    @Override
    public void deactivateCustomer(String customerId) {
        Customer customer = customerPersistencePort.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        customer.deactivate();
        customerPersistencePort.save(customer);
    }
    
    @Override
    public void activateCustomer(String customerId) {
        Customer customer = customerPersistencePort.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        customer.activate();
        customerPersistencePort.save(customer);
    }
    
    @Override
    public void recordLogin(String customerId) {
        Customer customer = customerPersistencePort.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        customer.recordLogin();
        customerPersistencePort.save(customer);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> searchCustomers(String searchTerm) {
        List<Customer> customers = customerPersistencePort.findByNameContaining(searchTerm);
        return customerMapper.toDtoList(customers);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> getCustomersByCity(String city) {
        List<Customer> customers = customerPersistencePort.findByCity(city);
        return customerMapper.toDtoList(customers);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canPlaceOrders(String customerId) {
        return customerDomainService.canPlaceOrders(customerId);
    }
}