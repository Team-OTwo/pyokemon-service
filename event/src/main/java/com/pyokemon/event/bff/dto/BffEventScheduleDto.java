package com.pyokemon.event.bff.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BffEventScheduleDto {
    private Long eventScheduleId;
    private Long eventId;
    private Long venueId;
    private LocalDateTime eventDate;
}
