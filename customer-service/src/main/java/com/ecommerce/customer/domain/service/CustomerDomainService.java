package com.ecommerce.customer.domain.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.customer.domain.model.Address;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.repository.CustomerRepository;

/**
 * Customer domain service for complex business logic
 * Implements business rules that span multiple entities or require external validation
 */
@DomainService
public class CustomerDomainService {
    
    private final CustomerRepository customerRepository;
    
    public CustomerDomainService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    /**
     * Register a new customer with validation
     * Ensures email and phone number uniqueness
     */
    public Customer registerCustomer(String firstName, String lastName, 
                                   String email, String phoneNumber) {
        
        // Check email uniqueness
        if (customerRepository.existsByEmail(email)) {
            throw new BusinessException("Email already registered: " + email);
        }
        
        // Check phone number uniqueness
        if (customerRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException("Phone number already registered: " + phoneNumber);
        }
        
        // Create and save customer
        Customer customer = Customer.create(firstName, lastName, email, phoneNumber);
        return customerRepository.save(customer);
    }
    
    /**
     * Update customer email with validation
     * Ensures new email is unique
     */
    public void updateCustomerEmail(String customerId, String newEmail) {
        Customer customer = findCustomerById(customerId);
        
        // Skip validation if email hasn't changed
        if (customer.getEmail().equals(newEmail)) {
            return;
        }
        
        // Check email uniqueness
        if (customerRepository.existsByEmail(newEmail)) {
            throw new BusinessException("Email already registered: " + newEmail);
        }
        
        // Update email through domain method (includes validation)
        customer.updatePersonalInfo(customer.getFirstName(), customer.getLastName(), 
                                  customer.getPhoneNumber());
        
        customerRepository.save(customer);
    }
    
    /**
     * Update customer phone number with validation
     * Ensures new phone number is unique
     */
    public void updateCustomerPhoneNumber(String customerId, String newPhoneNumber) {
        Customer customer = findCustomerById(customerId);
        
        // Skip validation if phone number hasn't changed
        if (customer.getPhoneNumber().equals(newPhoneNumber)) {
            return;
        }
        
        // Check phone number uniqueness
        if (customerRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new BusinessException("Phone number already registered: " + newPhoneNumber);
        }
        
        // Update phone number through domain method (includes validation)
        customer.updatePersonalInfo(customer.getFirstName(), customer.getLastName(), 
                                  newPhoneNumber);
        
        customerRepository.save(customer);
    }
    
    /**
     * Add address to customer with validation
     * Validates address format and business rules
     */
    public void addCustomerAddress(String customerId, Address address) {
        Customer customer = findCustomerById(customerId);
        
        // Validate address through domain method
        address.validate();
        
        // Add address through domain method
        customer.addAddress(address);
        
        customerRepository.save(customer);
    }
    
    /**
     * Validate Taipei address specifically
     * Additional validation for Taipei addresses as per requirements
     */
    public void validateTaipeiAddress(Address address) {
        if (!address.isTaipeiAddress()) {
            throw new ValidationException("Address is not a Taipei address");
        }
        
        // Perform Taipei-specific validation
        address.validate();
        
        // Additional business rules for Taipei addresses
        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            throw new ValidationException("Street address is required for Taipei addresses");
        }
        
        // Validate that the address can be used for delivery
        if (!isDeliverable(address)) {
            throw new ValidationException("Address is not suitable for delivery");
        }
    }
    
    /**
     * Check if customer can place orders
     * Business rule validation for order placement
     */
    public boolean canPlaceOrders(String customerId) {
        Customer customer = findCustomerById(customerId);
        
        // Customer must be active
        if (!customer.isActive()) {
            return false;
        }
        
        // Customer must have at least one valid address
        Address primaryAddress = customer.getPrimaryAddress();
        if (primaryAddress == null) {
            return false;
        }
        
        // Address must be deliverable
        return isDeliverable(primaryAddress);
    }
    
    /**
     * Deactivate inactive customers
     * Business rule for customer lifecycle management
     */
    public int deactivateInactiveCustomers(int inactiveDays) {
        var cutoffDate = java.time.LocalDateTime.now().minusDays(inactiveDays);
        var inactiveCustomers = customerRepository.findInactiveCustomers(cutoffDate);
        
        int deactivatedCount = 0;
        for (Customer customer : inactiveCustomers) {
            if (customer.isActive()) {
                customer.deactivate();
                customerRepository.save(customer);
                deactivatedCount++;
            }
        }
        
        return deactivatedCount;
    }
    
    /**
     * Merge duplicate customers
     * Business logic for handling duplicate customer accounts
     */
    public Customer mergeDuplicateCustomers(String primaryCustomerId, String duplicateCustomerId) {
        Customer primaryCustomer = findCustomerById(primaryCustomerId);
        Customer duplicateCustomer = findCustomerById(duplicateCustomerId);
        
        // Validate merge conditions
        if (primaryCustomer.getEmail().equals(duplicateCustomer.getEmail()) ||
            primaryCustomer.getPhoneNumber().equals(duplicateCustomer.getPhoneNumber())) {
            
            // Merge addresses from duplicate to primary
            for (Address address : duplicateCustomer.getAddresses()) {
                try {
                    primaryCustomer.addAddress(address);
                } catch (ValidationException e) {
                    // Skip duplicate addresses
                }
            }
            
            // Update last login if duplicate is more recent
            if (duplicateCustomer.getLastLoginDate() != null &&
                (primaryCustomer.getLastLoginDate() == null ||
                 duplicateCustomer.getLastLoginDate().isAfter(primaryCustomer.getLastLoginDate()))) {
                primaryCustomer.recordLogin();
            }
            
            // Save primary customer and delete duplicate
            Customer mergedCustomer = customerRepository.save(primaryCustomer);
            customerRepository.deleteById(duplicateCustomerId);
            
            return mergedCustomer;
        } else {
            throw new BusinessException("Customers cannot be merged - no matching email or phone");
        }
    }
    
    // Private helper methods
    private Customer findCustomerById(String customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new BusinessException("Customer not found: " + customerId));
    }
    
    private boolean isDeliverable(Address address) {
        // Basic deliverability check
        if (address == null) {
            return false;
        }
        
        // Must have all required fields
        if (address.getStreet() == null || address.getStreet().trim().isEmpty() ||
            address.getCity() == null || address.getCity().trim().isEmpty() ||
            address.getDistrict() == null || address.getDistrict().trim().isEmpty() ||
            address.getPostalCode() == null || address.getPostalCode().trim().isEmpty()) {
            return false;
        }
        
        // For Taiwan addresses, additional validation
        if (address.isTaiwanAddress()) {
            return isValidTaiwanDeliveryAddress(address);
        }
        
        return true;
    }
    
    private boolean isValidTaiwanDeliveryAddress(Address address) {
        // Taiwan-specific delivery validation
        try {
            address.validate();
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}