package com.pyokemon.event.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;

@Component
public class EventMapper {

  public Event toEntity(EventRegisterDto dto) {
    dto.setStatus(Event.EventStatus.PENDING);
    return Event.builder().tenantId(dto.getTenantId()).title(dto.getTitle())
        .ageLimit(dto.getAgeLimit()).description(dto.getDescription()).genre(dto.getGenre())
        .thumbnailUrl(dto.getThumbnailUrl()).status(dto.getStatus()).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();
  }

  public EventSchedule toEventSchedule(EventScheduleDto dto, Long eventId) {
    return EventSchedule.builder().eventId(eventId).venueId(dto.getVenueId())
        .ticketOpenAt(dto.getTicketOpenAt()).eventDate(dto.getEventDate())
        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
  }

  public Price toPrice(PriceDto dto, Long eventScheduleId) {
    return Price.builder().eventScheduleId(eventScheduleId).seatClassId(dto.getSeatClassId())
        .price(dto.getPrice()).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
        .build();
  }

  public EventResponseDto toResponseDto(Event event) {
    return EventResponseDto.builder().eventId(event.getEventId()).tenantId(event.getTenantId())
        .title(event.getTitle()).ageLimit(event.getAgeLimit()).description(event.getDescription())
        .genre(event.getGenre()).thumbnailUrl(event.getThumbnailUrl()).status(event.getStatus())
        .createdAt(event.getCreatedAt()).updatedAt(event.getUpdatedAt()).build();
  }
}
