package com.pyokemon.event.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Long bookingId;
    private Long eventScheduleId;
    private Long seatId;
    private Long userId;
    private Long paymentId;
    private Booked status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Booked {
        BOOKED, CANCELLED
    }
}
