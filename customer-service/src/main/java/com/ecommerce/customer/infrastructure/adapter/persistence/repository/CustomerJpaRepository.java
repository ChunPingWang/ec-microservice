package com.ecommerce.customer.infrastructure.adapter.persistence.repository;

import com.ecommerce.customer.domain.model.CustomerStatus;
import com.ecommerce.customer.infrastructure.adapter.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for Customer entities
 * Provides data access methods for customer operations
 */
@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, String> {
    
    /**
     * Find customer by email address
     */
    Optional<CustomerJpaEntity> findByEmail(String email);
    
    /**
     * Find customer by phone number
     */
    Optional<CustomerJpaEntity> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find customers by status
     */
    List<CustomerJpaEntity> findByStatus(CustomerStatus status);
    
    /**
     * Find customers by registration date range
     */
    List<CustomerJpaEntity> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find customers by city (through addresses)
     */
    @Query("SELECT DISTINCT c FROM CustomerJpaEntity c JOIN c.addresses a WHERE a.city = :city")
    List<CustomerJpaEntity> findByAddressesCity(@Param("city") String city);
    
    /**
     * Find customers by name containing search term (case insensitive)
     */
    List<CustomerJpaEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone number already exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Count customers by status
     */
    long countByStatus(CustomerStatus status);
    
    /**
     * Find customers who haven't logged in since the specified date or never logged in
     */
    List<CustomerJpaEntity> findByLastLoginDateBeforeOrLastLoginDateIsNull(LocalDateTime lastLoginBefore);
    
    /**
     * Find customers with active status and primary address in specific city
     */
    @Query("SELECT DISTINCT c FROM CustomerJpaEntity c JOIN c.addresses a " +
           "WHERE c.status = 'ACTIVE' AND a.isPrimary = true AND a.city = :city")
    List<CustomerJpaEntity> findActiveCustomersInCity(@Param("city") String city);
    
    /**
     * Find customers registered in the last N days
     */
    @Query("SELECT c FROM CustomerJpaEntity c WHERE c.registrationDate >= :sinceDate")
    List<CustomerJpaEntity> findRecentlyRegisteredCustomers(@Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Find customers with multiple addresses
     */
    @Query("SELECT c FROM CustomerJpaEntity c WHERE SIZE(c.addresses) > 1")
    List<CustomerJpaEntity> findCustomersWithMultipleAddresses();
    
    /**
     * Find customers without any addresses
     */
    @Query("SELECT c FROM CustomerJpaEntity c WHERE SIZE(c.addresses) = 0")
    List<CustomerJpaEntity> findCustomersWithoutAddresses();
}