package com.ecommerce.common.validation;

import com.ecommerce.common.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 電子郵件驗證器，遵循 SRP 原則
 * 專門負責電子郵件格式的驗證
 */
@Component
public class EmailValidator implements Validator<String> {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    @Override
    public void validate(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("電子郵件", "不能為空");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("電子郵件", "格式不正確");
        }
    }
    
    @Override
    public boolean isValid(String email) {
        try {
            validate(email);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}