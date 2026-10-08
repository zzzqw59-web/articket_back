package com.project.articket.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentWebClientConfig {

    @Bean
    public WebClient paymentWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .build();
    }
}