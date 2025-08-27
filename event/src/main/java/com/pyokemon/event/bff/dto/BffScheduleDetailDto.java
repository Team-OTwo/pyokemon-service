package com.pyokemon.event.bff.dto;

import lombok.Data;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Data
public class BffScheduleDetailDto {
    private Long eventScheduleId;
    private String title;
    private String venueName;
    private LocalDateTime eventDate;
}
