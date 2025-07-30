package com.pyokemon.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.pyokemon.account.auth.repository", "com.pyokemon.account.tenant.repository",
    "com.pyokemon.account.user.repository", "com.pyokemon.account.admin.repository"})
public class AccountApplication {

  public static void main(String[] args) {
    SpringApplication.run(AccountApplication.class, args);
  }
}
