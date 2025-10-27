package com.ecommerce.common.architecture;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 外部適配器註解
 * 標識與外部系統整合的適配器類別
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ExternalAdapter {
    
    /**
     * 指定 bean 的名稱
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}