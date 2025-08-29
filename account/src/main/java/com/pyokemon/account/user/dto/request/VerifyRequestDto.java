package com.pyokemon.account.user.dto.request;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
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
