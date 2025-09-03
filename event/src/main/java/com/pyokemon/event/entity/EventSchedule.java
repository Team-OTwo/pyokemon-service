package com.pyokemon.event.entity;

import java.time.LocalDateTime;

import com.pyokemon.common.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSchedule extends BaseEntity {

  private Long eventId;
  private Long venueId;
  private LocalDateTime ticketOpenAt;
  private LocalDateTime eventDate;

}
