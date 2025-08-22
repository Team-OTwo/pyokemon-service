package com.pyokemon.did.remote.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Collection;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor apiKeyRequestInterceptor() {
        return template -> {
            // Content-Type 헤더 설정
            template.header("Content-Type", "application/json");
            
            // Authorization 헤더를 Bearer 토큰 형태로 설정
            Collection<String> authorizationHeaders = template.headers().get("Authorization");
            if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                String authorization = authorizationHeaders.iterator().next();
                // Authorization 헤더가 있으면 Bearer 접두사 추가
                if (!authorization.startsWith("Bearer ")) {
                    // 기존 Authorization 헤더 제거
                    template.removeHeader("Authorization");
                    // 새로운 Bearer 토큰 헤더 추가
                    template.header("Authorization", "Bearer " + authorization);
                }
            }
        };
    }
} 