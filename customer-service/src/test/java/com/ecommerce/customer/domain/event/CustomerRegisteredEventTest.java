package com.ecommerce.customer.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomerRegisteredEvent
 * Tests domain event structure and behavior
 */
@DisplayName("Customer Registered Event Tests")
class CustomerRegisteredEventTest {

    @Test
    @DisplayName("Should create customer registered event with all required fields")
    void shouldCreateCustomerRegisteredEventWithAllRequiredFields() {
        // Given
        String customerId = "CUST-123";
        String email = "rex.chen@example.com";
        String firstName = "Rex";
        String lastName = "Chen";
        LocalDateTime registrationDate = LocalDateTime.now();

        // When
        CustomerRegisteredEvent event = new CustomerRegisteredEvent(
            customerId, email, firstName, lastName, registrationDate
        );

        // Then
        assertNotNull(event);
        assertEquals(customerId, event.getCustomerId());
        assertEquals(email, event.getEmail());
        assertEquals(firstName, event.getFirstName());
        assertEquals(lastName, event.getLastName());
        assertEquals(registrationDate, event.getRegistrationDate());
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
    }

    @Test
    @DisplayName("Should generate unique event IDs for different events")
    void shouldGenerateUniqueEventIdsForDifferentEvents() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        CustomerRegisteredEvent event1 = new CustomerRegisteredEvent(
            "CUST-123", "rex1@example.com", "Rex", "Chen", now
        );
        
        CustomerRegisteredEvent event2 = new CustomerRegisteredEvent(
            "CUST-124", "rex2@example.com", "Rex", "Wang", now
        );

        // When & Then
        assertNotEquals(event1.getEventId(), event2.getEventId());
        assertNotNull(event1.getEventId());
        assertNotNull(event2.getEventId());
    }

    @Test
    @DisplayName("Should have occurred on timestamp close to creation time")
    void shouldHaveOccurredOnTimestampCloseToCreationTime() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now();
        
        // When
        CustomerRegisteredEvent event = new CustomerRegisteredEvent(
            "CUST-123", "rex@example.com", "Rex", "Chen", LocalDateTime.now()
        );
        
        LocalDateTime afterCreation = LocalDateTime.now();

        // Then
        assertTrue(event.getOccurredOn().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(event.getOccurredOn().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should have proper string representation")
    void shouldHaveProperStringRepresentation() {
        // Given
        CustomerRegisteredEvent event = new CustomerRegisteredEvent(
            "CUST-123", "rex@example.com", "Rex", "Chen", LocalDateTime.now()
        );

        // When
        String eventString = event.toString();

        // Then
        assertNotNull(eventString);
        assertTrue(eventString.contains("CUST-123"));
        assertTrue(eventString.contains("rex@example.com"));
        assertTrue(eventString.contains("Rex"));
        assertTrue(eventString.contains("Chen"));
    }
}