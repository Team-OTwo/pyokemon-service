package com.pyokemon.event.dto.bff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BffEventScheduleDto {
    private Long id;
    private Long eventId;
    private Long venueId;
    private LocalDateTime eventDate;
}