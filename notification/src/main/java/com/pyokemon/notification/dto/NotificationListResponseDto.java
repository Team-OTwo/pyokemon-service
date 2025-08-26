package com.pyokemon.notification.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponseDto {
  private List<NotificationResponseDto> notifications;
  private Integer totalCount;
  private Boolean hasNext;
  private Long lastNotificationId;
}
