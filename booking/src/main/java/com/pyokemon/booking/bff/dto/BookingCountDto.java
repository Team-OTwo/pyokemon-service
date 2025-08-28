package com.pyokemon.booking.bff.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookingCountDto {
  private Long eventScheduleId;
  private Long ticketCount;
}
