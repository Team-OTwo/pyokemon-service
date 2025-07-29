package com.pyokemon.event.entity;

import java.time.LocalDateTime;

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
public class Venue {

  private Long venueId;
  private String venueName;
  private String city;
  private String street;
  private String zipcode;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

}
