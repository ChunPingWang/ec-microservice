package com.ecommerce.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 訂單服務測試套件
 * 執行所有訂單相關的測試
 */
@Suite
@SuiteDisplayName("訂單服務完整測試套件")
@SelectPackages({
    "com.ecommerce.order.domain",
    "com.ecommerce.order.application", 
    "com.ecommerce.order.infrastructure"
})
@DisplayName("訂單服務測試套件")
public class OrderServiceTestSuite {
    // 測試套件類別，用於組織和執行所有測試
}