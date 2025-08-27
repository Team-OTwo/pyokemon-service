package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.SeatDetailResponseDTO;
import com.pyokemon.event.dto.SeatPriceResponseDto;
import com.pyokemon.event.entity.Event;

@Mapper
public interface EventRepository {
  Long save(Event event);

  EventDetailResponseDTO findEventDetailByEventId(Long eventId);

  List<SeatPriceResponseDto> findSeatPriceByEventScheduleId(Long eventScheduleId);

  SeatDetailResponseDTO findSeatDetailByEventScheduleIdAndSeatId(Long eventScheduleId, Long seatId);
}
