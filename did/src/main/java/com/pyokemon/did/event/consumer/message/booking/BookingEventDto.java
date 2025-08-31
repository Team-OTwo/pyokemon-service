package com.pyokemon.did.event.consumer.message.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEventDto {
  private Long bookingId;
  private Long eventScheduleId;
  private Long seatId;
  private Long accountId;
  private Long tenantId;
  private String status;
}
