package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Seat;

@Mapper
public interface SeatRepository {
  List<Seat> findByVenueId(Long venueId);

  Optional<Seat> findById(Long seatId);
}
