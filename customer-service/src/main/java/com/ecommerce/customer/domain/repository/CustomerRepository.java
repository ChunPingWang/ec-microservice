package com.ecommerce.customer.domain.repository;

import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Customer repository interface following DDD repository pattern
 * Defines the contract for customer data access operations
 */
public interface CustomerRepository {
    
    /**
     * Save a customer entity
     * @param customer the customer to save
     * @return the saved customer
     */
    Customer save(Customer customer);
    
    /**
     * Find customer by ID
     * @param customerId the customer ID
     * @return optional customer
     */
    Optional<Customer> findById(String customerId);
    
    /**
     * Find customer by email address
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
     * @return list of customers with the specified status
     */
    List<Customer> findByStatus(CustomerStatus status);
    
    /**
     * Find customers registered within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of customers registered within the date range
     */
    List<Customer> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find customers by city
     * @param city the city name
     * @return list of customers in the specified city
     */
    List<Customer> findByCity(String city);
    
    /**
     * Find customers by name (first name or last name contains the search term)
     * @param searchTerm the search term
     * @return list of customers matching the search term
     */
    List<Customer> findByNameContaining(String searchTerm);
    
    /**
     * Check if email already exists
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone number already exists
     * @param phoneNumber the phone number to check
     * @return true if phone number exists, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Delete a customer by ID
     * @param customerId the customer ID
     */
    void deleteById(String customerId);
    
    /**
     * Count total number of customers
     * @return total customer count
     */
    long count();
    
    /**
     * Count customers by status
     * @param status the customer status
     * @return count of customers with the specified status
     */
    long countByStatus(CustomerStatus status);
    
    /**
     * Find all customers with pagination
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of customers for the specified page
     */
    List<Customer> findAll(int page, int size);
    
    /**
     * Find customers who haven't logged in since the specified date
     * @param lastLoginBefore the cutoff date
     * @return list of inactive customers
     */
    List<Customer> findInactiveCustomers(LocalDateTime lastLoginBefore);
}