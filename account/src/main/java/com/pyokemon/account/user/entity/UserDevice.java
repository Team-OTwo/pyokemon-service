package com.pyokemon.account.user.entity;

import java.time.LocalDateTime;

import com.pyokemon.common.entity.BaseEntity;

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
public class UserDevice extends BaseEntity {

  private Long userId;
  private String deviceNumber;
  private String fcmToken;
  private String osType; // ANDROID, IOS
  private Boolean isLogin;
  private Boolean isValid;

}
