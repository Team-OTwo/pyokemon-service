package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import com.pyokemon.event.dto.SeatClassInfoDto;
import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.SeatClass;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeatClassRepository {
  Optional<SeatClass> findByClassName(String className);

  List<SeatClass> findAll();

  List<SeatClass> findByVenueId(Long venueId);

  Optional<SeatClass> findById(Long seatClassId);

  Optional<SeatClassInfoDto> findSeatClassById(@Param("seatClassId") Long seatClassId);

  List<SeatClassInfoDto> findSeatClassesByIdIn(@Param("ids") List<Long> ids);
}
