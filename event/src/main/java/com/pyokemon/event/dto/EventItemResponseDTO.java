package com.pyokemon.event.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventItemResponseDTO {
  private Long eventScheduleId;
  private Long eventId;
  private Long venueId;
  private String genre;
  private Integer total;
  private String thumbnailUrl;
  private LocalDateTime ticketOpenAt;
  private LocalDateTime eventDate;

  private String title;
  private String venueName;
}
