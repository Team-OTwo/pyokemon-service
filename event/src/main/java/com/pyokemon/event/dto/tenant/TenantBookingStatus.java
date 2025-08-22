package com.pyokemon.event.dto.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantBookingStatus {
  private Long bookingId;
  private String userName;
  private SeatInfo seat;
  private Integer price;
  private String status;
  private String paymentStatus; // 결제상태 추가
  private String bookingNumber; // 예매번호 추가 (예: #0007, #0012)

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SeatInfo {
    private String className; // A, B 등 좌석 등급
    private Integer floor; // 2층, 3층
    private String row; // 행 정보
    private String col; // 81번, 60번 등
  }
}
