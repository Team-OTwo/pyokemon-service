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
public class Venue extends BaseEntity {

  private String venueName;
  private String city;
  private String street;
  private String zipcode;

}
