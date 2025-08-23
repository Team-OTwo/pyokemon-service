package com.pyokemon.event.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantEventListResponseDtoForApp {
  private List<TenantEventDetailDtoForApp> events;
  private LocalDateTime lastCursorDate;
  private Long lastCursorId;
}
