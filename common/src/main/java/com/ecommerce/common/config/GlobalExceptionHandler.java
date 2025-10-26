package com.ecommerce.common.config;

import com.ecommerce.common.dto.ErrorResponse;
import com.ecommerce.common.exception.BaseException;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * 全域異常處理器，遵循 SRP 原則
 * 統一處理所有微服務的異常回應
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getErrorMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false))
            .build();
            
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, WebRequest request) {
        
        log.warn("Validation error: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getErrorMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false))
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, WebRequest request) {
        
        log.warn("Business error: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getErrorMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false))
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBase(
            BaseException ex, WebRequest request) {
        
        log.error("Base exception: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getErrorMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("系統內部錯誤")
            .details(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}