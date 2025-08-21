package com.pyokemon.event.dto.tenant;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantBookingStatusResponse {
  private Boolean success;
  private String message;
  private List<TenantBookingStatus> data;
}
