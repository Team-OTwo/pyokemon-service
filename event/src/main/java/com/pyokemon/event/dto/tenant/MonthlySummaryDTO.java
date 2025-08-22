package com.pyokemon.event.dto.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryDTO {
  private Integer totalRevenue;
  private Integer activeEventCount;
  private Integer totalTicketsSold;
}
