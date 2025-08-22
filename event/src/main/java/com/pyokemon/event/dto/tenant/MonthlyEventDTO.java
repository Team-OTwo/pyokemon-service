package com.pyokemon.event.dto.tenant;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyEventDTO {
  private String title;
  private String venueName;
  private LocalDateTime eventDate;
  private Integer ticketCount;
}
