package com.pyokemon.event.repository;

import java.util.Optional;

import com.pyokemon.event.dto.bff.BffVenueDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.pyokemon.event.entity.Venue;

@Mapper
public interface VenueRepository {
  Optional<Venue> findById(Long venueId);

  // bff
  BffVenueDto findBffVenueById(Long venueId);
}
