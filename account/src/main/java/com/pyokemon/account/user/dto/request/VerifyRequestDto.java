package com.pyokemon.account.user.dto.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class VerifyRequestDto {
  private Long accountId;
  private String name;
  private String phoneNumber;
  private LocalDate birth;
  private String deviceNumber;
  private String fcmToken;
  private String osType;
}
