package com.pyokemon.event.service;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.entity.SavedEvent;
import com.pyokemon.event.repository.EventRepository;
import com.pyokemon.event.repository.SavedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
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
    void getEventDetailByEventId() {
    }

    @Test
    void saveEvent() {
        // given
        Long eventId = 1L;
        Long accountId = 1L;

        EventDetailResponseDTO mockEvent = mock(EventDetailResponseDTO.class);
        when(eventRepository.findEventDetailByEventId(eventId)).thenReturn(mockEvent);
        when(savedEventRepository.existsByAccountIdAndEventId(accountId, eventId)).thenReturn(false);

        // when
        String result = eventService.saveEvent(eventId, accountId);

        // then
        ArgumentCaptor<SavedEvent> captor = ArgumentCaptor.forClass(SavedEvent.class);
        verify(savedEventRepository, times(1)).save(captor.capture());
        assertEquals("관심 공연으로 등록되었습니다", result);
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(eventId, captor.getValue().getEventId());
    }
}