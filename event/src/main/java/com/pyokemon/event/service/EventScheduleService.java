package com.pyokemon.event.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.repository.EventScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventScheduleService {

  private final EventScheduleRepository eventScheduleRepository;

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

}
