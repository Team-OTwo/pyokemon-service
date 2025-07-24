package com.pyokemon.event.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.pyokemon.event.entity.Venue;

@Mapper
public interface VenueRepository {
  @Select("SELECT venue_id, venue_name, city, street, zipcode, created_at, updated_at FROM tb_venue WHERE venue_id = #{venueId}")
  Optional<Venue> findById(Long venueId);
}
