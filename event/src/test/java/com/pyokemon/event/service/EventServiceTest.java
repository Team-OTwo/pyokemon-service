package com.pyokemon.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.dto.SeatPriceResponseDto;
import com.pyokemon.event.entity.SavedEvent;
import com.pyokemon.event.repository.EventRepository;
import com.pyokemon.event.repository.SavedEventRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SavedEventRepository savedEventRepository;

    @InjectMocks
    private EventService eventService;

    private EventDetailResponseDTO mockEventDetail;
    private EventItemResponseDTO mockEventItem;
    private SeatPriceResponseDto mockSeatPrice;

    @BeforeEach
    void setUp() {
        mockEventDetail = EventDetailResponseDTO.builder()
                .eventId(1L)
                .title("Test Event")
                .description("Test Description")
                .genre("Pop")
                .eventScheduleId(1L)
                .build();

        mockEventItem = new EventItemResponseDTO();
        mockEventItem.setEventId(1L);
        mockEventItem.setTitle("Test Event");
        mockEventItem.setGenre("Pop");

        mockSeatPrice = new SeatPriceResponseDto("VIP", 50000L);
    }

    @Test
    void getEventDetail_ShouldReturnEventDetailWithSeatPricesAndSavedStatus() throws NotFoundException {
        Long eventId = 1L;
        Long accountId = 1L;
        List<SeatPriceResponseDto> seatPrices = Arrays.asList(mockSeatPrice);

        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(mockEventDetail);
        when(eventRepository.findSeatPriceByEventScheduleId(mockEventDetail.getEventScheduleId())).thenReturn(seatPrices);
        when(savedEventRepository.existsByAccountIdAndEventId(accountId, eventId)).thenReturn(true);

        EventDetailResponseDTO result = eventService.getEventDetail(eventId, accountId);

        assertNotNull(result);
        assertEquals(mockEventDetail.getEventId(), result.getEventId());
        assertEquals(seatPrices, result.getSeatPrice());
        assertTrue(result.isSaved());

        verify(eventRepository).findEventDetailByEventId(eventId);
        verify(eventRepository).findSeatPriceByEventScheduleId(mockEventDetail.getEventScheduleId());
        verify(savedEventRepository).existsByAccountIdAndEventId(accountId, eventId);
    }

    @Test
    void getEventDetail_WithNullAccountId_ShouldReturnEventDetailWithSavedStatusFalse() throws NotFoundException {
        Long eventId = 1L;
        Long accountId = null;
        List<SeatPriceResponseDto> seatPrices = Arrays.asList(mockSeatPrice);

        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(mockEventDetail);
        when(eventRepository.findSeatPriceByEventScheduleId(mockEventDetail.getEventScheduleId())).thenReturn(seatPrices);

        EventDetailResponseDTO result = eventService.getEventDetail(eventId, accountId);

        assertNotNull(result);
        assertEquals(mockEventDetail.getEventId(), result.getEventId());
        assertEquals(seatPrices, result.getSeatPrice());
        assertFalse(result.isSaved());

        verify(eventRepository).findEventDetailByEventId(eventId);
        verify(eventRepository).findSeatPriceByEventScheduleId(mockEventDetail.getEventScheduleId());
        verify(savedEventRepository, never()).existsByAccountIdAndEventId(any(), any());
    }

    @Test
    void getEventDetail_WhenEventNotFound_ShouldThrowNotFoundException() {
        Long eventId = 999L;
        Long accountId = 1L;

        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            eventService.getEventDetail(eventId, accountId);
        });

        verify(eventRepository).findEventDetailByEventId(eventId);
    }

    @Test
    void saveSavedEvent_WhenEventExistsAndNotSaved_ShouldSaveAndReturnSuccessMessage() {
        Long accountId = 1L;
        Long eventId = 1L;
        Object mockEvent = new Object();

        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(mockEvent);
        when(savedEventRepository.existsByAccountIdAndEventId(accountId, eventId)).thenReturn(false);

        String result = eventService.saveSavedEvent(accountId, eventId);

        assertEquals("관심 공연으로 등록되었습니다", result);
        verify(eventRepository).findEventDetailByEventId(eventId);
        verify(savedEventRepository).existsByAccountIdAndEventId(accountId, eventId);
        verify(savedEventRepository).save(any(SavedEvent.class));
    }

    @Test
    void saveSavedEvent_WhenEventExistsAndAlreadySaved_ShouldDeleteAndReturnDeleteMessage() {
        Long accountId = 1L;
        Long eventId = 1L;
        Object mockEvent = new Object();

        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(mockEvent);
        when(savedEventRepository.existsByAccountIdAndEventId(accountId, eventId)).thenReturn(true);

        String result = eventService.saveSavedEvent(accountId, eventId);

        assertEquals("관심 공연에서 삭제되었습니다", result);
        verify(eventRepository).findEventDetailByEventId(eventId);
        verify(savedEventRepository).existsByAccountIdAndEventId(accountId, eventId);
        verify(savedEventRepository).delete(accountId, eventId);
    }

    @Test
    void saveSavedEvent_WhenEventDoesNotExist_ShouldReturnErrorMessage() {
        Long accountId = 1L;
        Long eventId = 999L;

        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(null);

        String result = eventService.saveSavedEvent(accountId, eventId);

        assertEquals("존재하지 않는 공연입니다", result);
        verify(eventRepository).findEventDetailByEventId(eventId);
        verify(savedEventRepository, never()).existsByAccountIdAndEventId(any(), any());
        verify(savedEventRepository, never()).save(any());
        verify(savedEventRepository, never()).delete(any(), any());
    }

    @Test
    void getSavedEvents_ShouldReturnSavedEventsWithTotalCount() {
        Long accountId = 1L;
        int offset = 0;
        int limit = 9;
        int totalCount = 5;

        List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);
        when(savedEventRepository.findByAccountId(accountId, offset, limit)).thenReturn(expectedEvents);
        when(savedEventRepository.countTotalEventsByAccountId(accountId)).thenReturn(totalCount);

        List<EventItemResponseDTO> result = eventService.getSavedEvents(accountId, offset, limit);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(totalCount, result.get(0).getTotal());

        verify(savedEventRepository).findByAccountId(accountId, offset, limit);
        verify(savedEventRepository).countTotalEventsByAccountId(accountId);
    }
}
