package com.ecommerce.common.architecture;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 事件發布者註解
 * 標識負責發布領域事件的類別
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface EventPublisher {
    
    /**
     * 指定 bean 的名稱
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}