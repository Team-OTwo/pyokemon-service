package com.pyokemon.tenant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"com.pyokemon"})
@MapperScan("com.pyokemon.tenant.api.repository")
@EnableAspectJAutoProxy
public class TenantApplication {

  public static void main(String[] args) {
    SpringApplication.run(TenantApplication.class, args);
  }
}
