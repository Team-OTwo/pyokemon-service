package com.pyokemon.tenant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"})
@MapperScan("com.pyokemon.tenant.api.repository")
public class TenantApplication {

  public static void main(String[] args) {
    SpringApplication.run(TenantApplication.class, args);
  }
}
