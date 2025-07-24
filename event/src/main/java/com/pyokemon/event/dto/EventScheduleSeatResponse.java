package com.pyokemon.event.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleSeatResponse {
    private Long eventScheduleId;
    private List<SeatGradeRemaining> remainingSeatsByGrade;
}