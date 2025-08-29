package com.pyokemon.event.dto;

import lombok.Data;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Data
public class ScheduleDetailDto {
    private Long eventScheduleId;
    private String title;
    private String venueName;
    private LocalDateTime eventDate;
}
