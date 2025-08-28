package com.pyokemon.did;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.retry.annotation.EnableRetry;

@EnableFeignClients
@SpringBootApplication
@EnableRetry
@EnableRedisRepositories
public class DidApplication {

  public static void main(String[] args) {
    SpringApplication.run(DidApplication.class, args);
  }
}
