package com.pyokemon.event.controller;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponseDTO> getEventDetail(@PathVariable Long eventId){
        EventDetailResponseDTO dto = eventService.getEventDetailByEventId(eventId);
        return ResponseEntity.ok(dto);
    }

}