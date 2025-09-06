package com.pyokemon.event.bff.dto;

import lombok.Data;

@Data
public class BffSeatDto {
  private Long id;
  private Long seatClassId;
  private Long floor;
  private String row;
  private String col;
}
