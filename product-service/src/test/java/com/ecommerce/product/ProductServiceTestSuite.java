package com.ecommerce.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for Product Service
 * Runs all tests including unit tests, integration tests, and performance tests
 */
@Suite
@SuiteDisplayName("Product Service Test Suite")
@SelectPackages({
    "com.ecommerce.product.domain",
    "com.ecommerce.product.application",
    "com.ecommerce.product.infrastructure"
})
@DisplayName("Complete Product Service Test Suite")
public class ProductServiceTestSuite {
    // This class serves as a test suite runner
    // All tests in the selected packages will be executed
}