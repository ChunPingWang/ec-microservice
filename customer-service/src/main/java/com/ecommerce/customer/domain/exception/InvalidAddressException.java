package com.ecommerce.customer.domain.exception;

import com.ecommerce.common.exception.ValidationException;

/**
 * Exception thrown when an address is invalid
 */
public class InvalidAddressException extends ValidationException {
    
    public InvalidAddressException(String message) {
        super("Invalid address: " + message);
    }
    
    public InvalidAddressException(String field, String value) {
        super("Invalid address " + field + ": " + value);
    }
    
    public static InvalidAddressException forTaipeiDistrict(String district) {
        return new InvalidAddressException("Invalid Taipei district: " + district);
    }
    
    public static InvalidAddressException forPostalCode(String postalCode, String city) {
        return new InvalidAddressException("Invalid postal code " + postalCode + " for city " + city);
    }
}