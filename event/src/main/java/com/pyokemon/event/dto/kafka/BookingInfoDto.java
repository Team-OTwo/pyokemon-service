package com.pyokemon.event.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfoDto {
    private Long bookingId;
    private Long eventScheduleId;
    private Long seatId;
    private String status;
}
