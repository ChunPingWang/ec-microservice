package com.ecommerce.payment.domain.model;

import com.ecommerce.common.exception.ValidationException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 信用卡值物件
 * 包含信用卡資訊和驗證邏輯
 */
public class CreditCard {
    
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");
    
    private final String cardNumber;
    private final String cardHolderName;
    private final YearMonth expiryDate;
    private final String cvv;
    private final CardType cardType;
    
    private CreditCard(String cardNumber, String cardHolderName, YearMonth expiryDate, String cvv) {
        this.cardNumber = maskCardNumber(validateAndCleanCardNumber(cardNumber));
        this.cardHolderName = validateCardHolderName(cardHolderName);
        this.expiryDate = validateExpiryDate(expiryDate);
        this.cvv = validateCvv(cvv);
        this.cardType = determineCardType(cardNumber);
    }
    
    /**
     * 建立信用卡實例的工廠方法
     */
    public static CreditCard create(String cardNumber, String cardHolderName, 
                                  YearMonth expiryDate, String cvv) {
        return new CreditCard(cardNumber, cardHolderName, expiryDate, cvv);
    }
    
    /**
     * 建立信用卡實例的工廠方法（使用年月字串）
     */
    public static CreditCard create(String cardNumber, String cardHolderName, 
                                  int year, int month, String cvv) {
        return new CreditCard(cardNumber, cardHolderName, YearMonth.of(year, month), cvv);
    }
    
    /**
     * 檢查信用卡是否已過期
     */
    public boolean isExpired() {
        return YearMonth.now().isAfter(expiryDate);
    }
    
    /**
     * 檢查信用卡是否即將過期（3個月內）
     */
    public boolean isExpiringWithinMonths(int months) {
        return YearMonth.now().plusMonths(months).isAfter(expiryDate);
    }
    
    /**
     * 取得遮罩後的卡號（只顯示後四碼）
     */
    public String getMaskedCardNumber() {
        return cardNumber;
    }
    
    /**
     * 取得卡號後四碼
     */
    public String getLastFourDigits() {
        return cardNumber.substring(cardNumber.length() - 4);
    }
    
    // 驗證方法
    private String validateAndCleanCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new ValidationException("Card number is required");
        }
        
        // 移除空格和破折號
        String cleanedNumber = cardNumber.replaceAll("[\\s-]", "");
        
        if (!CARD_NUMBER_PATTERN.matcher(cleanedNumber).matches()) {
            throw new ValidationException("Invalid card number format");
        }
        
        if (!isValidLuhn(cleanedNumber)) {
            throw new ValidationException("Invalid card number");
        }
        
        return cleanedNumber;
    }
    
    private String validateCardHolderName(String cardHolderName) {
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new ValidationException("Card holder name is required");
        }
        
        String trimmedName = cardHolderName.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 50) {
            throw new ValidationException("Card holder name must be between 2 and 50 characters");
        }
        
        return trimmedName;
    }
    
    private YearMonth validateExpiryDate(YearMonth expiryDate) {
        if (expiryDate == null) {
            throw new ValidationException("Expiry date is required");
        }
        
        if (expiryDate.isBefore(YearMonth.now())) {
            throw new ValidationException("Card has expired");
        }
        
        // 檢查是否超過合理的未來日期（10年）
        if (expiryDate.isAfter(YearMonth.now().plusYears(10))) {
            throw new ValidationException("Invalid expiry date");
        }
        
        return expiryDate;
    }
    
    private String validateCvv(String cvv) {
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new ValidationException("CVV is required");
        }
        
        String trimmedCvv = cvv.trim();
        if (!CVV_PATTERN.matcher(trimmedCvv).matches()) {
            throw new ValidationException("Invalid CVV format");
        }
        
        return trimmedCvv;
    }
    
    /**
     * Luhn 演算法驗證卡號
     */
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }
    
    /**
     * 遮罩卡號，只保留後四碼
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return "*".repeat(cardNumber.length());
        }
        
        String masked = "*".repeat(cardNumber.length() - 4);
        return masked + cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * 根據卡號判斷卡片類型
     */
    private CardType determineCardType(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return CardType.VISA;
        } else if (cardNumber.startsWith("5") || cardNumber.startsWith("2")) {
            return CardType.MASTERCARD;
        } else if (cardNumber.startsWith("3")) {
            return CardType.AMERICAN_EXPRESS;
        } else {
            return CardType.OTHER;
        }
    }
    
    // Getters
    public String getCardNumber() { return cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public YearMonth getExpiryDate() { return expiryDate; }
    public String getCvv() { return cvv; }
    public CardType getCardType() { return cardType; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditCard that = (CreditCard) o;
        return Objects.equals(cardNumber, that.cardNumber) &&
               Objects.equals(cardHolderName, that.cardHolderName) &&
               Objects.equals(expiryDate, that.expiryDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, cardHolderName, expiryDate);
    }
    
    @Override
    public String toString() {
        return "CreditCard{" +
                "cardNumber='" + cardNumber + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", expiryDate=" + expiryDate +
                ", cardType=" + cardType +
                '}';
    }
    
    /**
     * 信用卡類型枚舉
     */
    public enum CardType {
        VISA("Visa"),
        MASTERCARD("MasterCard"),
        AMERICAN_EXPRESS("American Express"),
        OTHER("Other");
        
        private final String displayName;
        
        CardType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}