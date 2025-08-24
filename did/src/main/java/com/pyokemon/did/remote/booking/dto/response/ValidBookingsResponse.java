package com.pyokemon.did.remote.booking.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidBookingsResponse {

  private List<BookingResponse> bookings;

  private static class BookingResponse {
    private Long bookingId;
    private Long eventId;
    private Long tenantId;
  }

}
