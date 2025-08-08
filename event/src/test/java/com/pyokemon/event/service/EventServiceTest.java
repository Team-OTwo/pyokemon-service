package com.pyokemon.event.service;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.entity.SavedEvent;
import com.pyokemon.event.repository.EventRepository;
import com.pyokemon.event.repository.SavedEventRepository;
import org.apache.ibatis.javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @InjectMocks
    private EventService eventService;
    @Mock
    private SavedEventRepository savedEventRepository;
    @Mock
    private EventRepository eventRepository;


    @Test
    void 공연상세정보조회_비회원() {
        // given
        Long eventId = 1L;
        Long accountId = 1L; // 비회원 테스트

        EventDetailResponseDTO mockDto = EventDetailResponseDTO.builder()
                .eventId(eventId)
                .title("테스트 공연")
                .ageLimit(15L)
                .description("공연 설명입니다")
                .genre("뮤지컬")
                .thumbnailUrl("http://image.url")
                .eventScheduleId(100L)
                .ticketOpenAt(LocalDateTime.of(2025, 8, 10, 10, 0))
                .eventDate(LocalDateTime.of(2025, 8, 15, 19, 30))
                .venueName("서울아트센터")
                .isSaved(false)
                .build();

        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(mockDto);

        // when
        EventDetailResponseDTO result = null;
        try {
            result = eventService.getEventDetail(eventId, accountId);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

        // then
        assertNotNull(result);
        assertEquals(mockDto.getEventId(), result.getEventId());
        assertEquals(mockDto.getTitle(), result.getTitle());
        assertEquals(mockDto.getAgeLimit(), result.getAgeLimit());
        assertEquals(mockDto.getDescription(), result.getDescription());
        assertEquals(mockDto.getGenre(), result.getGenre());
        assertEquals(mockDto.getThumbnailUrl(), result.getThumbnailUrl());
        assertEquals(mockDto.getEventScheduleId(), result.getEventScheduleId());
        assertEquals(mockDto.getTicketOpenAt(), result.getTicketOpenAt());
        assertEquals(mockDto.getEventDate(), result.getEventDate());
        assertEquals(mockDto.getVenueName(), result.getVenueName());
        assertFalse(result.isSaved());
    }

    @Test
    void saveSavedEvent() {
        // given
        Long eventId = 1L;
        Long accountId = 1L;

        EventDetailResponseDTO mockEvent = mock(EventDetailResponseDTO.class);
        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(mockEvent);
        when(savedEventRepository.existsByAccountIdAndEventId(accountId, eventId)).thenReturn(false);

        // when
        String result = eventService.saveSavedEvent(eventId, accountId);

        // then
        ArgumentCaptor<SavedEvent> captor = ArgumentCaptor.forClass(SavedEvent.class);
        verify(savedEventRepository, times(1)).save(captor.capture());
        assertEquals("관심 공연으로 등록되었습니다", result);
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(eventId, captor.getValue().getEventId());
    }



    @Test
    void saveSavedEvent_존재하지_않는_공연일_경우() {
        // given
        Long accountId = 1L;
        Long eventId = 100L;
        given(eventRepository.findEventDetailByEventId(eventId)).willReturn(null);

        // when
        String result = eventService.saveSavedEvent(accountId, eventId);

        // then
        assertEquals("존재하지 않는 공연입니다", result);
        verify(savedEventRepository, never()).save(any());
        verify(savedEventRepository, never()).delete(any(), any());
    }


    @Test
    void saveSavedEvent_이미_관심공연일_경우_삭제() {
        // given
        Long accountId = 1L;
        Long eventId = 1L;

        EventDetailResponseDTO mockEvent = mock(EventDetailResponseDTO.class);
        given(eventRepository.findEventDetailByEventId(eventId)).willReturn(mockEvent);
        given(savedEventRepository.existsByAccountIdAndEventId(accountId, eventId)).willReturn(true);

        // when
        String result = eventService.saveSavedEvent(accountId, eventId);

        // then
        assertEquals("관심 공연에서 삭제되었습니다", result);
        verify(savedEventRepository).delete(accountId, eventId);
        verify(savedEventRepository, never()).save(any());
    }

    @Test
    void getSavedEvents(){
        // given
        Long accountId = 1L;
        int offset = 0;
        int limit = 9;

        EventItemResponseDTO event1 = new EventItemResponseDTO();
        event1.setEventId(1L);
        event1.setTitle("콘서트 A");

        EventItemResponseDTO event2 = new EventItemResponseDTO();
        event2.setEventId(2L);
        event2.setTitle("콘서트 B");

        List<EventItemResponseDTO> mockEventList = List.of(event1, event2);
        int mockTotalCount = 15;

        when(savedEventRepository.findByAccountId(accountId, offset, limit)).thenReturn(mockEventList);
        when(savedEventRepository.countTotalEventsByAccountId(accountId)).thenReturn(mockTotalCount);

        // when
        List<EventItemResponseDTO> result = eventService.getSavedEvents(accountId, offset, limit);

        // then
        assertEquals(2, result.size());
        assertEquals("콘서트 A", result.get(0).getTitle());
        assertEquals("콘서트 B", result.get(1).getTitle());
        assertEquals(mockTotalCount, result.get(0).getTotal());
        assertEquals(mockTotalCount, result.get(1).getTotal());

        verify(savedEventRepository, times(1)).findByAccountId(accountId, offset, limit);
        verify(savedEventRepository, times(1)).countTotalEventsByAccountId(accountId);
    }

}