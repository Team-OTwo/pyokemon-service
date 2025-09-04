package com.pyokemon.event;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"})
@MapperScan({"com.pyokemon.event.repository", "com.pyokemon.event.bff.repository"})
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@EnableKafka
@EnableFeignClients
public class EventApplication {
  public static void main(String[] args) {
    SpringApplication.run(EventApplication.class, args);
  }
}
