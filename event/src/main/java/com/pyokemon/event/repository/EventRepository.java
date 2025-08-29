package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import com.pyokemon.event.dto.EventInfoDto;
import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.SeatDetailResponseDTO;
import com.pyokemon.event.dto.SeatPriceResponseDto;
import com.pyokemon.event.entity.Event;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EventRepository {
  Long save(Event event);

  EventDetailResponseDTO findEventDetailByEventId(Long eventId);

  List<SeatPriceResponseDto> findSeatPriceByEventScheduleId(Long eventScheduleId);

  SeatDetailResponseDTO findSeatDetailByEventScheduleIdAndSeatId(Long eventScheduleId, Long seatId);

  Optional<EventInfoDto> findEventById(@Param("eventId") Long eventId);

  List<Long> findIdsByGenre(@Param("genre") String genre);

  List<EventInfoDto> findEventsByIdIn(@Param("ids") List<Long> ids);

  List<Long> findIdsByEventIds(@Param("eventIds") List<Long> eventIds);
}
