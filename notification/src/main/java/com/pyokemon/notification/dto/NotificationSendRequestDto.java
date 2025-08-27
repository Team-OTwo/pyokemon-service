package com.pyokemon.notification.dto;

import java.util.List;

import lombok.*;

@Data
public class NotificationSendRequestDto {
  private String token; // 단일 기기 토큰
  private String title; // 알림 제목
  private String message; // 알림 내용

}
