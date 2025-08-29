package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VenueInfoDto {
  private Long venueId;
  private String venueName;
}
