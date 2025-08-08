package com.pyokemon.account.auth.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppLoginResponseDto {

  private String accessToken;
  private String refreshToken;
  private String role;
  private Long accountId;
  private String deviceStatus;
}
