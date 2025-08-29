package com.pyokemon.event.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EventScheduleInfoDto {
  private Long eventScheduleId;
  private Long eventId;
  private Long venueId;
  private LocalDateTime eventDate;
}
