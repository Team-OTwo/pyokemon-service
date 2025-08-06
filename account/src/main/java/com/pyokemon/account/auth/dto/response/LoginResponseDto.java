package com.pyokemon.account.auth.dto.response;

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
public class LoginResponseDto {

  private String accessToken;
  private String refreshToken;
  private String role;
  private Long accountId;
  private Boolean isVerified;
}
