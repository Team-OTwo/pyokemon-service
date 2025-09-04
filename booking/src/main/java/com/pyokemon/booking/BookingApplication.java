package com.pyokemon.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"})
@MapperScan({"com.pyokemon.booking.repository", "com.pyokemon.booking.bff.repository"})
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@EnableKafka
public class BookingApplication {
  public static void main(String[] args) {

    SpringApplication.run(BookingApplication.class, args);
  }
}
