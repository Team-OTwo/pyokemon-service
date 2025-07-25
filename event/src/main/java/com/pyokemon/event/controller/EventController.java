package com.pyokemon.event.controller;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.service.EventService;
import com.pyokemon.event.service.EventScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final EventScheduleService eventScheduleService;

    //오늘 오픈 티켓
    @GetMapping("/open-today")
    public List<EventItemResponseDTO> getOpenTicketsToday() {
        return eventScheduleService.getTodayOpenedTickets();
    }

    //오픈 예정 티켓
    @GetMapping("/to-be-opend")
    public List<EventItemResponseDTO> getOpenTicketsToBeOpened() {
        return eventScheduleService.getTicketsToBeOpened();
    }
  
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponseDTO> getEventDetail(@PathVariable Long eventId){
        EventDetailResponseDTO dto = eventService.getEventDetailByEventId(eventId);
        return ResponseEntity.ok(dto);
    }

}

