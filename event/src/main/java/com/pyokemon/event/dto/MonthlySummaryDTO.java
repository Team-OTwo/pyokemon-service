package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryDTO {
    private int totalRevenue;
    private int activeEventCount;
    private int totalTicketsSold;
} 