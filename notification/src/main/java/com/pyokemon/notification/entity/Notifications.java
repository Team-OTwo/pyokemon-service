package com.pyokemon.notification.entity;

import java.time.LocalDateTime;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {

  private Long notificationId;
  private Long accountId;
  private String title;
  private String message;
  private Boolean isChecked;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

}
