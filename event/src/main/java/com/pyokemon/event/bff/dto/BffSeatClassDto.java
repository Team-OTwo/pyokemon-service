package com.pyokemon.event.bff.dto;

import lombok.Data;

@Data
public class BffSeatClassDto {
  private Long seatClassId;
  private String className;
  private Integer priority;
}
