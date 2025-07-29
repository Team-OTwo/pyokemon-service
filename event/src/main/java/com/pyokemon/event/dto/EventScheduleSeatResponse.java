package com.pyokemon.event.dto;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleSeatResponse {
  private Long eventScheduleId;
  private List<SeatGradeRemaining> remainingSeatsByGrade;
}
