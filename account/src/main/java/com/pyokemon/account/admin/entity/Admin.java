package com.pyokemon.account.admin.entity;

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
public class Admin {

  private Long adminId;
  private Long accountId;
  private String name;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
