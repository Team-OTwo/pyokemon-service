package com.pyokemon.payment.entity;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
  private Long bookingId;
  private Long eventScheduleId;
  private Long seatId;
  private Long accountId;
  private Long paymentId;
  private Booked status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public enum Booked {
    PENDING, BOOKED, CANCELLED
  }
}
