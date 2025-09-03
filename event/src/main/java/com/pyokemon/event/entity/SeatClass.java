package com.pyokemon.event.entity;

import com.pyokemon.common.entity.BaseEntity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatClass extends BaseEntity {
  private String className;
  private Integer priority;
}
