package com.pyokemon.event.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.PriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventScheduleService {

  private final EventScheduleRepository eventScheduleRepository;
  private final PriceRepository priceRepository;

  public List<EventItemResponseDTO> getTodayOpenedTickets() {
    return eventScheduleRepository.selectTodayOpenedTickets();
  }

  public List<EventItemResponseDTO> getTicketsToBeOpened() {
    return eventScheduleRepository.selectTicketsToBeOpened();
  }

  public List<EventItemResponseDTO> getConcertsByPage(String genre, int offset, int limit) {
    List<EventItemResponseDTO> events =
        eventScheduleRepository.selectEventList(genre, limit, offset);
    int total = eventScheduleRepository.getTotalCountByGenre(genre);

    for (EventItemResponseDTO event : events) {
      event.setTotal(total);
    }

    return events;
  }

  public List<EventItemResponseDTO> getEventSearch(String keyword, int offset, int limit,
      String genre) {
    List<EventItemResponseDTO> events =
        eventScheduleRepository.selectEventSearchList(keyword, limit, offset, genre);
    int total = eventScheduleRepository.getSearchTotalCount(keyword, genre);

    for (EventItemResponseDTO event : events) {
      event.setTotal(total);
    }

    return events;
  }
  
  public void registerEventSchedule(EventScheduleDto eventScheduleDto) {
    // Create and save event schedule
    EventSchedule eventSchedule = mapToEventSchedule(eventScheduleDto);
    eventScheduleRepository.save(eventSchedule);
    Long eventScheduleId = eventSchedule.getEventScheduleId();

    // Save prices if present
    if (eventScheduleDto.getPrices() != null) {
      for (PriceDto priceDto : eventScheduleDto.getPrices()) {
        priceDto.setEventScheduleId(eventScheduleId);
        Price price = mapToPrice(priceDto);
        priceRepository.save(price);
      }
    }
  }

  private EventSchedule mapToEventSchedule(EventScheduleDto dto) {
    EventSchedule eventSchedule = EventSchedule.builder().eventId(dto.getEventId())
        .venueId(dto.getVenueId()).ticketOpenAt(dto.getTicketOpenAt()).eventDate(dto.getEventDate())
        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    return eventSchedule;
  }

  private Price mapToPrice(PriceDto dto) {
    return Price.builder().eventScheduleId(dto.getEventScheduleId())
        .seatClassId(dto.getSeatClassId()).price(dto.getPrice()).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();
  }

}
