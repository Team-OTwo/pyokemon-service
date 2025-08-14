package com.pyokemon.booking.dto.bff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BffBookingDto {
    private Long id;
    private Long accountId;
    private Long eventScheduleId;
    private Long seatId;
    private Long paymentId;
    private BookingStatus status;  // PENDDING, BOOKED, CANCELLED
    private LocalDateTime updatedAt;
}
