package com.ecommerce.logistics.application.port.out;

import com.ecommerce.logistics.domain.event.*;

/**
 * 配送事件發布輸出埠
 * 遵循 DIP 原則 - 定義應用層需要的事件發布抽象
 * 遵循 ISP 原則 - 只定義配送事件發布相關的操作
 */
public interface DeliveryEventPort {
    
    /**
     * 發布配送請求建立事件
     */
    void publishDeliveryCreatedEvent(DeliveryCreatedEvent event);
    
    /**
     * 發布配送狀態更新事件
     */
    void publishDeliveryStatusUpdatedEvent(DeliveryStatusUpdatedEvent event);
    
    /**
     * 發布配送完成事件
     */
    void publishDeliveryCompletedEvent(DeliveryCompletedEvent event);
    
    /**
     * 發布配送失敗事件
     */
    void publishDeliveryFailedEvent(DeliveryFailedEvent event);
}