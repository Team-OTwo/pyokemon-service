package com.pyokemon.event.service;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;

    public EventDetailResponseDTO getEventDetailByEventId(Long eventId){
        return eventRepository.findEventDetailByEventId(eventId);
    }
}
