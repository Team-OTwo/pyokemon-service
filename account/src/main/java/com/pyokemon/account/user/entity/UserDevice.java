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
public class UserDevice {

  private Long userDeviceId;
  private Long userId;
  private String deviceNumber;
  private String fcmToken;
  private String osType; // ANDROID, IOS
  private Boolean isValid;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
