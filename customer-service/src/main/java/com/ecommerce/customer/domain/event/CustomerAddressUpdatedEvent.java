package com.ecommerce.customer.domain.event;

import com.ecommerce.common.architecture.DomainEvent;

import java.time.LocalDateTime;

/**
 * Domain event fired when a customer's address is updated
 */
public class CustomerAddressUpdatedEvent implements DomainEvent {
    
    private final String customerId;
    private final String addressId;
    private final String formattedAddress;
    private final boolean isPrimary;
    private final LocalDateTime occurredOn;
    
    public CustomerAddressUpdatedEvent(String customerId, String addressId, 
                                     String formattedAddress, boolean isPrimary) {
        this.customerId = customerId;
        this.addressId = addressId;
        this.formattedAddress = formattedAddress;
        this.isPrimary = isPrimary;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public String getAddressId() {
        return addressId;
    }
    
    public String getFormattedAddress() {
        return formattedAddress;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    @Override
    public String toString() {
        return "CustomerAddressUpdatedEvent{" +
                "customerId='" + customerId + '\'' +
                ", addressId='" + addressId + '\'' +
                ", formattedAddress='" + formattedAddress + '\'' +
                ", isPrimary=" + isPrimary +
                ", occurredOn=" + occurredOn +
                '}';
    }
}