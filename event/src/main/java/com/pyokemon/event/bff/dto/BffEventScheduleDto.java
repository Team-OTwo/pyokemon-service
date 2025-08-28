package com.pyokemon.event.bff.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BffEventScheduleDto {
  private Long eventScheduleId;
  private Long eventId;
  private Long venueId;
  private LocalDateTime eventDate;
}
