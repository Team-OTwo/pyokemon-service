package com.pyokemon.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.repository.EventScheduleRepository;

class EventScheduleServiceTest {

  @Mock
  private EventScheduleRepository eventScheduleRepository;

  @InjectMocks
  private EventScheduleService eventScheduleService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getTodayOpenedTickets_호출시_리포지토리_결과가_반환되어야_함() {

    EventItemResponseDTO mockEvent = new EventItemResponseDTO();
    when(eventScheduleRepository.selectTodayOpenedTickets()).thenReturn(List.of(mockEvent));


    List<EventItemResponseDTO> result = eventScheduleService.getTodayOpenedTickets();


    assertEquals(1, result.size());
    verify(eventScheduleRepository).selectTodayOpenedTickets();
  }

  @Test
  void getConcertsByPage_정상호출() {
    // given
    String genre = "콘서트";
    int limit = 10;
    int offset = 0;

    EventItemResponseDTO mockEvent = new EventItemResponseDTO();
    when(eventScheduleRepository.selectEventList(genre, limit, offset))
        .thenReturn(List.of(mockEvent));
    when(eventScheduleRepository.getTotalCountByGenre(genre)).thenReturn(1);

    // when
    List<EventItemResponseDTO> result =
        eventScheduleService.getConcertsByPage(genre, offset, limit);

    // then
    assertEquals(1, result.size());
    assertEquals(1, result.get(0).getTotal());
    verify(eventScheduleRepository).selectEventList(genre, limit, offset);
    verify(eventScheduleRepository).getTotalCountByGenre(genre);
  }

  @Test
  void getEventSearch_키워드검색_정상호출() {
    // given
    String keyword = "뮤지컬";
    int limit = 5;
    int offset = 0;
    String genre = "전체";

    EventItemResponseDTO mockEvent = new EventItemResponseDTO();
    when(eventScheduleRepository.selectEventSearchList(keyword, limit, offset, genre))
        .thenReturn(List.of(mockEvent));
    when(eventScheduleRepository.getSearchTotalCount(keyword, genre)).thenReturn(1);

    // when
    List<EventItemResponseDTO> result =
        eventScheduleService.getEventSearch(keyword, offset, limit, genre);

    // then
    assertEquals(1, result.size());
    assertEquals(1, result.get(0).getTotal());
    verify(eventScheduleRepository).selectEventSearchList(keyword, limit, offset, genre);
    verify(eventScheduleRepository).getSearchTotalCount(keyword, genre);
  }
}
