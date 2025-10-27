package com.ecommerce.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.ecommerce.logistics",
    "com.ecommerce.common"
})
public class LogisticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogisticsServiceApplication.class, args);
    }
}