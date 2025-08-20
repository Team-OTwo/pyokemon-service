package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Seat;

@Mapper
public interface SeatRepository {
  List<Seat> findBySeatClassId(Long seatClassId);
  
  List<Seat> findByVenueIdAndSeatClassId(Long venueId, Long seatClassId);

  Long countByVenueIdAndSeatClassId(Long venueId, Long seatClassId);
}
