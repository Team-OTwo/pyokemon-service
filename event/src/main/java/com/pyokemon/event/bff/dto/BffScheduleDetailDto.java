package com.pyokemon.event.bff.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class BffScheduleDetailDto {
  private Long eventScheduleId;
  private String title;
  private String venueName;
  private LocalDateTime eventDate;
}
