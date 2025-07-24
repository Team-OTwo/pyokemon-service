package com.pyokemon.event.service;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.repository.EventScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
