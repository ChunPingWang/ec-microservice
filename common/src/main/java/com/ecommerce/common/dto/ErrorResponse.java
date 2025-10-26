package com.ecommerce.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 統一錯誤回應 DTO，遵循 SRP 原則
 * 用於所有微服務的錯誤回應格式
 */
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private String path;
}