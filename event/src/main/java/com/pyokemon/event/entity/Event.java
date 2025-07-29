package com.pyokemon.event.entity;

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
public class Event {

  private Long eventId;
  private Long tenantId;
  private String title;
  private Long ageLimit;
  private String description;
  private String genre;
  private String thumbnailUrl;
  private EventStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public enum EventStatus {
    APPROVED, REJECTED, PENDING
  }
}
