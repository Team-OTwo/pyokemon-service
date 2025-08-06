package com.pyokemon.did.remote.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${acapy.tenant.api-key}")
    private String tenantApiKey;

    @Value("${acapy.mediator.api-key}")
    private String mediatorApiKey;

    @Bean
    public RequestInterceptor apiKeyRequestInterceptor() {
        return template -> {
            // URL에 따라 다른 API 키 사용
            String url = template.url();
            if (url.contains("8002")) { // tenant ACA-Py
                template.header("x-api-key", tenantApiKey);
            } else if (url.contains("8001")) { // mediator ACA-Py
                template.header("x-api-key", mediatorApiKey);
            }
            template.header("Content-Type", "application/json");
        };
    }
} 