package com.pyokemon.event.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.pyokemon.event.entity.Venue;

@Repository
public interface VenueRepository {
  // Add method for finding a venue by ID
  Optional<Venue> findById(Long venueId);
}
