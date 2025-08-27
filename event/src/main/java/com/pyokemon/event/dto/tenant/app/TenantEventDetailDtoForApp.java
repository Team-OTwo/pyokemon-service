package com.pyokemon.event.dto.tenant.app;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantEventDetailDtoForApp {
  private Long eventId;
  private String title;
  private LocalDateTime eventDate;
  private String venueName;
  private String genre;
}
