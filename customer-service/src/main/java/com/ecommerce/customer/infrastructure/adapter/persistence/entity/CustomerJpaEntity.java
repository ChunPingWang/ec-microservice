package com.ecommerce.customer.infrastructure.adapter.persistence.entity;

import com.ecommerce.customer.domain.model.CustomerStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for Customer
 * Maps customer domain object to database table
 */
@Entity
@Table(name = "customers")
public class CustomerJpaEntity {
    
    @Id
    @Column(name = "customer_id", length = 50)
    private String customerId;
    
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status;
    
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;
    
    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AddressJpaEntity> addresses = new ArrayList<>();
    
    // Constructors
    public CustomerJpaEntity() {}
    
    public CustomerJpaEntity(String customerId, String firstName, String lastName, 
                           String email, String phoneNumber, CustomerStatus status,
                           LocalDateTime registrationDate) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.registrationDate = registrationDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for bidirectional relationship
    public void addAddress(AddressJpaEntity address) {
        addresses.add(address);
        address.setCustomer(this);
    }
    
    public void removeAddress(AddressJpaEntity address) {
        addresses.remove(address);
        address.setCustomer(null);
    }
    
    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public CustomerStatus getStatus() {
        return status;
    }
    
    public void setStatus(CustomerStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }
    
    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<AddressJpaEntity> getAddresses() {
        return addresses;
    }
    
    public void setAddresses(List<AddressJpaEntity> addresses) {
        this.addresses = addresses;
    }
}