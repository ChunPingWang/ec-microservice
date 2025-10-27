package com.ecommerce.payment.application.config;

import com.ecommerce.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.payment.application.port.out.PaymentNotificationPort;
import com.ecommerce.payment.application.port.out.PaymentPersistencePort;
import com.ecommerce.payment.application.service.PaymentNotificationService;
import com.ecommerce.payment.application.service.PaymentRetryService;
import com.ecommerce.payment.application.strategy.BankTransferPaymentStrategy;
import com.ecommerce.payment.application.strategy.CreditCardPaymentStrategy;
import com.ecommerce.payment.application.strategy.PaymentStrategy;
import com.ecommerce.payment.application.strategy.PaymentStrategyFactory;
import com.ecommerce.payment.application.usecase.PaymentProcessingService;
import com.ecommerce.payment.domain.service.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

/**
 * 付款應用層配置
 * 配置應用層的服務和策略
 */
@Configuration
@EnableAsync
public class PaymentApplicationConfig {
    
    /**
     * 配置付款策略工廠
     */
    @Bean
    public PaymentStrategyFactory paymentStrategyFactory(List<PaymentStrategy> strategies) {
        return new PaymentStrategyFactory(strategies);
    }
    
    /**
     * 配置信用卡付款策略
     */
    @Bean
    public CreditCardPaymentStrategy creditCardPaymentStrategy(PaymentGatewayPort paymentGatewayPort) {
        return new CreditCardPaymentStrategy(paymentGatewayPort);
    }
    
    /**
     * 配置銀行轉帳付款策略
     */
    @Bean
    public BankTransferPaymentStrategy bankTransferPaymentStrategy(PaymentGatewayPort paymentGatewayPort) {
        return new BankTransferPaymentStrategy(paymentGatewayPort);
    }
    
    /**
     * 配置付款重試服務
     */
    @Bean
    public PaymentRetryService paymentRetryService(PaymentStrategyFactory strategyFactory,
                                                 PaymentDomainService paymentDomainService) {
        return new PaymentRetryService(strategyFactory, paymentDomainService);
    }
    
    /**
     * 配置付款通知服務
     */
    @Bean
    public PaymentNotificationService paymentNotificationService(PaymentNotificationPort paymentNotificationPort) {
        return new PaymentNotificationService(paymentNotificationPort);
    }
    
    /**
     * 配置付款處理服務
     */
    @Bean
    public PaymentProcessingService paymentProcessingService(
            PaymentPersistencePort paymentPersistencePort,
            PaymentNotificationPort paymentNotificationPort,
            PaymentDomainService paymentDomainService,
            PaymentStrategyFactory strategyFactory,
            PaymentRetryService retryService) {
        
        return new PaymentProcessingService(
            paymentPersistencePort,
            paymentNotificationPort,
            paymentDomainService,
            strategyFactory,
            retryService
        );
    }
}