package com.pyokemon.booking.bff.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
  private Long bookingId;
  private Long accountId;
  private Long eventScheduleId;
  private Long tenantId;
  private Long seatId;
  private Long paymentId;
  private Enum status;
  private LocalDateTime updatedAt;
}
