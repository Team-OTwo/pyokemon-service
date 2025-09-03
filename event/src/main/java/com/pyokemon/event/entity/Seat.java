package com.pyokemon.event.entity;

import com.pyokemon.common.entity.BaseEntity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat extends BaseEntity {
  private Long venueId;
  private Long seatClassId;
  private Long floor;
  private String row;
  private String col;
}
