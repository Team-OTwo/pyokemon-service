package com.pyokemon.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"})
@MapperScan("com.pyokemon.booking.repository")
public class BookingApplication {
  public static void main(String[] args) {

    SpringApplication.run(BookingApplication.class, args);
  }
}
