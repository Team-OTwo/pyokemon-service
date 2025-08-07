package com.pyokemon.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"})
@MapperScan("com.pyokemon.booking.repository")
@EnableScheduling
public class BookingApplication {
  public static void main(String[] args) {

    SpringApplication.run(BookingApplication.class, args);
  }
}
