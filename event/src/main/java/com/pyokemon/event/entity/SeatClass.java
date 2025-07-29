package com.pyokemon.event.entity;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatClass {
  private Long seatClassId;
  private String className;
  private Integer priority;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
