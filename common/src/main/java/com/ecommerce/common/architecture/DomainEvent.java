package com.ecommerce.common.architecture;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 領域事件基礎類別，遵循 OCP 原則
 * 支援事件驅動架構的擴展性
 */
@Data
public abstract class DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String eventType;
    
    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.eventType = eventType;
    }
}