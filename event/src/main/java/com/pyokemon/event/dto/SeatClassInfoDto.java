package com.pyokemon.event.dto;

import lombok.Data;

@Data
public class SeatClassInfoDto {
  private Long seatClassId;
  private String className;
  private Integer priority;
}
