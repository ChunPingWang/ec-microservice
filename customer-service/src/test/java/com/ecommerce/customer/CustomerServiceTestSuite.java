package com.ecommerce.customer;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for Customer Service
 * Runs all unit and integration tests
 */
@Suite
@SuiteDisplayName("Customer Service Test Suite")
@SelectPackages({
    "com.ecommerce.customer.domain",
    "com.ecommerce.customer.application", 
    "com.ecommerce.customer.infrastructure"
})
public class CustomerServiceTestSuite {
    // Test suite configuration
}