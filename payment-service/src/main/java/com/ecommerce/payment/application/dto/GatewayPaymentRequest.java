package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.payment.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * 閘道付款請求 DTO
 */
public class GatewayPaymentRequest extends BaseDto {
    
    private String transactionId;
    private String merchantReference;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String description;
    
    // Credit card fields
    private String cardNumber;
    private String cardHolderName;
    private YearMonth expiryDate;
    private String cvv;
    
    // Bank transfer fields
    private String bankAccount;
    private String bankCode;
    
    // Digital wallet fields
    private String walletId;
    private String walletProvider;
    
    // Customer information
    private String customerId;
    private String customerEmail;
    private String customerPhone;
    
    // Billing address
    private String billingAddress;
    private String billingCity;
    private String billingCountry;
    private String billingPostalCode;
    
    // Constructors
    public GatewayPaymentRequest() {
        this.currency = "TWD"; // Default to Taiwan Dollar
    }
    
    public GatewayPaymentRequest(String transactionId, String merchantReference, BigDecimal amount,
                               PaymentMethod paymentMethod, String description) {
        this();
        this.transactionId = transactionId;
        this.merchantReference = merchantReference;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }
    
    // Factory methods
    public static GatewayPaymentRequest createCreditCardRequest(String transactionId, String merchantReference,
                                                              BigDecimal amount, String description,
                                                              String cardNumber, String cardHolderName,
                                                              YearMonth expiryDate, String cvv) {
        GatewayPaymentRequest request = new GatewayPaymentRequest(transactionId, merchantReference,
                                                                amount, PaymentMethod.CREDIT_CARD, description);
        request.setCardNumber(cardNumber);
        request.setCardHolderName(cardHolderName);
        request.setExpiryDate(expiryDate);
        request.setCvv(cvv);
        return request;
    }
    
    public static GatewayPaymentRequest createBankTransferRequest(String transactionId, String merchantReference,
                                                                BigDecimal amount, String description,
                                                                String bankAccount, String bankCode) {
        GatewayPaymentRequest request = new GatewayPaymentRequest(transactionId, merchantReference,
                                                                amount, PaymentMethod.BANK_TRANSFER, description);
        request.setBankAccount(bankAccount);
        request.setBankCode(bankCode);
        return request;
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
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
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    
    public String getBillingCity() { return billingCity; }
    public void setBillingCity(String billingCity) { this.billingCity = billingCity; }
    
    public String getBillingCountry() { return billingCountry; }
    public void setBillingCountry(String billingCountry) { this.billingCountry = billingCountry; }
    
    public String getBillingPostalCode() { return billingPostalCode; }
    public void setBillingPostalCode(String billingPostalCode) { this.billingPostalCode = billingPostalCode; }
}