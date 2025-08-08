package com.pyokemon.booking.dto.response;

import com.pyokemon.booking.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfo {
    private Long eventScheduleId;
    private Long paymentId;
    private Booking.Booked status;
    private Long seatId;
    private LocalDateTime createdAt;
}
