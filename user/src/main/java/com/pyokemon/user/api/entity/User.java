package com.pyokemon.user.api.entity;

import java.time.LocalDateTime;
import java.time.LocalDate;

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
public class User {

  private Long id;
  private String name;
  private String email;
  private String password;
  private String phone;
  private LocalDate birth;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
