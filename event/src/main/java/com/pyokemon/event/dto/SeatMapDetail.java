package com.pyokemon.event.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapDetail {
  private Long seatId;
  private String row;
  private String col;
  private String seatGrade;
  private boolean isBooked;

  public boolean isBooked() {
    return isBooked;
  }

  public void setBooked(boolean booked) {
    isBooked = booked;
  }
}
