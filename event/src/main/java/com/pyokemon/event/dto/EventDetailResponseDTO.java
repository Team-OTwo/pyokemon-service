package com.pyokemon.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.Event.EventStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder
public class EventDetailResponseDTO {
    // event
    private Long eventId;
    private String title;
    private Long ageLimit;
    private String description;
    private String genre;
    private String thumbnailUrl;

    // event schedule
    private Long eventScheduleId;
    private LocalDateTime ticketOpenAt;
    private LocalDateTime eventDate;

    // venue
    private String venueName;
}
