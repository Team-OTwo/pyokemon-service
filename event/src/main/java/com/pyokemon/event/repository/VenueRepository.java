package com.pyokemon.event.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.pyokemon.event.entity.Venue;

@Mapper
public interface VenueRepository {
  Optional<Venue> findById(Long venueId);
}
