package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import com.pyokemon.event.dto.VenueInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.pyokemon.event.entity.Venue;

@Mapper
public interface VenueRepository {
  Optional<Venue> findById(Long venueId);

  Optional<VenueInfoDto> findVenueById(@Param("venueId") Long venueId);

  List<VenueInfoDto> findVenuesByIdIn(@Param("ids") List<Long> ids);
}
