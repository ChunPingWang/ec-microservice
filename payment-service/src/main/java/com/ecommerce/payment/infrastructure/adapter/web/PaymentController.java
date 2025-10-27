package com.ecommerce.payment.infrastructure.adapter.web;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.payment.application.dto.PaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.RefundRequest;
import com.ecommerce.payment.application.dto.RefundResponse;
import com.ecommerce.payment.application.port.in.PaymentProcessingUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 付款控制器
 * 提供付款相關的 REST API 端點
 */
@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentProcessingUseCase paymentProcessingUseCase;
    
    public PaymentController(PaymentProcessingUseCase paymentProcessingUseCase) {
        this.paymentProcessingUseCase = paymentProcessingUseCase;
    }
    
    /**
     * 處理付款請求
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentProcessingUseCase.processPayment(request);
            
            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Payment processing failed", null, "SYSTEM_ERROR"));
            }
            
            if (response.isSuccessful()) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("付款處理成功", response));
            } else if (response.isPending() || response.isProcessing()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(ApiResponse.success("付款處理中", response));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(response.getFailureReason(), response, "PAYMENT_FAILED"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage(), null, "SYSTEM_ERROR"));
        }
    }
    
    /**
     * 查詢付款狀態
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(
            @PathVariable String transactionId) {
        
        PaymentResponse response = paymentProcessingUseCase.getPaymentStatus(transactionId);
        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }
    
    /**
     * 取消付款
     */
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String transactionId,
            @RequestParam(required = false, defaultValue = "客戶取消") String reason) {
        
        PaymentResponse response = paymentProcessingUseCase.cancelPayment(transactionId, reason);
        return ResponseEntity.ok(ApiResponse.success("付款已取消", response));
    }
    
    /**
     * 處理退款請求
     */
    @PostMapping("/refunds")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @Valid @RequestBody RefundRequest request) {
        try {
            RefundResponse response = paymentProcessingUseCase.processRefund(request);
            
            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Refund processing failed", null, "SYSTEM_ERROR"));
            }
            
            if (response.isSuccessful()) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("退款處理成功", response));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(response.getFailureReason(), response, "REFUND_FAILED"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage(), null, "SYSTEM_ERROR"));
        }
    }
    
    /**
     * 重試失敗的付款
     */
    @PostMapping("/{transactionId}/retry")
    public ResponseEntity<ApiResponse<PaymentResponse>> retryPayment(
            @PathVariable String transactionId) {
        
        PaymentResponse response = paymentProcessingUseCase.retryPayment(transactionId);
        
        if (response.isSuccessful()) {
            return ResponseEntity.ok(ApiResponse.success("付款重試成功", response));
        } else if (response.isPending() || response.isProcessing()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.success("付款重試處理中", response));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getFailureReason(), response, "PAYMENT_RETRY_FAILED"));
        }
    }
    
    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Payment service is healthy", "OK"));
    }
}