package com.pyokemon.did.remote.booking.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidBookingsRequest {

    private Long userId;
    private List<Long> bookings;

    public static ValidBookingsRequest of(Long userId, List<Long> bookingIds) {
        return ValidBookingsRequest.builder()
                .userId(userId)
                .bookings(bookingIds)
                .build();
    }

}
