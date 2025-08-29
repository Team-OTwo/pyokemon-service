package com.pyokemon.event.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusResponse {
  private List<SeatStatusInfo> seatStatusInfos;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SeatStatusInfo {
    private Long seatId;
    private String status;
  }
}
