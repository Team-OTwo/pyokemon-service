package com.pyokemon.booking.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountIdResponse {
  private Long accountId;
  private List<BookingInfo> bookings;
}
