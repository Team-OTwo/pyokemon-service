package com.pyokemon.event.entity;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedEvent {
  private Long savedEventId;
  private Long eventId;
  private Long accountId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
