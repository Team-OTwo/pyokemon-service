package com.pyokemon.event.repository;

import java.util.List;

import com.pyokemon.event.dto.SeatPriceResponseDto;
import com.pyokemon.event.dto.bff.BffEventDto;
import com.pyokemon.event.dto.bff.BffVenueDto;
import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.entity.Event;

@Mapper
public interface EventRepository {
    List<Event> findByTenantId(Long tenantId);

    List<Event> findByAccountId(Long accountId);

    List<Event> findByStatus(Event.EventStatus status);

    List<Event> findByGenre(String genre);

    List<Event> findByTitleContainingIgnoreCase(String title);

    List<Event> findByAgeLimit(Long ageLimit);

    Long save(Event event);

    EventDetailResponseDTO findEventDetailByEventId(Long eventId);

    Event findById(Long eventId);

    int updateEvent(Event event);

    List<SeatPriceResponseDto> findSeatPriceByEventScheduleId(Long eventScheduleId);



    // bff
    BffEventDto findBffEventById(Long eventId);

}
