package com.pyokemon.account.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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
public class RegisterDeviceRequestDto {

  @NotBlank(message = "디바이스 ID는 필수입니다.")
  @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "디바이스 ID는 영문자, 숫자, 하이픈(-) 또는 언더바(_)만 포함할 수 있습니다.")
  private String deviceNumber;

  @NotBlank(message = "FCM 토큰은 필수입니다.")
  @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "FCM 토큰은 영문자, 숫자, 하이픈(-) 또는 언더바(_)만 포함할 수 있습니다.")
  private String fcmToken;

  @NotBlank(message = "운영체제 타입은 필수입니다.")
  @Pattern(regexp = "^(ANDROID|IOS)$", message = "운영체제 타입은 ANDROID 또는 IOS 중 하나여야 합니다.")
  private String osType; // ANDROID, IOS
}
