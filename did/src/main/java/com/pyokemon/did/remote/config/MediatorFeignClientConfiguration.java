package com.pyokemon.did.remote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class MediatorFeignClientConfiguration {

  @Value("${acapy.mediator.api-key}")
  private String apiKey;

  @Bean
  public RequestInterceptor requestInterceptor() {
    return template -> template.header("X-API-Key", apiKey);
  }
}
