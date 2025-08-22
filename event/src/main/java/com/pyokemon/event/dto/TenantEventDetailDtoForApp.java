package com.pyokemon.event.dto;

import com.pyokemon.event.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantEventDetailDtoForApp {
    private Long eventId;
    private String title;
    private LocalDateTime eventDate;
    private String venueName;
    private String genre;
}
