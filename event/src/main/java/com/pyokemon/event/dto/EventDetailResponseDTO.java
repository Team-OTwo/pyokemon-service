package com.pyokemon.event.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventDetailResponseDTO {
  // event
  private Long eventId;
  private String title;
  private Long ageLimit;
  private String description;
  private String genre;
  private String thumbnailUrl;

  // event schedule
  private Long eventScheduleId;
  private LocalDateTime ticketOpenAt;
  private LocalDateTime eventDate;

  // venue
  private String venueName;
}
