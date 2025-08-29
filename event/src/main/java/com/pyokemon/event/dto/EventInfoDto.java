package com.pyokemon.event.dto;

import lombok.Data;

@Data
public class EventInfoDto {
  private Long eventId;
  private String title;
  private String thumbnailUrl;
}
