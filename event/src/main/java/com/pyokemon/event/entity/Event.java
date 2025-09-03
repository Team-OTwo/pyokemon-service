package com.pyokemon.event.entity;

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
public class Event extends BaseEntity {

  private Long accountId;
  private String title;
  private Long ageLimit;
  private String description;
  private String genre;
  private String thumbnailUrl;
  private EventStatus status;

  public enum EventStatus {
    APPROVED, REJECTED, PENDING, CANCELED;
  }


}
