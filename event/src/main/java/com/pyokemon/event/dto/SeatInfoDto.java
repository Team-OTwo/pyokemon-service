package com.pyokemon.event.dto;

import lombok.Data;

@Data
public class SeatInfoDto {
  private Long seatId;
  private Long seatClassId;
  private Long floor;
  private String row;
  private String col;
}
