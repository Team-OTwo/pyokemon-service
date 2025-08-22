package com.pyokemon.event.bff.repository;

import com.pyokemon.event.bff.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BffEventRepository {
    Optional<BffEventScheduleDto> findEventScheduleById(@Param("eventScheduleId") Long eventScheduleId);

    Optional<BffVenueDto>        findVenueById(@Param("venueId") Long venueId);

    Optional<BffSeatDto>         findSeatById(@Param("seatId") Long seatId);

    Optional<BffEventDto>        findEventById(@Param("eventId") Long eventId);

    Optional<BffSeatClassDto>    findSeatClassById(@Param("seatClassId") Long seatClassId);

    List<BffSeatDto> findSeatsByIdIn(@Param("ids") List<Long> ids);

    List<BffSeatClassDto> findSeatClassesByIdIn(@Param("ids") List<Long> ids);

    List<BffEventScheduleDto> findEventSchedulesByIdIn(@Param("ids") List<Long> ids);

    List<BffEventDto> findEventsByIdIn(@Param("ids") List<Long> ids);

    List<BffVenueDto> findVenuesByIdIn(@Param("ids") List<Long> ids);
}
