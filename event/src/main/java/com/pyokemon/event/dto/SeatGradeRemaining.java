package com.pyokemon.event.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatGradeRemaining {
    private String seatGrade;
    private Integer remainingSeats;
    private int price;
}