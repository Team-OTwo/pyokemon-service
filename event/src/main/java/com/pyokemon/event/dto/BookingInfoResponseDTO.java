package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingInfoResponseDTO {
  private Long seatClassId;
  private String seatGrade;
  private Integer price;
  private Long seatCount;
}
