package com.pyokemon.event.repository;

import java.util.List;

import com.pyokemon.event.dto.SeatPriceResponseDto;
import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.entity.Event;

@Mapper
public interface EventRepository {
    Long save(Event event);

    EventDetailResponseDTO findEventDetailByEventId(Long eventId);

    List<SeatPriceResponseDto> findSeatPriceByEventScheduleId(Long eventScheduleId);
}
