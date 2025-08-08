package com.pyokemon.event.dto;

import java.time.LocalDateTime;

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
public class MonthlyEventDTO {
    private String title;
    private String venueName;
    private LocalDateTime eventDate;
    private int ticketCount;
} 