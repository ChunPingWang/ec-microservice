package com.ecommerce.customer.domain.event;

import com.ecommerce.common.architecture.DomainEvent;

import java.time.LocalDateTime;

/**
 * Domain event fired when a new customer is registered
 */
public class CustomerRegisteredEvent implements DomainEvent {
    
    private final String customerId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final LocalDateTime registrationDate;
    private final LocalDateTime occurredOn;
    
    public CustomerRegisteredEvent(String customerId, String email, String firstName, 
                                 String lastName, LocalDateTime registrationDate) {
        this.customerId = customerId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registrationDate = registrationDate;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    
    @Override
    public String toString() {
        return "CustomerRegisteredEvent{" +
                "customerId='" + customerId + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", registrationDate=" + registrationDate +
                ", occurredOn=" + occurredOn +
                '}';
    }
}