package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatInfoResponseDTO {
    private Long seatId;
    private String col;
    private String row;
    private String seatGrade;
}
