package com.pyokemon.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"})
@MapperScan("com.pyokemon.payment.repository")
public class PaymentApplication {
  public static void main(String[] args) {

    SpringApplication.run(PaymentApplication.class, args);
  }
}
