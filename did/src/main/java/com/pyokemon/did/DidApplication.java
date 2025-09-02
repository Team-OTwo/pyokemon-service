package com.pyokemon.did;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.retry.annotation.EnableRetry;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@EnableFeignClients
@SpringBootApplication
@EnableRetry
@EnableRedisRepositories
@OpenAPIDefinition(info = @Info(title = "Pyokemon DID Service", version = "1.0.0",
    description = "DID (Decentralized Identifier) 서비스 API"))
public class DidApplication {

  public static void main(String[] args) {
    SpringApplication.run(DidApplication.class, args);
  }
}
