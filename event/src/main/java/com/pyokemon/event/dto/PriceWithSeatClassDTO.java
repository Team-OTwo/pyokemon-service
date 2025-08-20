package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceWithSeatClassDTO {
  private Long priceId;
  private Long eventScheduleId;
  private Long seatClassId;
  private Integer price;
  private String className; // 좌석 등급명
}
