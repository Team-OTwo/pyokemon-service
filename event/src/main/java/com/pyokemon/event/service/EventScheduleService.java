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
    return eventScheduleRepository.selectEventList(genre, limit, offset);
  }
}
