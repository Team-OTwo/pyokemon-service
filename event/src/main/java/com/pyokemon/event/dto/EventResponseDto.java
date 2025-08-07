package com.pyokemon.event.dto;

import java.time.LocalDateTime;

import com.pyokemon.event.entity.Event.EventStatus;

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
public class EventResponseDto {
  private Long eventId;
  private Long accountId;
  private String title;
  private Long ageLimit;
  private String description;
  private String genre;
  private String thumbnailUrl;
  private EventStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
