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
public class TenantEventListDto {
  private Long eventId;
  private Long eventScheduleId;
  private String thumbnailUrl;
  private String title;
  private LocalDateTime eventDate;
  private String venueName;
  private String status;
}
