package com.ecommerce.customer.application.usecase;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.customer.application.dto.AddressDto;
import com.ecommerce.customer.application.dto.CreateAddressRequest;
import com.ecommerce.customer.application.dto.UpdateAddressRequest;
import com.ecommerce.customer.application.mapper.AddressMapper;
import com.ecommerce.customer.application.port.in.AddressManagementUseCase;
import com.ecommerce.customer.application.port.out.CustomerEventPort;
import com.ecommerce.customer.application.port.out.CustomerPersistencePort;
import com.ecommerce.customer.domain.event.CustomerAddressUpdatedEvent;
import com.ecommerce.customer.domain.exception.CustomerNotFoundException;
import com.ecommerce.customer.domain.model.Address;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.service.CustomerDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Address management service implementation
 * Implements address-related use cases
 */
@UseCase
@Service
@Transactional
public class AddressManagementService implements AddressManagementUseCase {
    
    private final CustomerPersistencePort customerPersistencePort;
    private final CustomerEventPort customerEventPort;
    private final CustomerDomainService customerDomainService;
    private final AddressMapper addressMapper;
    
    public AddressManagementService(CustomerPersistencePort customerPersistencePort,
                                  CustomerEventPort customerEventPort,
                                  CustomerDomainService customerDomainService,
                                  AddressMapper addressMapper) {
        this.customerPersistencePort = customerPersistencePort;
        this.customerEventPort = customerEventPort;
        this.customerDomainService = customerDomainService;
        this.addressMapper = addressMapper;
    }
    
    @Override
    public AddressDto addAddress(String customerId, CreateAddressRequest request) {
        Customer customer = findCustomerById(customerId);
        
        // Convert request to domain object
        Address address = addressMapper.fromCreateRequest(request);
        
        // Use domain service for address validation and addition
        customerDomainService.addCustomerAddress(customerId, address);
        
        // Find the added address (it will have a generated ID)
        Customer updatedCustomer = customerPersistencePort.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        Address addedAddress = updatedCustomer.getAddresses().stream()
            .filter(addr -> addr.getStreet().equals(request.getStreet()) &&
                           addr.getCity().equals(request.getCity()) &&
                           addr.getDistrict().equals(request.getDistrict()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to find added address"));
        
        // Publish domain event
        CustomerAddressUpdatedEvent event = new CustomerAddressUpdatedEvent(
            customerId,
            addedAddress.getAddressId(),
            addedAddress.getFormattedAddress(),
            addedAddress.isPrimary()
        );
        customerEventPort.publishCustomerAddressUpdated(event);
        
        return addressMapper.toDto(addedAddress);
    }
    
    @Override
    public AddressDto updateAddress(String customerId, String addressId, UpdateAddressRequest request) {
        Customer customer = findCustomerById(customerId);
        
        // Convert request to domain object
        Address updatedAddress = addressMapper.fromUpdateRequest(request);
        
        // Update address through domain method
        customer.updateAddress(addressId, updatedAddress);
        
        // Save customer
        Customer savedCustomer = customerPersistencePort.save(customer);
        
        // Find the updated address
        Address address = savedCustomer.getAddresses().stream()
            .filter(addr -> addr.getAddressId().equals(addressId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Address not found after update"));
        
        // Publish domain event
        CustomerAddressUpdatedEvent event = new CustomerAddressUpdatedEvent(
            customerId,
            addressId,
            address.getFormattedAddress(),
            address.isPrimary()
        );
        customerEventPort.publishCustomerAddressUpdated(event);
        
        return addressMapper.toDto(address);
    }
    
    @Override
    public void removeAddress(String customerId, String addressId) {
        Customer customer = findCustomerById(customerId);
        
        // Remove address through domain method
        customer.removeAddress(addressId);
        
        // Save customer
        customerPersistencePort.save(customer);
        
        // Publish domain event
        CustomerAddressUpdatedEvent event = new CustomerAddressUpdatedEvent(
            customerId,
            addressId,
            "Address removed",
            false
        );
        customerEventPort.publishCustomerAddressUpdated(event);
    }
    
    @Override
    public void setPrimaryAddress(String customerId, String addressId) {
        Customer customer = findCustomerById(customerId);
        
        // Set primary address through domain method
        customer.setPrimaryAddress(addressId);
        
        // Save customer
        Customer savedCustomer = customerPersistencePort.save(customer);
        
        // Find the primary address
        Address primaryAddress = savedCustomer.getPrimaryAddress();
        
        // Publish domain event
        CustomerAddressUpdatedEvent event = new CustomerAddressUpdatedEvent(
            customerId,
            addressId,
            primaryAddress != null ? primaryAddress.getFormattedAddress() : "Unknown",
            true
        );
        customerEventPort.publishCustomerAddressUpdated(event);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getCustomerAddresses(String customerId) {
        Customer customer = findCustomerById(customerId);
        return addressMapper.toDtoList(customer.getAddresses());
    }
    
    @Override
    @Transactional(readOnly = true)
    public AddressDto getPrimaryAddress(String customerId) {
        Customer customer = findCustomerById(customerId);
        Address primaryAddress = customer.getPrimaryAddress();
        return addressMapper.toDto(primaryAddress);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateTaipeiAddress(CreateAddressRequest request) {
        try {
            // Convert to domain object for validation
            Address address = addressMapper.fromCreateRequest(request);
            
            // Use domain service for Taipei-specific validation
            customerDomainService.validateTaipeiAddress(address);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Private helper methods
    private Customer findCustomerById(String customerId) {
        return customerPersistencePort.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}