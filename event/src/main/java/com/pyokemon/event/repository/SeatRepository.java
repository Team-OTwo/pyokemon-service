package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import com.pyokemon.event.dto.SeatInfoDto;
import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Seat;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeatRepository {
  List<Seat> findBySeatClassId(Long seatClassId);

  List<Seat> findByVenueIdAndSeatClassId(Long venueId, Long seatClassId);

  Long countByVenueIdAndSeatClassId(Long venueId, Long seatClassId);

  List<Seat> findByVenueId(Long venueId);

  Optional<SeatInfoDto> findSeatById(@Param("seatId") Long seatId);

  List<SeatInfoDto> findSeatsByIdIn(@Param("ids") List<Long> ids);
}
