package com.pyokemon.account.user.dto.response;

import java.time.LocalDate;
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
public class UserDetailDto {

  private Long userId;
  private Long accountId;
  private String loginId;
  private String name;
  private String phone;
  private LocalDate birth;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
