package com.ecommerce.product.application.port.out;

import com.ecommerce.product.application.dto.StockNotificationRequest;

import java.util.List;

/**
 * Output port for notification operations
 * Follows DIP principle by defining abstraction for notification services
 */
public interface NotificationPort {
    
    /**
     * Send stock availability notification
     * @param productId the product ID
     * @param productName the product name
     * @param availableQuantity the available quantity
     */
    void sendStockAvailableNotification(String productId, String productName, Integer availableQuantity);
    
    /**
     * Send out of stock notification
     * @param productId the product ID
     * @param productName the product name
     */
    void sendOutOfStockNotification(String productId, String productName);
    
    /**
     * Send low stock alert to administrators
     * @param productId the product ID
     * @param productName the product name
     * @param currentQuantity the current quantity
     * @param minimumThreshold the minimum threshold
     */
    void sendLowStockAlert(String productId, String productName, Integer currentQuantity, Integer minimumThreshold);
    
    /**
     * Send restock notification to customers who requested it
     * @param productId the product ID
     * @param productName the product name
     * @param availableQuantity the newly available quantity
     */
    void sendRestockNotification(String productId, String productName, Integer availableQuantity);
    
    /**
     * Register customer for stock notification
     * @param productId the product ID
     * @param customerId the customer ID
     * @param email the customer email
     */
    void registerStockNotification(String productId, String customerId, String email);
    
    /**
     * Unregister customer from stock notification
     * @param productId the product ID
     * @param customerId the customer ID
     */
    void unregisterStockNotification(String productId, String customerId);
    
    /**
     * Send bulk stock notifications
     * @param notifications list of notification requests
     */
    void sendBulkStockNotifications(List<StockNotificationRequest> notifications);
    
    /**
     * Send price change notification
     * @param productId the product ID
     * @param productName the product name
     * @param oldPrice the old price
     * @param newPrice the new price
     */
    void sendPriceChangeNotification(String productId, String productName, String oldPrice, String newPrice);
}