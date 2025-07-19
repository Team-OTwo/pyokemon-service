package com.pyokemon.user.entity;

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
public class User {

  private Long id;
  private String username;
  private String email;
  private String password;
  private String nickname;
  private UserStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public enum UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
  }
}
