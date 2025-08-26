package com.pyokemon.notification.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
  private Long notificationId;
  private String title;
  private String message;
  private Boolean isChecked;
  private LocalDateTime createdAt;
}
