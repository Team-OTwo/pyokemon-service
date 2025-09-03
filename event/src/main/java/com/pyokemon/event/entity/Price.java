package com.pyokemon.event.entity;

import com.pyokemon.common.entity.BaseEntity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Price extends BaseEntity {
  private Long eventScheduleId;
  private Long seatClassId;
  private Integer price;
}
