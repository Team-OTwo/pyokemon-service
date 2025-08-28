package com.pyokemon.booking.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TotalSoldTicketsResponseDto {
    private Long totalTicketsSold;
}
