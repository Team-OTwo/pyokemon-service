package com.pyokemon.booking.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookingCountDto {
  private Long eventScheduleId;
  private Long ticketCount;
}
