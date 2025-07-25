package com.pyokemon.event.entity;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Price {
  private Long priceId;
  private Long eventScheduleId;
  private Long seatClassId;
  private Integer price;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
