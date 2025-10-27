package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.payment.domain.model.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * 付款請求 DTO
 */
public class PaymentRequest extends BaseDto {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @DecimalMax(value = "100000.00", message = "Amount cannot exceed maximum limit")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String description;
    
    // Credit card specific fields
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number format")
    private String cardNumber;
    
    @Size(min = 2, max = 50, message = "Card holder name must be between 2 and 50 characters")
    private String cardHolderName;
    
    private YearMonth expiryDate;
    
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV format")
    private String cvv;
    
    // Bank transfer specific fields
    private String bankAccount;
    private String bankCode;
    
    // Digital wallet specific fields
    private String walletId;
    private String walletProvider;
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(String orderId, String customerId, BigDecimal amount, 
                         PaymentMethod paymentMethod, String description) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }
    
    // Factory methods for different payment types
    public static PaymentRequest createCreditCardPayment(String orderId, String customerId, 
                                                       BigDecimal amount, String description,
                                                       String cardNumber, String cardHolderName,
                                                       YearMonth expiryDate, String cvv) {
        PaymentRequest request = new PaymentRequest(orderId, customerId, amount, 
                                                  PaymentMethod.CREDIT_CARD, description);
        request.setCardNumber(cardNumber);
        request.setCardHolderName(cardHolderName);
        request.setExpiryDate(expiryDate);
        request.setCvv(cvv);
        return request;
    }
    
    public static PaymentRequest createBankTransferPayment(String orderId, String customerId,
                                                         BigDecimal amount, String description,
                                                         String bankAccount, String bankCode) {
        PaymentRequest request = new PaymentRequest(orderId, customerId, amount,
                                                  PaymentMethod.BANK_TRANSFER, description);
        request.setBankAccount(bankAccount);
        request.setBankCode(bankCode);
        return request;
    }
    
    public static PaymentRequest createDigitalWalletPayment(String orderId, String customerId,
                                                          BigDecimal amount, String description,
                                                          String walletId, String walletProvider) {
        PaymentRequest request = new PaymentRequest(orderId, customerId, amount,
                                                  PaymentMethod.DIGITAL_WALLET, description);
        request.setWalletId(walletId);
        request.setWalletProvider(walletProvider);
        return request;
    }
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    
    public YearMonth getExpiryDate() { return expiryDate; }
    public void setExpiryDate(YearMonth expiryDate) { this.expiryDate = expiryDate; }
    
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    
    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }
    
    public String getWalletProvider() { return walletProvider; }
    public void setWalletProvider(String walletProvider) { this.walletProvider = walletProvider; }
}