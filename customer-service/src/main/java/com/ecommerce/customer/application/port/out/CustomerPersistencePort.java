package com.ecommerce.customer.application.port.out;

import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output port for customer persistence operations
 * Defines the contract for customer data access
 */
public interface CustomerPersistencePort {
    
    /**
     * Save customer
     * @param customer the customer to save
     * @return saved customer
     */
    Customer save(Customer customer);
    
    /**
     * Find customer by ID
     * @param customerId the customer ID
     * @return optional customer
     */
    Optional<Customer> findById(String customerId);
    
    /**
     * Find customer by email
     * @param email the email address
     * @return optional customer
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Find customer by phone number
     * @param phoneNumber the phone number
     * @return optional customer
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find customers by status
     * @param status the customer status
     * @return list of customers
     */
    List<Customer> findByStatus(CustomerStatus status);
    
    /**
     * Find customers by city
     * @param city the city name
     * @return list of customers
     */
    List<Customer> findByCity(String city);
    
    /**
     * Find customers by name containing search term
     * @param searchTerm the search term
     * @return list of matching customers
     */
    List<Customer> findByNameContaining(String searchTerm);
    
    /**
     * Check if email exists
     * @param email the email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone number exists
     * @param phoneNumber the phone number to check
     * @return true if exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Delete customer by ID
     * @param customerId the customer ID
     */
    void deleteById(String customerId);
    
    /**
     * Find inactive customers
     * @param lastLoginBefore cutoff date for last login
     * @return list of inactive customers
     */
    List<Customer> findInactiveCustomers(LocalDateTime lastLoginBefore);
}