package com.pyokemon.event.entity;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
  private Long seatId;
  private Long venueId;
  private Long seatClassId;
  private Long floor;
  private String row;
  private String col;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
