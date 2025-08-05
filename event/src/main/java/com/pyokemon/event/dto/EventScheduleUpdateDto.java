package com.pyokemon.event.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleUpdateDto {

    private Long eventScheduleId;

    @NotNull(message = "Venue ID is required")
    private Long venueId;

    @NotNull(message = "Ticket open date is required")
    @Future(message = "Ticket open date must be in the future")
    private LocalDateTime ticketOpenAt;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;

    @Valid
    private List<PriceUpdateDto> prices;
} 