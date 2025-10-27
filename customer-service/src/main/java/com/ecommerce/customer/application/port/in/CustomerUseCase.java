package com.ecommerce.customer.application.port.in;

import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.dto.CreateCustomerRequest;
import com.ecommerce.customer.application.dto.UpdateCustomerRequest;

import java.util.List;

/**
 * Input port for customer management use cases
 * Defines the contract for customer-related operations
 */
public interface CustomerUseCase {
    
    /**
     * Register a new customer
     * @param request customer registration details
     * @return created customer DTO
     */
    CustomerDto registerCustomer(CreateCustomerRequest request);
    
    /**
     * Get customer by ID
     * @param customerId the customer ID
     * @return customer DTO
     */
    CustomerDto getCustomer(String customerId);
    
    /**
     * Get customer by email
     * @param email the customer email
     * @return customer DTO
     */
    CustomerDto getCustomerByEmail(String email);
    
    /**
     * Update customer information
     * @param customerId the customer ID
     * @param request update details
     * @return updated customer DTO
     */
    CustomerDto updateCustomer(String customerId, UpdateCustomerRequest request);
    
    /**
     * Deactivate customer account
     * @param customerId the customer ID
     */
    void deactivateCustomer(String customerId);
    
    /**
     * Activate customer account
     * @param customerId the customer ID
     */
    void activateCustomer(String customerId);
    
    /**
     * Record customer login
     * @param customerId the customer ID
     */
    void recordLogin(String customerId);
    
    /**
     * Search customers by name
     * @param searchTerm the search term
     * @return list of matching customers
     */
    List<CustomerDto> searchCustomers(String searchTerm);
    
    /**
     * Get customers by city
     * @param city the city name
     * @return list of customers in the city
     */
    List<CustomerDto> getCustomersByCity(String city);
    
    /**
     * Check if customer can place orders
     * @param customerId the customer ID
     * @return true if customer can place orders
     */
    boolean canPlaceOrders(String customerId);
}