package com.ecommerce.customer.application.port.out;

import com.ecommerce.customer.domain.event.CustomerAddressUpdatedEvent;
import com.ecommerce.customer.domain.event.CustomerRegisteredEvent;

/**
 * Output port for customer event publishing
 * Defines the contract for publishing customer domain events
 */
public interface CustomerEventPort {
    
    /**
     * Publish customer registered event
     * @param event the customer registered event
     */
    void publishCustomerRegistered(CustomerRegisteredEvent event);
    
    /**
     * Publish customer address updated event
     * @param event the customer address updated event
     */
    void publishCustomerAddressUpdated(CustomerAddressUpdatedEvent event);
}