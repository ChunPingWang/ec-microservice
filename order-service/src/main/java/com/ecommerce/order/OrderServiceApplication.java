package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Order Service 主應用程式
 */
@SpringBootApplication(scanBasePackages = {
    "com.ecommerce.order",
    "com.ecommerce.common"
})
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class OrderServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}