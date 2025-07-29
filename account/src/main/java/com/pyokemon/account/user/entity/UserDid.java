package com.pyokemon.account.user.entity;

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
public class UserDid {

  private Long userDidId;
  private Long userId;
  private String did;
  private Boolean isValid;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
