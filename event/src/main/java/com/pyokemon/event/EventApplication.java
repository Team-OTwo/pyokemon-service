package com.pyokemon.event;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"},exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@MapperScan("com.pyokemon.event.repository")
public class EventApplication {
  public static void main(String[] args) {
    SpringApplication.run(EventApplication.class, args);
  }
}
