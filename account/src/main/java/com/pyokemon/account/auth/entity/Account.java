package com.pyokemon.account.auth.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

  private Long accountId;
  private String role; // USER, TENANT, ADMIN
  private String loginId;
  private String password;
  private AccountStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
