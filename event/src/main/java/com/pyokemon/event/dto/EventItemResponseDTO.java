package com.pyokemon.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EventItemResponseDTO {
    private Long eventScheduleId;
    private Long eventId;
    private Long venueId;
    private LocalDateTime ticketOpenAt;
    private LocalDateTime eventDate;

    private String title;
    private String venueName;
}
