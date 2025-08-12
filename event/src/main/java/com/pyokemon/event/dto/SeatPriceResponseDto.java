package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatPriceResponseDto {
    private String className;
    private Long price;
}
