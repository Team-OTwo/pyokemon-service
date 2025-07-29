package com.pyokemon.account.user.dto.request;

import jakarta.validation.constraints.NotBlank;

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
public class UserDeviceRequestDto {

  @NotBlank(message = "디바이스 ID는 필수입니다.")
  private String deviceNumber;

  private String fcmToken;

  @NotBlank(message = "운영체제 타입은 필수입니다.")
  private String osType; // ANDROID, IOS
}
