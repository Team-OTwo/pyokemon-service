package com.pyokemon.event.repository;

import com.pyokemon.event.entity.Seat;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SeatRepository {
    List<Seat> findByVenueId(Long venueId);
    Optional<Seat> findById(Long seatId);
}
