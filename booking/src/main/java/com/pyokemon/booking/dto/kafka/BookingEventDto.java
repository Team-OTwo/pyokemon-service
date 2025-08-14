package com.pyokemon.booking.dto.kafka;

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
  private Long accountId;
  private Long tenantId;
  private String status;
}
