package com.pyokemon.event;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"})
@MapperScan("com.pyokemon.event.repository")
@EnableScheduling
@EnableKafka
@org.springframework.context.annotation.ComponentScan(
    basePackages = {"com.pyokemon.event", "com.pyokemon.common"})
public class EventApplication {
  public static void main(String[] args) {
    SpringApplication.run(EventApplication.class, args);
  }
}
