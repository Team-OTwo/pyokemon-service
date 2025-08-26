package com.pyokemon.notification.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponseDtoApp {
  private List<NotificationResponseDto> notifications;
  private Long lastCursorId;
}
