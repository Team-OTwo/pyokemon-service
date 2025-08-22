package com.pyokemon.did.remote.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
