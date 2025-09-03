package com.pyokemon.event.entity;

import com.pyokemon.common.entity.BaseEntity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedEvent extends BaseEntity {
  private Long eventId;
  private Long accountId;
}
