package com.ecommerce.product.infrastructure.adapter.external;

import com.ecommerce.common.architecture.Adapter;
import com.ecommerce.product.application.dto.StockNotificationRequest;
import com.ecommerce.product.application.port.out.NotificationPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Notification Adapter
 * Implements NotificationPort for sending notifications
 * Follows DIP principle by implementing the output port interface
 */
@Adapter
@Component
public class NotificationAdapter implements NotificationPort {
    
    // In a real implementation, you would inject actual notification services
    // such as email service, SMS service, push notification service, etc.
    
    @Override
    public void sendStockAvailableNotification(String productId, String productName, Integer availableQuantity) {
        // Simulate sending stock available notification
        String message = String.format("Good news! %s is now available with %d units in stock.", 
                                     productName, availableQuantity);
        
        logNotification("STOCK_AVAILABLE", productId, productName, message);
        
        // In a real implementation, you would:
        // 1. Query customer notification preferences for this product
        // 2. Send email/SMS/push notifications to interested customers
        // 3. Handle notification delivery failures and retries
        
        simulateNotificationDelivery(message);
    }
    
    @Override
    public void sendOutOfStockNotification(String productId, String productName) {
        String message = String.format("Alert: %s is now out of stock.", productName);
        
        logNotification("OUT_OF_STOCK", productId, productName, message);
        
        // In a real implementation, you would:
        // 1. Notify administrators about out of stock situation
        // 2. Update product status on website
        // 3. Trigger reorder processes if configured
        
        simulateNotificationDelivery(message);
    }
    
    @Override
    public void sendLowStockAlert(String productId, String productName, Integer currentQuantity, Integer minimumThreshold) {
        String message = String.format("Low stock alert: %s has only %d units left (threshold: %d).", 
                                     productName, currentQuantity, minimumThreshold);
        
        logNotification("LOW_STOCK_ALERT", productId, productName, message);
        
        // In a real implementation, you would:
        // 1. Send alerts to inventory managers
        // 2. Trigger automatic reorder if configured
        // 3. Update dashboard alerts
        
        simulateNotificationDelivery(message);
    }
    
    @Override
    public void sendRestockNotification(String productId, String productName, Integer availableQuantity) {
        String message = String.format("%s has been restocked! %d units are now available.", 
                                     productName, availableQuantity);
        
        logNotification("RESTOCK_NOTIFICATION", productId, productName, message);
        
        // In a real implementation, you would:
        // 1. Find all customers who requested restock notifications for this product
        // 2. Send personalized notifications to each customer
        // 3. Remove customers from notification list after sending
        // 4. Track notification delivery success/failure
        
        simulateNotificationDelivery(message);
    }
    
    @Override
    public void registerStockNotification(String productId, String customerId, String email) {
        String message = String.format("Registered stock notification for customer %s (email: %s) for product %s", 
                                     customerId, email, productId);
        
        logNotification("REGISTER_NOTIFICATION", productId, customerId, message);
        
        // In a real implementation, you would:
        // 1. Store the notification request in a database
        // 2. Validate email address
        // 3. Send confirmation email to customer
        // 4. Handle duplicate registrations
        
        simulateNotificationDelivery("Registration confirmation sent to " + email);
    }
    
    @Override
    public void unregisterStockNotification(String productId, String customerId) {
        String message = String.format("Unregistered stock notification for customer %s for product %s", 
                                     customerId, productId);
        
        logNotification("UNREGISTER_NOTIFICATION", productId, customerId, message);
        
        // In a real implementation, you would:
        // 1. Remove the notification request from database
        // 2. Send confirmation to customer
        
        simulateNotificationDelivery(message);
    }
    
    @Override
    public void sendBulkStockNotifications(List<StockNotificationRequest> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        
        logNotification("BULK_NOTIFICATION", "MULTIPLE", "BULK", 
                       "Processing " + notifications.size() + " bulk notifications");
        
        // Process notifications asynchronously for better performance
        CompletableFuture.runAsync(() -> {
            for (StockNotificationRequest notification : notifications) {
                try {
                    processStockNotification(notification);
                } catch (Exception e) {
                    System.err.println("Failed to process notification: " + notification + ", error: " + e.getMessage());
                }
            }
        });
    }
    
    @Override
    public void sendPriceChangeNotification(String productId, String productName, String oldPrice, String newPrice) {
        String message = String.format("Price update: %s price changed from %s to %s", 
                                     productName, oldPrice, newPrice);
        
        logNotification("PRICE_CHANGE", productId, productName, message);
        
        // In a real implementation, you would:
        // 1. Find customers who have this product in wishlist/cart
        // 2. Send price change notifications
        // 3. Handle price increase vs decrease differently
        
        simulateNotificationDelivery(message);
    }
    
    // Private helper methods
    
    private void processStockNotification(StockNotificationRequest notification) {
        switch (notification.getNotificationType()) {
            case STOCK_AVAILABLE -> sendStockAvailableNotification(
                notification.getProductId(), 
                notification.getProductName(), 
                notification.getQuantity()
            );
            case OUT_OF_STOCK -> sendOutOfStockNotification(
                notification.getProductId(), 
                notification.getProductName()
            );
            case LOW_STOCK_ALERT -> sendLowStockAlert(
                notification.getProductId(), 
                notification.getProductName(), 
                notification.getQuantity(), 
                0 // Would need minimum threshold in request
            );
            case RESTOCK_NOTIFICATION -> sendRestockNotification(
                notification.getProductId(), 
                notification.getProductName(), 
                notification.getQuantity()
            );
            case PRICE_CHANGE -> sendPriceChangeNotification(
                notification.getProductId(), 
                notification.getProductName(), 
                "N/A", // Would need old price in request
                "N/A"  // Would need new price in request
            );
            default -> System.err.println("Unknown notification type: " + notification.getNotificationType());
        }
    }
    
    private void logNotification(String type, String productId, String target, String message) {
        // In a real implementation, use proper logging framework
        System.out.println(String.format("[NOTIFICATION] Type: %s, Product: %s, Target: %s, Message: %s", 
                                        type, productId, target, message));
    }
    
    private void simulateNotificationDelivery(String message) {
        // Simulate notification delivery delay
        try {
            Thread.sleep(10); // 10ms delay to simulate network call
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // In a real implementation, you would:
        // 1. Make HTTP calls to notification services
        // 2. Send emails via SMTP
        // 3. Send SMS via SMS gateway
        // 4. Send push notifications via FCM/APNS
        // 5. Handle delivery failures and implement retry logic
        
        System.out.println("[NOTIFICATION SENT] " + message);
    }
}