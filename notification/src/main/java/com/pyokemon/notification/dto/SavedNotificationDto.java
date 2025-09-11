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
public class SavedNotificationDto {
  private Long eventId;
  private Long accountId;
  private String title;
  private String message;
  private LocalDateTime ticketOpenAt;

}
